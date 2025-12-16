from src.app.domain.user import User
from src.app.domain.exceptions import AuthenticationError
from src.controller.outbound.user_dto import UserDTO


# ----------------------------------------------------------------
# User Service Implementation
# ----------------------------------------------------------------
class UserService:
    def __init__(self, user_repo):
        self.user_repo = user_repo

    def register(self, email: str, p1: str, p2: str) -> UserDTO:
        """
        Orchestrates the Sign-Up Flow:
        1. Create Domain Object (Validates & Hashes)
        2. Save to DB via Repo
        3. Return DTO
        """
        # 1. Domain Logic: Validates input and creates the rich object
        # Note: If passwords mismatch or email is invalid, User.create raises UserDomainValidationError
        new_user = User.create(email, p1, p2)

        # 2. Persistence: Save to the database
        # Note: If email exists, repo.save raises EmailAlreadyExistsError
        self.user_repo.save(new_user)

        # 3. Response: Convert to DTO
        return UserDTO.from_domain(new_user)

    def login(self, email: str, password: str) -> UserDTO:
        """
        Orchestrates the Login Flow:
        1. Fetch User from Repo
        2. Verify Password
        3. Return DTO
        """
        # 1. Fetch User (Repo returns a User Domain object or None)
        user = self.user_repo.validate_credentials(email)

        # 2. Validation
        if user is None:
            # Security Best Practice: Generic error message to prevent enumeration
            raise AuthenticationError("Invalid email or password")

        if not user.verify_password(password):
            raise AuthenticationError("Invalid email or password")

        # 3. Response
        return UserDTO.from_domain(user)

    def fetchUser(self, id):
        if not id:
            raise AuthenticationError("Invalid user id")
        user = self.user_repo.get_user_by_id(id)

        if not user:
            raise AuthenticationError("Invalid user id")

        return UserDTO.from_domain(user)
