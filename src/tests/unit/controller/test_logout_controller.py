import pytest  # type: ignore
from unittest.mock import Mock
from src.controller.inbound.logout_controller import LogoutController

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_token_service():
    return Mock()


@pytest.fixture
def logout_controller(mock_token_service):
    return LogoutController(mock_token_service)


# ----------------------------------------------------------------
# Tests
# ----------------------------------------------------------------


def test_logout_success(logout_controller, mock_token_service):
    """
    Scenario: User sends a valid logout request with a refresh cookie.
    Expected:
    1. TokenService.logout() is called with the token.
    2. Response is 200 OK.
    3. Cookies are cleared (Max-Age=0).
    """
    # Arrange: Mock Request with a cookie
    mock_request = Mock()
    mock_request.cookies = {"refresh_token": "valid.jwt.token"}

    # Act
    response = logout_controller.handle(mock_request)

    # Assert: Service Interaction
    mock_token_service.logout.assert_called_once_with("valid.jwt.token")

    # Assert: Response Status
    assert response.status_code == 200
    assert response.body["message"] == "Logged out successfully"

    # Assert: Cookies are cleared
    # We look for Max-Age=0 in the Set-Cookie headers
    cookie_header = response.headers.get("Set-Cookie", "")
    assert "access_token=;" in cookie_header
    assert "refresh_token=;" in cookie_header
    assert "Max-Age=0" in cookie_header


def test_logout_idempotent_if_no_cookie(logout_controller, mock_token_service):
    """
    Scenario: User is already logged out (no cookies sent).
    Expected: 200 OK (Don't crash, just ensure cookies are cleared).
    """
    # Arrange: No cookies
    mock_request = Mock()
    mock_request.cookies = {}

    # Act
    response = logout_controller.handle(mock_request)

    # Assert
    # Service should NOT be called (nothing to blacklist)
    mock_token_service.logout.assert_not_called()

    # But we still send the "Clear Cookies" header just to be safe
    assert response.status_code == 200
    assert "Max-Age=0" in response.headers["Set-Cookie"]
