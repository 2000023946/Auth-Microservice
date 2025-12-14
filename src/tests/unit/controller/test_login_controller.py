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


# ----------------------------------------------------------------
# Happy Path
# ----------------------------------------------------------------


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
    # User Service returns a DTO (Data Transfer Object)
    mock_user_service.login.return_value = Mock(id=1, email="test@gt.edu")
    # Token Service returns the pair of strings
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

    # Assert: Cookies (The most important part)
    # We check if the 'Set-Cookie' header was constructed correctly
    cookies = response.headers.get("Set-Cookie", "")

    # Check Access Token Cookie
    assert "access_token=fake_access_token" in cookies
    assert "HttpOnly" in cookies
    assert "Secure" in cookies
    assert "Max-Age=900" in cookies  # 15 mins

    # Check Refresh Token Cookie
    assert "refresh_token=fake_refresh_token" in cookies
    assert "Path=/auth/refresh" in cookies  # Security restriction


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
