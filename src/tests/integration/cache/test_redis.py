import pytest
from unittest.mock import Mock
from src.cache.outbound.redis_cache import RedisCache

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_redis_client():
    """Mocks the actual 'redis' library client."""
    return Mock()


@pytest.fixture
def redis_cache(mock_redis_client):
    """Injects the mock client into our wrapper class."""
    return RedisCache(mock_redis_client)


# ----------------------------------------------------------------
# Tests
# ----------------------------------------------------------------


def test_blacklist_token_sets_expiry(redis_cache, mock_redis_client):
    """
    Scenario: Blacklisting a token (Logout).
    Expected: Calls setex(key, time, value).
    """
    # Act
    # Blacklist JTI "abc-123" for 900 seconds (15 mins)
    redis_cache.blacklist_token("abc-123", 900)

    # Assert
    # Verify we set the key with the specific Time-To-Live (TTL)
    mock_redis_client.setex.assert_called_with("abc-123", 900, "blacklisted")


def test_is_blacklisted_returns_true(redis_cache, mock_redis_client):
    """
    Scenario: Checking a token that WAS revoked.
    Expected: Returns True.
    """
    # Arrange: Redis 'exists' returns 1 (found)
    mock_redis_client.exists.return_value = 1

    # Act
    result = redis_cache.is_blacklisted("bad-token")

    # Assert
    assert result is True
    mock_redis_client.exists.assert_called_with("bad-token")


def test_is_blacklisted_returns_false(redis_cache, mock_redis_client):
    """
    Scenario: Checking a valid token.
    Expected: Returns False.
    """
    # Arrange: Redis 'exists' returns 0 (not found)
    mock_redis_client.exists.return_value = 0

    # Act
    result = redis_cache.is_blacklisted("good-token")

    # Assert
    assert result is False
