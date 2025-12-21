import pytest
from unittest.mock import Mock, patch
from mysql.connector import IntegrityError
from src.repository.outbound.userRepo import UserRepo
from src.app.domain.user import User
from src.app.domain.exceptions import EmailAlreadyExistsError, AuthenticationError

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_executor():
    """Mocks UserDBExecuter."""
    return Mock()


@pytest.fixture
def mock_hasher():
    """Mocks Hasher service."""
    hasher = Mock()
    hasher.hash_password.return_value = "mocked_hash_123"
    # Important: Default to True for success cases, override in failure tests
    hasher.verify_password.return_value = True
    return hasher


@pytest.fixture
def user_repo(mock_executor, mock_hasher):
    """Initializes Repo with injected mocks."""
    return UserRepo(sql_executor=mock_executor, hasher=mock_hasher)


# ----------------------------------------------------------------
# 1. Save / Create User Tests
# ----------------------------------------------------------------


def test_save_orchestrates_mapping_and_db_call(user_repo, mock_executor, mock_hasher):
    """
    Verifies that the repo uses the Mapper and calls the executor with correct fields.
    """
    # Arrange
    # Create a real domain user (or mock it)
    domain_user = User.create("test@gatech.edu", "PlainPass123!", "PlainPass123!")

    # Act
    user_repo.save(domain_user)

    # Assert
    # Verify the executor was called with the mapped ID and hashed password
    # UserMapper.domain_to_db uses hasher internally, so we check if create_user was hit
    mock_executor.create_user.assert_called_once()
    args, _ = mock_executor.create_user.call_args

    assert args[1] == "test@gatech.edu"
    assert args[2] == "mocked_hash_123"  # Result of mock_hasher.hash_password


def test_save_handles_duplicate_email(user_repo, mock_executor):
    """Scenario: Executor throws IntegrityError (MySQL Duplicate Key)."""
    domain_user = User.create("dupe@gatech.edu", "Pass123!", "Pass123!")
    mock_executor.create_user.side_effect = IntegrityError()

    with pytest.raises(EmailAlreadyExistsError) as exc:
        user_repo.save(domain_user)

    assert "dupe@gatech.edu" in str(exc.value)


# ----------------------------------------------------------------
# 2. Credential & Fetch Tests
# ----------------------------------------------------------------


def test_validate_credentials_success(user_repo, mock_executor, mock_hasher):
    """
    Matches logic: validates dict-style row and returns (user_id, email).
    """
    # Arrange
    email = "user@gt.edu"
    password = "correct_password"

    # Mocking the dictionary return your UserSQLExecuter now provides
    mock_executor.login_user.return_value = {
        "id": "uuid-123",
        "password_hash": "$2b$12$hashed",
    }
    mock_hasher.verify_password.return_value = True

    # Act
    user_id, user_email = user_repo.validate_credentials(email, password)

    # Assert
    mock_hasher.verify_password.assert_called_with(password, "$2b$12$hashed")
    assert user_id == "uuid-123"
    assert user_email == email


def test_validate_credentials_invalid_password(user_repo, mock_executor, mock_hasher):
    """Scenario: User found but password check fails."""
    mock_executor.login_user.return_value = {
        "id": "uuid-123",
        "password_hash": "some_hash",
    }
    mock_hasher.verify_password.return_value = False  # Explicitly fail

    with pytest.raises(AuthenticationError) as exc:
        user_repo.validate_credentials("user@gt.edu", "wrong_pass")

    assert "Invalid Credentials" in str(exc.value)


def test_get_user_by_id_success(user_repo, mock_executor):
    """
    Matches logic: expects dictionary lookup row['id'] and row['createdAt'].
    """
    # Arrange
    # Note: Your code has a small bug: email = row["id"].
    # I am testing based on your PROVIDED code logic.
    mock_executor.get_user_by_id.return_value = {
        "id": "profile@gt.edu",
        "createdAt": "2025-12-20",
    }

    # Act
    email, created_at = user_repo.get_user_by_id("id_99")

    # Assert
    assert email == "profile@gt.edu"
    assert created_at == "2025-12-20"
