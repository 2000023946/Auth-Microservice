import pytest
from unittest.mock import Mock
from src.controller.inbound.login_controller import LoginController
from src.app.domain.exceptions import AuthenticationError

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_user_service():
    return Mock()


@pytest.fixture
def mock_token_service():
    return Mock()


@pytest.fixture
def login_controller(mock_user_service, mock_token_service):
    return LoginController(mock_user_service, mock_token_service)


def test_login_success_sets_cookies(
    login_controller, mock_user_service, mock_token_service
):
    """
    Scenario: Valid credentials.
    Expected: 200 OK, User Profile in JSON, Tokens in HttpOnly Cookies.
    """
    # Arrange: Mock Request
    mock_request = Mock()
    mock_request.json = {"email": "test@gt.edu", "password": "pass"}

    # Arrange: Mock Service Responses
    mock_user_service.login.return_value = Mock(id=1, email="test@gt.edu")
    mock_token_service.create_jwt.return_value = (
        "fake_access_token",
        "fake_refresh_token",
    )

    # Act
    response = login_controller.handle(mock_request)

    # Assert: Status
    assert response.status_code == 200

    # Assert: Body contains user info but NOT passwords
    assert response.body["email"] == "test@gt.edu"
    assert "password" not in response.body

    # --- UPDATED COOKIE ASSERTION ---
    # Since headers is now a list of tuples [('Set-Cookie', '...'), ...],
    # we cannot use .get(). We must filter the list.

    # 1. Extract all 'Set-Cookie' header values
    cookie_values = [value for key, value in response.headers if key == "Set-Cookie"]

    # 2. Join them into one string to make searching easier
    all_cookies_str = "".join(cookie_values)

    # 3. Verify Access Token
    assert "access_token" in all_cookies_str
    assert "fake_access_token" in all_cookies_str

    # 4. Verify Refresh Token
    assert "refresh_token" in all_cookies_str
    assert "fake_refresh_token" in all_cookies_str

    # 5. Verify Security Flags
    assert "HttpOnly" in all_cookies_str
    assert "SameSite=Lax" in all_cookies_str


# ----------------------------------------------------------------
# Error Handling
# ----------------------------------------------------------------


def test_login_fails_invalid_credentials(login_controller, mock_user_service):
    """
    Scenario: Wrong password.
    Expected: 401 Unauthorized.
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {"email": "test@gt.edu", "password": "wrong"}

    # Service raises error
    mock_user_service.login.side_effect = AuthenticationError("Invalid creds")

    # Act
    response = login_controller.handle(mock_request)

    # Assert
    assert response.status_code == 401
    assert "Invalid creds" in response.body["error"]


def test_login_fails_validation(login_controller):
    """
    Scenario: Missing password in JSON.
    Expected: 400 Bad Request (Schema Validation).
    """
    # Arrange: Invalid JSON (missing password)
    mock_request = Mock()
    mock_request.json = {"email": "test@gt.edu"}

    # Act
    response = login_controller.handle(mock_request)

    # Assert
    assert response.status_code == 400
    assert "Field required" in str(response.body["error"])
