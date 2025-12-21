import pytest
from unittest.mock import Mock, ANY
from datetime import datetime, timezone
from src.app.services.token_service import TokenService
from src.app.domain.exceptions import TokenError

# ----------------------------------------------------------------
# Test Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_redis():
    """Mocks the ICache interface."""
    return Mock()


@pytest.fixture
def mock_provider():
    """Mocks the ITokenAdapter (The JWT Infrastructure)."""
    return Mock()


@pytest.fixture
def token_service(mock_redis, mock_provider):
    """Initializes Service with mocked infrastructure."""
    return TokenService(redis_cache=mock_redis, token_provider=mock_provider)


# ----------------------------------------------------------------
# 1. Create Token Tests
# ----------------------------------------------------------------


def test_create_jwt_success(token_service, mock_provider):
    """
    Scenario: Successful login.
    Expected: Service generates payloads and delegates signing to provider.
    """
    user_id = 123
    # Setup mock behavior for the two calls (Access then Refresh)
    mock_provider.encode.side_effect = ["access_token_str", "refresh_token_str"]

    # Act
    access, refresh = token_service.create_jwt(user_id)

    # Assert
    assert access == "access_token_str"
    assert refresh == "refresh_token_str"

    # Verify the service sent the correct user_id to the provider
    assert mock_provider.encode.call_count == 2
    # Check first call (Access Token)
    first_call_args = mock_provider.encode.call_args_list[0][0][0]
    assert first_call_args["sub"] == str(user_id)
    assert first_call_args["type"] == "access"


# ----------------------------------------------------------------
# 2. Refresh Token Tests (Rotation)
# ----------------------------------------------------------------


def test_refresh_token_rotation_success(token_service, mock_redis, mock_provider):
    """
    Scenario: Valid refresh token is used.
    Expected: Old token blacklisted, new pair generated.
    """
    # Arrange
    old_token_str = "valid_old_refresh_token"
    # Set expiration to 1 hour in the future
    future_exp = datetime.now(timezone.utc).timestamp() + 3600

    mock_provider.decode.return_value = {
        "sub": "456",
        "type": "refresh",
        "jti": "old-jti-123",
        "exp": future_exp,
    }
    mock_redis.is_blacklisted.return_value = False
    mock_provider.encode.side_effect = ["new_access", "new_refresh"]

    # Act
    new_access, new_refresh = token_service.refresh_token(old_token_str)

    # Assert
    # 1. Check blacklist was consulted
    mock_redis.is_blacklisted.assert_called_with("old-jti-123")
    # 2. Check old token was added to blacklist
    mock_redis.blacklist_token.assert_called_once()
    # 3. Check new pair was issued
    assert new_access == "new_access"
    assert new_refresh == "new_refresh"


def test_refresh_fails_if_blacklisted(token_service, mock_redis, mock_provider):
    """
    Scenario: Attacker tries to reuse a rotated token.
    """
    mock_provider.decode.return_value = {
        "sub": "1",
        "type": "refresh",
        "jti": "stolen-jti",
        "exp": 9999999999,
    }
    mock_redis.is_blacklisted.return_value = True

    with pytest.raises(TokenError, match="Token is blacklisted"):
        token_service.refresh_token("stolen_token")


def test_refresh_fails_on_wrong_token_type(token_service, mock_provider):
    """
    Scenario: User sends an Access token to the refresh endpoint.
    """
    mock_provider.decode.return_value = {"type": "access", "sub": "1"}

    with pytest.raises(TokenError, match="Invalid token type"):
        token_service.refresh_token("access_token_acting_as_refresh")


# ----------------------------------------------------------------
# 3. Logout & Security Tests
# ----------------------------------------------------------------


def test_logout_calculates_correct_ttl(token_service, mock_redis, mock_provider):
    """
    Scenario: Logout occurs.
    Expected: Redis is called with the remaining TTL of the token.
    """
    # Arrange
    now_ts = datetime.now(timezone.utc).timestamp()
    token_exp = now_ts + 500  # 500 seconds left
    mock_provider.decode.return_value = {"jti": "logout-jti", "exp": token_exp}

    # Act
    token_service.logout("active_token")

    # Assert
    # We use pytest.approx because time moves forward slightly during the test
    mock_redis.blacklist_token.assert_called_with(
        "logout-jti", pytest.approx(500, abs=1)
    )


def test_validate_user_id_success(token_service, mock_provider):
    """Checks the helper used by the API to identify users."""
    mock_provider.decode.return_value = {"type": "access", "sub": "789"}

    uid = token_service.validate_and_get_user_id("valid_access_token")

    assert uid == str(789)
