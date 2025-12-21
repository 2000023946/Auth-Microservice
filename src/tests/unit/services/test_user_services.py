import pytest
from unittest.mock import Mock, ANY
from datetime import datetime, timedelta, timezone
from src.app.services.token_service import TokenService
from src.app.domain.exceptions import TokenError

# ----------------------------------------------------------------
# Test Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_redis():
    """Mocks the RedisCache layer (ICache)."""
    return Mock()


@pytest.fixture
def mock_provider():
    """Mocks the Token Provider (ITokenProvider)."""
    return Mock()


@pytest.fixture
def token_service(mock_redis, mock_provider):
    """Initializes Service with Mocks."""
    return TokenService(redis_cache=mock_redis, token_provider=mock_provider)


# ----------------------------------------------------------------
# 1. Create Token Tests
# ----------------------------------------------------------------


def test_create_jwt_orchestration(token_service, mock_provider):
    """
    Scenario: User logs in.
    Expected: Service generates payloads and calls provider twice.
    """
    user_id = 123
    mock_provider.encode.side_effect = ["access_token_str", "refresh_token_str"]

    # Act
    access, refresh = token_service.create_jwt(user_id)

    # Assert
    assert access == "access_token_str"
    assert refresh == "refresh_token_str"

    # Verify the provider was called with a dict containing the sub
    assert mock_provider.encode.call_count == 2
    args, _ = mock_provider.encode.call_args_list[0]
    assert args[0]["sub"] == str(user_id)


# ----------------------------------------------------------------
# 2. Refresh Token Tests (Rotation)
# ----------------------------------------------------------------


def test_refresh_token_success(token_service, mock_redis, mock_provider):
    """
    Scenario: Valid refresh token rotation.
    Expected:
    1. Decodes old token.
    2. Checks/Updates Redis.
    3. Encodes new tokens.
    """
    # Arrange
    old_token_str = "old_refresh_token"
    # Future timestamp (1 hour from now)
    future_exp = datetime.now(timezone.utc).timestamp() + 3600

    mock_provider.decode.return_value = {
        "sub": "456",
        "type": "refresh",
        "jti": "uuid-123",
        "exp": future_exp,
    }
    mock_redis.is_blacklisted.return_value = False
    mock_provider.encode.side_effect = ["new_access", "new_refresh"]

    # Act
    new_access, new_refresh = token_service.refresh_token(old_token_str)

    # Assert
    mock_provider.decode.assert_called_with(old_token_str)
    mock_redis.is_blacklisted.assert_called_with("uuid-123")
    mock_redis.blacklist_token.assert_called_once()  # Old token rotated
    assert new_access == "new_access"


def test_refresh_fails_if_wrong_type(token_service, mock_provider):
    """
    Scenario: User tries to refresh using an ACCESS token.
    Expected: Raises TokenError.
    """
    mock_provider.decode.return_value = {"type": "access", "sub": "1"}

    with pytest.raises(TokenError, match="Invalid token type"):
        token_service.refresh_token("some_token")


def test_refresh_fails_if_blacklisted(token_service, mock_redis, mock_provider):
    """
    Scenario: Token reuse attempt.
    """
    mock_provider.decode.return_value = {
        "type": "refresh",
        "jti": "stolen-id",
        "sub": "1",
        "exp": datetime.now(timezone.utc).timestamp() + 100,
    }
    mock_redis.is_blacklisted.return_value = True

    with pytest.raises(TokenError, match="Token is blacklisted"):
        token_service.refresh_token("stolen_token")


# ----------------------------------------------------------------
# 3. Logout Tests
# ----------------------------------------------------------------


def test_logout_logic(token_service, mock_redis, mock_provider):
    """
    Scenario: Logout correctly calculates TTL for Redis.
    """
    now_ts = datetime.now(timezone.utc).timestamp()
    mock_provider.decode.return_value = {
        "jti": "logout-jti",
        "exp": now_ts + 500,  # 500 seconds remaining
    }

    token_service.logout("valid_token")

    # Assert blacklist called with roughly 500s TTL
    mock_redis.blacklist_token.assert_called_with(
        "logout-jti", pytest.approx(500, rel=1e-2)
    )
