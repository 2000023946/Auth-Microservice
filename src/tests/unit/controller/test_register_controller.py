import pytest  # type: ignore
from unittest.mock import Mock
from src.controller.inbound.register_controller import RegisterController
from src.app.domain.exceptions import UserDomainValidationError, EmailAlreadyExistsError

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_user_service():
    return Mock()


@pytest.fixture
def register_controller(mock_user_service):
    return RegisterController(mock_user_service)


# ----------------------------------------------------------------
# Happy Path
# ----------------------------------------------------------------


def test_register_success(register_controller, mock_user_service):
    """
    Scenario: Valid input, passwords match.
    Expected: 201 Created, returns User DTO (no password).
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {
        "email": "fresh@gt.edu",
        "pass1": "SecurePass1!",
        "pass2": "SecurePass1!",
    }

    # Mock Service returning a DTO
    mock_user_service.register.return_value = Mock(id=50, email="fresh@gt.edu")

    # Act
    response = register_controller.handle(mock_request)

    # Assert
    assert response.status_code == 201
    assert response.body["email"] == "fresh@gt.edu"
    assert response.body["id"] == 50
    assert "pass" not in response.body  # No passwords in response!


# ----------------------------------------------------------------
# Validation Errors (Schema & Logic)
# ----------------------------------------------------------------


def test_register_fails_password_mismatch(register_controller):
    """
    Scenario: pass1 != pass2.
    Expected: 400 Bad Request (Caught by Pydantic Schema).
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {"email": "mismatch@gt.edu", "pass1": "PassA", "pass2": "PassB"}

    # Act
    response = register_controller.handle(mock_request)

    # Assert
    assert response.status_code == 400
    assert "match" in str(response.body["error"])


def test_register_fails_email_taken(register_controller, mock_user_service):
    """
    Scenario: Email already exists in DB.
    Expected: 409 Conflict.
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {"email": "taken@gt.edu", "pass1": "Pass1!", "pass2": "Pass1!"}

    # Mock Service throwing the Domain Exception
    mock_user_service.register.side_effect = EmailAlreadyExistsError("Email taken")

    # Act
    response = register_controller.handle(mock_request)

    # Assert
    assert response.status_code == 409
    assert "taken" in str(response.body["error"])


def test_register_fails_weak_password(register_controller, mock_user_service):
    """
    Scenario: Password violates Domain rules (too short).
    Expected: 400 Bad Request (Bubbled up from Domain Layer).
    """
    # Arrange
    mock_request = Mock()
    mock_request.json = {"email": "weak@gt.edu", "pass1": "123", "pass2": "123"}

    mock_user_service.register.side_effect = UserDomainValidationError(
        "Password too weak"
    )

    # Act
    response = register_controller.handle(mock_request)

    # Assert
    assert response.status_code == 400
    assert "too weak" in str(response.body["error"])
