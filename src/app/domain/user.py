import re
import bcrypt  # type: ignore
from src.app.domain.exceptions import UserDomainValidationError


class User:
    def __init__(self, email, password_hash, id=None):
        self.email = email
        self.password_hash = password_hash
        self.id = None

    @classmethod
    def create(cls, email, p1, p2):
        """Factory method to validate input and create a User instance."""
        # 1. Check Matching Passwords
        if p1 != p2:
            raise UserDomainValidationError("Passwords do not match")

        # 2. Validate Formats (calls the static methods below)
        cls.validate_email(email)
        cls.validate_password(p1)

        # 3. Hash Password (bcrypt requires bytes, returns bytes, so we decode to string)
        hashed_str = cls.create_hashed_password(p1)

        # 4. Return new Instance
        return cls(email=email, password_hash=hashed_str, id=id)

    @classmethod
    def retrieve(cls, email, pass_hash, id):
        # 2. Validate Formats (calls the static methods below)
        cls.validate_email(email)

        # 3. Hash Password (bcrypt requires bytes, returns bytes, so we decode to string)
        user_retrieved = cls(email=email, password_hash=pass_hash)
        user_retrieved.setId(id)
        return user_retrieved

    @classmethod
    def create_hashed_password(self, password):
        hashed_bytes = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())
        return hashed_bytes.decode("utf-8")

    def verify_password(self, plain_password) -> bool:
        """Checks if the plain password matches the stored hash."""
        # bcrypt.checkpw requires bytes for both arguments
        return bcrypt.checkpw(
            plain_password.encode("utf-8"), self.password_hash.encode("utf-8")
        )

    @staticmethod
    def validate_email(email):
        if not email:
            raise UserDomainValidationError("Email cannot be null")

        # Simple but effective Regex for standard email format
        email_regex = r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$"
        if not re.match(email_regex, email):
            raise UserDomainValidationError("Invalid email format")
        return True

    @staticmethod
    def validate_password(password):
        if not password:
            raise UserDomainValidationError("Password cannot be empty")

        # Example Rules: Min 8 chars, at least 1 number
        if len(password) < 8:
            raise UserDomainValidationError(
                "Password is too weak: Must be at least 8 characters"
            )
        if not re.search(r"\d", password):
            raise UserDomainValidationError(
                "Password is too weak: Must contain a number"
            )
        return True

    def setId(self, id: str):
        print("settings id of ", id)
        if not id:
            raise UserDomainValidationError("Id cannot be null")

        try:
            id = int(id)
        except (TypeError, ValueError):
            raise UserDomainValidationError("Id must be int type!")

        self.id = id
        return self
