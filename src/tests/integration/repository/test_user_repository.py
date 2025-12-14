import pytest
from unittest.mock import Mock
from src.repository.outbound.userRepo import UserRepo
from src.app.domain.user import User
from src.app.domain.exceptions import EmailAlreadyExistsError
from mysql.connector import IntegrityError  # type: ignore

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_executor():
    """
    We mock the SqlExecutor.
    The Repo relies on this, so we control what it returns.
    """
    return Mock()


@pytest.fixture
def user_repo(mock_executor):
    """
    Dependency Injection: Pass the mock executor into the Repo.
    """
    return UserRepo(mock_executor)


# ----------------------------------------------------------------
# Save Tests
# ----------------------------------------------------------------


def test_save_calls_executor_correctly(user_repo, mock_executor):
    """
    Scenario: Saving a user.
    Expected: Repo orchestrates the call to 'create_user' procedure.
    """
    # Arrange
    user = User.create("test@gt.edu", "SecurePass1!", "SecurePass1!")

    # Simulate DB returning a new ID (e.g., 99)
    mock_executor.execute_write.return_value = 99

    # Act
    user_repo.save(user)

    # Assert
    # 1. Did we call the correct procedure name? (Matches your code: 'create_user')
    # 2. Did we extract the tuple correctly?
    mock_executor.execute_write.assert_called_with(
        "create_user", ("test@gt.edu", user.password_hash)
    )

    # 3. Did the Domain Object get the new ID?
    assert user.id == 99


def test_save_handles_integrity_error(user_repo, mock_executor):
    """
    Scenario: Duplicate email.
    Expected: Repo catches MySQL error -> Raises Domain Error.
    """
    # Arrange
    user = User.create("dupe@gt.edu", "SecurePass1!", "SecurePass1!")

    # Simulate the Executor crashing with a DB error
    mock_executor.execute_write.side_effect = IntegrityError("Duplicate entry")

    # Act & Assert
    with pytest.raises(EmailAlreadyExistsError):
        user_repo.save(user)


# ----------------------------------------------------------------
# Login / Fetch Tests
# ----------------------------------------------------------------


def test_validate_credentials_maps_row_to_domain(user_repo, mock_executor):
    """
    Scenario: Fetching user by email.
    Expected: Repo calls 'login_user' and returns a User object.
    """
    # Arrange
    # Simulate the Executor returning a raw tuple (id, email, hash)
    mock_executor.execute_read_one.return_value = (50, "real@gt.edu", "hashed_secret")

    # Act
    found_user = user_repo.validate_credentials("real@gt.edu")
    print(found_user, "1. user that is found", found_user.__dict__)
    # Assert
    # 1. Verify call args
    mock_executor.execute_read_one.assert_called_with("login_user", ("real@gt.edu",))

    print(found_user, "2. user that is found", found_user.__dict__)

    # 2. Verify the Mapper worked (Row -> Object)
    assert found_user is not None
    assert found_user.id == 50
    assert found_user.email == "real@gt.edu"
    assert found_user.password_hash == "hashed_secret"


def test_validate_credentials_returns_none_if_missing(user_repo, mock_executor):
    """
    Scenario: User not found.
    Expected: Returns None.
    """
    # Simulate DB returning None
    mock_executor.execute_read_one.return_value = None

    # Act
    found_user = user_repo.validate_credentials("ghost@gt.edu")

    # Assert
    assert found_user is None
