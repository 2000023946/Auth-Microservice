import pytest  # type: ignore
from unittest.mock import Mock, ANY
import jwt  # type: ignore
from datetime import datetime, timedelta
from src.app.services.token_service import TokenService
from src.app.domain.exceptions import TokenError
from datetime import timezone

# ----------------------------------------------------------------
# Test Config
# ----------------------------------------------------------------
TEST_SECRET = "super_secret_key_for_testing"


@pytest.fixture
def mock_redis():
    """Mocks the RedisCache layer."""
    return Mock()


@pytest.fixture
def token_service(mock_redis):
    """Initializes Service with the Mock Redis and a Test Secret."""
    return TokenService(redis_cache=mock_redis, secret_key=TEST_SECRET)


# ----------------------------------------------------------------
# 1. Create JWT Tests (Login)
# ----------------------------------------------------------------


def test_create_jwt_returns_pair(token_service):
    """
    Scenario: User logs in.
    Expected: Returns a tuple (access_token, refresh_token).
    """
    user_id = 123

    # Act
    access, refresh = token_service.create_jwt(user_id)

    # Assert - They must be strings
    assert isinstance(access, str)
    assert isinstance(refresh, str)

    # Assert - Payloads must be correct
    access_payload = jwt.decode(access, TEST_SECRET, algorithms=["HS256"])
    refresh_payload = jwt.decode(refresh, TEST_SECRET, algorithms=["HS256"])

    assert access_payload["sub"] == str(user_id)
    assert access_payload["type"] == "access"

    assert refresh_payload["sub"] == str(user_id)
    assert refresh_payload["type"] == "refresh"

    # Assert - JTIs must be unique
    assert access_payload["jti"] != refresh_payload["jti"]


# ----------------------------------------------------------------
# 2. Refresh Token Tests (Rotation)
# ----------------------------------------------------------------
def test_refresh_token_success(token_service, mock_redis):
    """
    Scenario: Valid refresh token is provided.
    Expected:
    1. Checks if old token is blacklisted.
    2. Blacklists the OLD token.
    3. Returns a NEW access token AND a NEW refresh token.
    """
    # Arrange
    user_id = 456
    old_token = token_service._generate_token(user_id, "refresh")
    mock_redis.is_blacklisted.return_value = False

    # Act
    # FIX: Unpack the tuple into two variables
    new_access, new_refresh = token_service.refresh_token(old_token)

    # Assert
    mock_redis.is_blacklisted.assert_called_once()
    mock_redis.blacklist_token.assert_called_once()

    # Verify Access Token
    payload_access = jwt.decode(new_access, TEST_SECRET, algorithms=["HS256"])
    assert payload_access["sub"] == str(user_id)
    assert payload_access["type"] == "access"

    # Verify Refresh Token (The Service now returns this too!)
    payload_refresh = jwt.decode(new_refresh, TEST_SECRET, algorithms=["HS256"])
    assert payload_refresh["sub"] == str(user_id)
    assert payload_refresh["type"] == "refresh"


def test_refresh_fails_if_blacklisted(token_service, mock_redis):
    """
    Scenario: Token reuse attack (Hacker uses an old refresh token).
    Expected: Raises TokenError.
    """
    # Arrange
    old_token = token_service._generate_token(user_id=1, token_type="refresh")

    # Mock Redis saying "YES, this is blacklisted"
    mock_redis.is_blacklisted.return_value = True

    # Act & Assert
    with pytest.raises(TokenError, match="Token is blacklisted"):
        token_service.refresh_token(old_token)


def test_refresh_fails_if_expired(token_service):
    """
    Scenario: Refresh token is too old.
    Expected: Raises TokenError (ExpiredSignature).
    """
    # Arrange: Manually forge an expired token
    expired_payload = {
        "sub": 1,
        "type": "refresh",
        "exp": datetime.now(timezone.utc) - timedelta(days=1),  # Past
    }
    bad_token = jwt.encode(expired_payload, TEST_SECRET, algorithm="HS256")

    # Act & Assert
    with pytest.raises(TokenError, match="expired"):
        token_service.refresh_token(bad_token)


# ----------------------------------------------------------------
# 3. Logout Tests
# ----------------------------------------------------------------


def test_logout_blacklists_token(token_service, mock_redis):
    """
    Scenario: User logs out.
    Expected: The token JTI is added to Redis.
    """
    # Arrange
    token = token_service._generate_token(user_id=999, token_type="refresh")
    decoded = jwt.decode(token, TEST_SECRET, algorithms=["HS256"])
    jti = decoded["jti"]

    # Act
    token_service.logout(token)

    # Assert
    # Verify we sent the specific JTI to Redis
    mock_redis.blacklist_token.assert_called_with(jti, ANY)
