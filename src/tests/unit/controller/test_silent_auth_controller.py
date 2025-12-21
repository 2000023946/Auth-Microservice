import pytest
from unittest.mock import Mock, MagicMock
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
def controller(mock_token_service, mock_user_service):
    return SilentAuthController(mock_token_service, mock_user_service)


# ----------------------------------------------------------------
# Tests
# ----------------------------------------------------------------


def test_handle_success_with_valid_access_token(
    controller, mock_token_service, mock_user_service
):
    """
    Scenario: User provides a valid access token.
    Expectation: Return user profile, no new cookies set.
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {"access_token": "valid_jwt"}

    mock_token_service.validate_and_get_user_id.return_value = "user-uuid-123"

    user_mock = Mock()
    user_mock.email = "student@gt.edu"
    mock_user_service.fetchUser.return_value = user_mock

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 200
    assert response.body["email"] == "student@gt.edu"
    assert response.body["isAuthenticated"] is True
    # Verify no cookies were set because access token was valid
    assert len(response.headers) == 0


def test_handle_success_with_refresh_rotation(
    controller, mock_token_service, mock_user_service
):
    """
    Scenario: Access token is expired/invalid, but Refresh token is valid.
    Expectation: New tokens issued via cookies, user profile returned.
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {
        "access_token": "expired_jwt",
        "refresh_token": "valid_refresh_jwt",
    }

    # Access fails
    mock_token_service.validate_and_get_user_id.side_effect = TokenError("Expired")
    # Refresh succeeds
    mock_token_service.refresh_token.return_value = ("new_access", "new_refresh")
    mock_token_service.get_user_id_from_token.return_value = "user-uuid-456"

    user_mock = Mock()
    user_mock.email = "refresh_user@gt.edu"
    mock_user_service.fetchUser.return_value = user_mock

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 200
    assert response.body["email"] == "refresh_user@gt.edu"

    # Check that rotation cookies are present in the list of tuples
    cookie_headers = [val for key, val in response.headers if key == "Set-Cookie"]
    assert len(cookie_headers) == 2
    assert "access_token=new_access" in cookie_headers[0]
    assert "refresh_token=new_refresh" in cookie_headers[1]


def test_handle_failure_all_tokens_invalid(controller, mock_token_service):
    """
    Scenario: Access token invalid and Refresh token invalid.
    Expectation: 401 Unauthorized.
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {"access_token": "bad", "refresh_token": "worse"}

    mock_token_service.validate_and_get_user_id.side_effect = TokenError()
    mock_token_service.refresh_token.side_effect = TokenError()

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 401
    assert response.body["isAuthenticated"] is False


def test_handle_failure_user_service_error(
    controller, mock_token_service, mock_user_service
):
    """
    Scenario: Tokens are valid, but database lookup for the user fails.
    Expectation: 401 Unauthorized (via catch-all in _build_success_response).
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {"access_token": "valid_jwt"}
    mock_token_service.validate_and_get_user_id.return_value = "123"

    # Database or logic error
    mock_user_service.fetchUser.side_effect = Exception("DB Down")

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 401
    assert response.body["isAuthenticated"] is False
