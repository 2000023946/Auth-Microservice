import pytest
from unittest.mock import Mock
from src.controller.inbound.register_controller import RegisterController
from src.app.domain.exceptions import UserDomainValidationError, EmailAlreadyExistsError

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_user_service():
    """Mock of the IUserService interface."""
    return Mock()


@pytest.fixture
def controller(mock_user_service):
    """RegisterController with the mocked service injected."""
    return RegisterController(mock_user_service)


# ----------------------------------------------------------------
# Happy Path
# ----------------------------------------------------------------


def test_handle_register_success(controller, mock_user_service):
    """
    Scenario: Valid email and matching passwords.
    Expected: 201 Created and user profile returned (no password).
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {
        "email": "buzz@gatech.edu",
        "pass1": "PerfectPassword123!",
        "pass2": "PerfectPassword123!",
    }

    # Mocking the DTO returned by the service
    mock_dto = Mock()
    mock_dto.user_id = "uuid-1234"
    mock_dto.email = "buzz@gatech.edu"
    mock_user_service.register.return_value = mock_dto

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 201
    assert response.body["id"] == "uuid-1234"
    assert response.body["email"] == "buzz@gatech.edu"
    # Ensure UserResponse is working (Pydantic model_dump)
    assert "password" not in response.body


# ----------------------------------------------------------------
# Pydantic Schema Validation (ValueError)
# ----------------------------------------------------------------


def test_handle_register_pydantic_mismatch(controller):
    """
    Scenario: Passwords do not match in the JSON payload.
    Expected: 400 Bad Request (Caught via ValueError from model_validator).
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {
        "email": "student@gatech.edu",
        "pass1": "PassA",
        "pass2": "PassB",
    }

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 400
    assert "Passwords do not match" in str(response.body["error"])


def test_handle_register_invalid_email(controller):
    """
    Scenario: Email format is invalid.
    Expected: 400 Bad Request (Caught via Pydantic EmailStr).
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {
        "email": "not-an-email",
        "pass1": "Pass123!",
        "pass2": "Pass123!",
    }

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 400
    # Pydantic's EmailStr error message contains "value is not a valid email address"
    assert "email" in str(response.body["error"])


# ----------------------------------------------------------------
# Domain & Infrastructure Errors
# ----------------------------------------------------------------


def test_handle_register_email_exists(controller, mock_user_service):
    """
    Scenario: Email is valid but already taken in the DB.
    Expected: 409 Conflict.
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {
        "email": "existing@gatech.edu",
        "pass1": "Pass123!",
        "pass2": "Pass123!",
    }
    mock_user_service.register.side_effect = EmailAlreadyExistsError("Email taken")

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 409
    assert "Email taken" in response.body["error"]


def test_handle_register_weak_password(controller, mock_user_service):
    """
    Scenario: Passwords match, but Domain Service rejects it (e.g., too short).
    Expected: 400 Bad Request.
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {"email": "user@gatech.edu", "pass1": "123", "pass2": "123"}
    mock_user_service.register.side_effect = UserDomainValidationError(
        "Password too weak"
    )

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 400
    assert "too weak" in response.body["error"]


def test_handle_register_unexpected_crash(controller, mock_user_service):
    """
    Scenario: Unexpected code crash or DB connection failure.
    Expected: 500 Internal Server Error.
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {
        "email": "user@gatech.edu",
        "pass1": "Pass123!",
        "pass2": "Pass123!",
    }
    mock_user_service.register.side_effect = Exception("Sudden DB death")

    # Act
    response = controller.handle(mock_request)

    # Assert
    assert response.status_code == 500
    assert response.body["error"] == "Internal Server Error"
