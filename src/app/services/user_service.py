from src.app.domain.user import User
from src.app.domain.exceptions import AuthenticationError
from src.controller.outbound.user_dto import UserDTO
from ...repository.inbound.userRepo import UserRepoBase
from .inbound.user_service import IUserService


# ----------------------------------------------------------------
# User Service Implementation
# ----------------------------------------------------------------
class UserService(IUserService):
    def __init__(self, user_repo: UserRepoBase):
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
        print("user domain validated")
        # creates the hashed password
        # 2. Persistence: Save to the database
        # Note: If email exists, repo.save raises EmailAlreadyExistsError
        self.user_repo.save(new_user)
        print("saved in the repo")
        # 3. Response: Convert to DTO
        return UserDTO.from_domain(new_user)

    def login(self, email: str, password: str) -> UserDTO:
        """
        Orchestrates the Login Flow:
        1. Fetch User from Repo
        2. Verify Password
        3. Return DTO
        """
        print("logging as teh user")
        # 0. validate the inputs
        domain_validation = User.validate_credentials(email, password)
        print("domain validtion good")
        if not domain_validation:
            raise AuthenticationError("Invalid credentials")
        print("domain validtion")
        # 1. Validate credentials based on repo (Repo returns a User Domain object or None)
        user_id, email = self.user_repo.validate_credentials(email, password)
        print("db validtion good")
        if not user_id or not email:
            raise AuthenticationError("Invalid Credentials")
        print("db valition okay")
        # 3. Return True User is logged in
        return UserDTO.create(email, user_id)

    def fetchUser(self, user_id: str) -> UserDTO:
        if not user_id:
            raise AuthenticationError("Invalid user id")
        email, createdAt = self.user_repo.get_user_by_id(user_id)

        if not email:
            raise AuthenticationError("Invalid user id")

        return UserDTO.create(email, user_id)
