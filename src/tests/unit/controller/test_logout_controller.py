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
    1. TokenService.logout() is called.
    2. Response is 200 OK.
    3. Both tokens are cleared via Set-Cookie Max-Age=0.
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {"refresh_token": "valid.jwt.token"}

    # Act
    response = logout_controller.handle(mock_request)

    # Assert: Service Interaction
    mock_token_service.logout.assert_called_once_with("valid.jwt.token")
    print(response)
    # Assert: Status
    assert response.status_code == 200

    # Assert: Headers (List of Tuples Assertion)
    # We extract all 'Set-Cookie' values into a single string for easy checking
    set_cookie_headers = [val for key, val in response.headers if key == "Set-Cookie"]
    all_cookies_str = " | ".join(set_cookie_headers)

    # Check for deletion flags
    assert "access_token=" in all_cookies_str
    assert "refresh_token=" in all_cookies_str
    assert "Max-Age=0" in all_cookies_str
    assert "HttpOnly" in all_cookies_str


def test_logout_idempotent_if_no_cookie(logout_controller, mock_token_service):
    """
    Scenario: User hits logout but has no cookies.
    Expected: Still returns 200 and sends clear-cookie headers (Safe Logout).
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {}

    # Act
    response = logout_controller.handle(mock_request)

    # Assert
    mock_token_service.logout.assert_not_called()
    assert response.status_code == 200

    # We still want to clear cookies just in case the client has "ghost" cookies
    set_cookie_headers = [val for key, val in response.headers if key == "Set-Cookie"]
    assert any("Max-Age=0" in s for s in set_cookie_headers)
