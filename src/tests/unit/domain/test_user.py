import pytest  # type: ignore
from src.app.domain.user import User
from src.app.domain.exceptions import UserDomainValidationError

# ----------------------------------------------------------------
# 1. Happy Path Tests (Success Scenarios)
# ----------------------------------------------------------------


def test_create_user_success():
    """
    Scenario: Valid email and matching strong passwords.
    Expected: User object is created, password is hashed (not plain text).
    """
    email = "test@gatech.edu"
    pass1 = "SecurePass123!"
    pass2 = "SecurePass123!"

    # Act
    user = User.create(email, pass1, pass2)

    # Assert
    assert user.email == email
    assert user.password_hash != pass1  # Must be hashed
    assert user.password_hash.startswith("$2b$")  # Bcrypt standard prefix

    assert user.id is None


def test_verify_password_check():
    """
    Scenario: User is created. We check if the password verification works.
    Expected: Returns True for correct password, False for wrong one.
    """
    user = User.create("test@gatech.edu", "Secret123!", "Secret123!")

    # Act & Assert
    assert user.verify_password("Secret123!") is True
    assert user.verify_password("WrongPass!") is False


# ----------------------------------------------------------------
# 2. Validation Failure Tests (Error Scenarios)
# ----------------------------------------------------------------


def test_create_user_mismatch_passwords():
    """
    Scenario: Passwords do not match.
    Expected: Raises UserDomainValidationError.
    """
    with pytest.raises(UserDomainValidationError) as exc:
        User.create("test@gatech.edu", "PassA", "PassB")

    assert "Passwords do not match" in str(exc.value)


def test_create_user_invalid_email_format():
    """
    Scenario: Email is missing '@' or domain.
    Expected: Raises UserDomainValidationError.
    """
    invalid_emails = ["plainaddress", "@missingusername.com", "username@.com.my"]

    for email in invalid_emails:
        with pytest.raises(UserDomainValidationError) as exc:
            User.create(email, "Pass123!", "Pass123!")
        assert "Invalid email format" in str(exc.value)


def test_create_user_weak_password():
    """
    Scenario: Password is too short or simple (based on your rules).
    Expected: Raises UserDomainValidationError.
    """
    weak_passwords = ["123", "short", "onlyletters"]

    for pwd in weak_passwords:
        with pytest.raises(UserDomainValidationError) as exc:
            User.create("test@gatech.edu", pwd, pwd)
        assert "Password is too weak" in str(exc.value)
