from mysql.connector import IntegrityError
from src.app.domain.user import User
from src.app.domain.exceptions import EmailAlreadyExistsError, AuthenticationError
from src.mapper.user_mapper import UserMapper
from ..dbEntity.user import UserDB
from ..inbound.dbExecuter import UserDBExecuter
from ...app.services.inbound.hashing_service import Hasher
from ..inbound.userRepo import UserRepoBase


class UserRepo(UserRepoBase):
    """
    Acts as orchestrator of the DB logic. The DB makes sures it stores safe values of the user
    The repository is then incharge of converting all fields in the user object to safe way
    """

    def __init__(self, sql_executor: UserDBExecuter, hasher: Hasher):
        self.sql_executor = sql_executor
        self.hasher = hasher

    def save(self, user: User) -> UserDB:
        try:
            # convert the user to userDB since the db entity
            if not user:
                raise Exception(f"User cannot be null")  # raise error
            user_entity = UserMapper.domain_to_db(user, self.hasher)
            self.sql_executor.create_user(
                user_entity.id, user_entity.email, user_entity.password_hash
            )

        except IntegrityError:
            raise EmailAlreadyExistsError(f"User {user.email} already exists.")

    def validate_credentials(self, email: str, password: str) -> tuple[str, str]:
        # 1. Execute SQL
        print("stargin the validiton with the sql")
        row = self.sql_executor.login_user(email)

        if not row:
            raise AuthenticationError("invalid credentials")
        print("row returned from the db", row)
        # get the id and password hash from the row
        user_id, password_hash_db = row.get("id"), row.get("password_hash")

        is_valid = self.hasher.verify_password(password, password_hash_db)

        if not is_valid:
            raise AuthenticationError("Invalid Credentials")

        # 2. DB Row -> Domain Object
        return (user_id, email)

    def get_user_by_id(self, id: str):
        row = self.sql_executor.get_user_by_id(id)

        email, createdAt = row["id"], row["createdAt"]

        return (email, createdAt)
