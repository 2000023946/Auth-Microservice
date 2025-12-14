import pytest
from unittest.mock import Mock, ANY
import jwt
from datetime import datetime, timedelta, timezone
from src.app.services.token_service import TokenService
from src.app.domain.exceptions import TokenError

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

    # FIX: Compare against string version of user_id
    assert access_payload["sub"] == str(user_id)  # <--- FIXED
    assert access_payload["type"] == "access"

    assert refresh_payload["sub"] == str(user_id)  # <--- FIXED
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
    1. Checks if old token is blacklisted (must be False).
    2. Blacklists the OLD token (Token Rotation).
    3. Returns a NEW access token.
    """
    # Arrange: Create a valid refresh token
    user_id = 456
    old_token = token_service._generate_token(user_id, "refresh")

    # Mock Redis saying "This token is NOT blacklisted"
    mock_redis.is_blacklisted.return_value = False

    # Act
    new_access, new_refresh = token_service.refresh_token(old_token)

    # Assert
    # 1. Did we check blacklist?
    mock_redis.is_blacklisted.assert_called_once()

    # 2. Did we blacklist the OLD token?
    mock_redis.blacklist_token.assert_called_once()

    # 3. Is the new token valid?
    payload = jwt.decode(new_access, TEST_SECRET, algorithms=["HS256"])

    # FIX: Compare against string version
    assert payload["sub"] == str(user_id)  # <--- FIXED
    assert payload["type"] == "access"


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
    # FIX: Use timezone-aware datetime
    expired_payload = {
        "sub": "1",
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
    mock_redis.blacklist_token.assert_called_with(jti, ANY)
