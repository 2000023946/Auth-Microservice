class UserDomainValidationError(Exception):
    """Class used when the domain buisness rules are validated"""

    pass


class EmailAlreadyExistsError(Exception):
    """Class used when the email already exists exception"""

    pass


class AuthenticationError(Exception):
    """error called when the auth is invalid"""

    pass


class TokenError(Exception):
    """Raised when JWT validation fails (expired, blacklisted, invalid)."""

    pass
