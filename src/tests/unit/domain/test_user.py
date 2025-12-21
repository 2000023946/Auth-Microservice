import pytest
from uuid import UUID
from datetime import date
from src.app.domain.user import User
from src.app.domain.exceptions import UserDomainValidationError

# ----------------------------------------------------------------
# 1. Happy Path Tests (Success Scenarios)
# ----------------------------------------------------------------


def test_create_user_success():
    """
    Scenario: Valid email and matching strong passwords.
    Expected: User object is created with plain text password (hashing happens in Mapper).
    """
    email = "mohamed@gatech.edu"
    pass1 = "SecurePass123!"

    # Act
    user = User.create(email, pass1, pass1)

    # Assert
    assert user.email == email
    assert user.password == pass1  # In Pure Domain, we keep the raw string
    assert isinstance(user.user_id, UUID)
    assert user.createdAt == date.today()


def test_user_is_immutable():
    """
    Scenario: Attempting to change an attribute on a frozen dataclass.
    Expected: Raises FrozenInstanceError.
    """
    user = User.create("test@gatech.edu", "Password123!", "Password123!")

    with pytest.raises(
        AttributeError
    ):  # dataclasses.FrozenInstanceError inherits from AttributeError
        user.email = "new@gatech.edu"


# ----------------------------------------------------------------
# 2. Validation Failure Tests (Error Scenarios)
# ----------------------------------------------------------------


def test_create_user_mismatch_passwords():
    """
    Scenario: Passwords do not match.
    Expected: Raises UserDomainValidationError.
    """
    with pytest.raises(UserDomainValidationError) as exc:
        User.create("test@gatech.edu", "Password123!", "Different123!")

    assert "Passwords do not match" in str(exc.value)


def test_create_user_null_inputs():
    """
    Scenario: Passing None for required fields.
    Expected: Raises UserDomainValidationError.
    """
    with pytest.raises(UserDomainValidationError) as exc:
        User.create(None, "Pass123!", "Pass123!")

    assert "Domain inputs cannot be null" in str(exc.value)


@pytest.mark.parametrize(
    "invalid_email", ["plainaddress", "@missingusername.com", "username@.com.my", ""]
)
def test_create_user_invalid_email_format(invalid_email):
    """
    Scenario: Various invalid email formats.
    """
    with pytest.raises(UserDomainValidationError) as exc:
        User.create(invalid_email, "Pass123!", "Pass123!")

    assert "Invalid email format" in str(exc.value) or "Email cannot be null" in str(
        exc.value
    )


@pytest.mark.parametrize(
    "weak_pwd", ["1234567", "onlyletters", ""]  # Too short  # No numbers  # Empty
)
def test_create_user_weak_password(weak_pwd):
    """
    Scenario: Password fails complexity rules.
    """
    with pytest.raises(UserDomainValidationError) as exc:
        User.create("test@gatech.edu", weak_pwd, weak_pwd)

    assert "Password is too weak" in str(
        exc.value
    ) or "Password cannot be empty" in str(exc.value)


# ----------------------------------------------------------------
# 3. Property & Method Tests
# ----------------------------------------------------------------


def test_user_id_string_property():
    """
    Check if the helper property returns the string version of UUID.
    """
    user = User.create("test@gatech.edu", "Pass123!", "Pass123!")
    assert isinstance(user.get_user_id, str)
    assert len(user.get_user_id) == 36  # Standard UUID length
