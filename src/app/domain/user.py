import re
from src.app.domain.exceptions import UserDomainValidationError
from uuid import uuid4, UUID
from dataclasses import dataclass, field
from datetime import datetime, date


@dataclass(frozen=True)
class User:
    """
    Pure Domain User Model. It only contains the logic based on how each attribute should be defined.
    And it contains the logic of how the attributes should behave with each other. It contains all the
    validation needed for the use cases for the login and register.
    """

    email: str
    password: str
    user_id: UUID = field(default_factory=uuid4)
    createdAt: datetime = field(default_factory=date.today)

    @classmethod
    def create(cls, email: str, p1: str, p2: str):
        """Factory method to validate input and create a User instance."""
        # 1. Check Matching Passwords
        if email is None or p1 is None or p2 is None:
            raise UserDomainValidationError("Domain inputs cannot be null")

        if p1 != p2:
            raise UserDomainValidationError("Passwords do not match")

        cls.validate_credentials(email, p1)

        # 4. Return new Instance
        return cls(email=email, password=p1)

    @classmethod
    def validate_credentials(
        cls,
        email: str,
        password: str,
    ) -> bool:
        """Checks if the stored password matches the current password"""

        cls.validate_email(email)
        cls.validate_password(password)

        return True

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

    @property
    def get_user_id(self) -> str:
        return str(self.user_id)

    @property
    def get_created_date(self) -> str:
        return str(self.createdAt)
