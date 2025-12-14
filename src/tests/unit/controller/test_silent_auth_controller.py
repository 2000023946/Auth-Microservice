import pytest  # type: ignore
from unittest.mock import Mock
from src.controller.inbound.silent_auth_controller import SilentAuthController
from src.app.domain.exceptions import TokenError

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_token_service():
    return Mock()


@pytest.fixture
def mock_user_service():
    return Mock()


@pytest.fixture
def silent_auth_controller(mock_token_service, mock_user_service):
    return SilentAuthController(mock_token_service, mock_user_service)


# ----------------------------------------------------------------
# Happy Path Tests
# ----------------------------------------------------------------


def test_silent_auth_valid_access_token(
    silent_auth_controller, mock_token_service, mock_user_service
):
    """
    Scenario: Browser has a valid Access Token.
    Expected:
    1. Validate token.
    2. Return User Profile (200 OK).
    3. NO new cookies needed.
    """
    # Arrange: Request has valid access token
    mock_request = Mock()
    mock_request.cookies = {"access_token": "valid_access_token"}

    # Mock: Token is valid and belongs to User 123
    mock_token_service.validate_and_get_user_id.return_value = 123

    # Mock: User Service finds user
    mock_user_service.get_user_by_id.return_value = Mock(id=123, email="test@gt.edu")

    # Act
    response = silent_auth_controller.handle(mock_request)

    # Assert
    assert response.status_code == 200
    assert response.body["email"] == "test@gt.edu"
    assert response.body["isAuthenticated"] is True

    # Ensure we didn't rotate tokens unnecessarily
    assert "Set-Cookie" not in response.headers


def test_silent_auth_expired_access_uses_refresh(
    silent_auth_controller, mock_token_service, mock_user_service
):
    """
    Scenario: Access Token expired, but Refresh Token is valid.
    Expected:
    1. Access check fails.
    2. Refresh logic triggers (Rotation).
    3. Returns User Profile + NEW Cookies.
    """
    # Arrange: Access expired, Refresh present
    mock_request = Mock()
    mock_request.cookies = {
        "access_token": "expired_token",
        "refresh_token": "valid_refresh",
    }

    # 1. Access token fails
    mock_token_service.validate_and_get_user_id.side_effect = TokenError("Expired")

    # 2. Refresh logic kicks in (returns Tuple)
    mock_token_service.refresh_token.return_value = (
        "new_access_jwt",
        "new_refresh_jwt",
    )

    # 3. Extract ID from the NEW token
    mock_token_service.get_user_id_from_token.return_value = 456

    # 4. Fetch User
    mock_user_service.get_user_by_id.return_value = Mock(
        id=456, email="refreshed@gt.edu"
    )

    # Act
    response = silent_auth_controller.handle(mock_request)

    # Assert
    assert response.status_code == 200
    assert response.body["email"] == "refreshed@gt.edu"

    # Verify New Cookies are set
    cookies = response.headers["Set-Cookie"]
    assert "access_token=new_access_jwt" in cookies
    assert "refresh_token=new_refresh_jwt" in cookies


# ----------------------------------------------------------------
# Failure Tests
# ----------------------------------------------------------------


def test_silent_auth_fails_if_both_tokens_invalid(
    silent_auth_controller, mock_token_service
):
    """
    Scenario: Access expired AND Refresh expired/blacklisted.
    Expected: 401 Unauthorized (User must login again).
    """
    mock_request = Mock()
    mock_request.cookies = {"access_token": "bad", "refresh_token": "bad"}

    # Both fail
    mock_token_service.validate_and_get_user_id.side_effect = TokenError("Expired")
    mock_token_service.refresh_token.side_effect = TokenError("Blacklisted")

    response = silent_auth_controller.handle(mock_request)

    assert response.status_code == 401
    assert response.body["isAuthenticated"] is False
