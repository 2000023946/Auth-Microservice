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
    Expected: 200 OK, User returned.
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {"access_token": "valid_access_token"}

    # Mock Token Service
    mock_token_service.validate_and_get_user_id.return_value = 123

    # --- FIX 1: Mock the correct method (fetchUser) & explicitly set email ---
    mock_user = Mock()
    mock_user.id = 123
    mock_user.email = "test@gt.edu"
    mock_user_service.fetchUser.return_value = mock_user

    # Act
    response = silent_auth_controller.handle(mock_request)

    # Assert
    assert response.status_code == 200
    # Verify the body content if possible
    assert response.body["email"] == "test@gt.edu"


def test_silent_auth_expired_access_uses_refresh(
    silent_auth_controller, mock_token_service, mock_user_service
):
    """
    Scenario: Access Token expired, Refresh Token valid -> Rotation.
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {
        "access_token": "expired_token",
        "refresh_token": "valid_refresh",
    }

    # 1. Access token fails
    mock_token_service.validate_and_get_user_id.side_effect = TokenError("Expired")

    # 2. Refresh logic triggers
    mock_token_service.refresh_token.return_value = (
        "new_access_jwt",
        "new_refresh_jwt",
    )

    # 3. Get ID from new token
    mock_token_service.get_user_id_from_token.return_value = 456

    # --- FIX 1 REPEATED: Mock fetchUser correctly ---
    mock_user = Mock()
    mock_user.id = 456
    mock_user.email = "refreshed@gt.edu"
    mock_user_service.fetchUser.return_value = mock_user

    # Act
    response = silent_auth_controller.handle(mock_request)

    # --- FIX 2: Handle Headers as a List of Tuples ---
    # response.headers is [('Set-Cookie', '...'), ('Set-Cookie', '...')]

    # Helper to find cookies in the list
    cookies_found = [value for key, value in response.headers if key == "Set-Cookie"]

    # Assert
    assert response.status_code == 200
    assert len(cookies_found) == 2  # Should have set both Access and Refresh cookies
    assert "access_token=new_access_jwt" in cookies_found[0]
    assert "refresh_token=new_refresh_jwt" in cookies_found[1]


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
