from ..app.domain.user import User
from ..repository.dbEntity.user import UserDB
from ..app.services.hashing_service import Hasher


class UserMapper:
    @classmethod
    def domain_to_db(cls, user: User, hasher: Hasher) -> UserDB:
        """
        Maps Domain -> Data Class
        """
        return UserDB(
            id=user.get_user_id,
            email=user.email,
            password_hash=hasher.hash_password(user.password),
        )

    @classmethod
    def row_to_db(cls, row: tuple) -> UserDB | None:
        """
        Maps Row Tuple -> UserDB
        """
        if not row:
            return None

        # Unpack securely
        user_id, email, password_hash, createdAt = row

        # Use your factory method (User.retrieve) to bypass validation
        print("mapping the user", user_id, email, password_hash)
        return UserDB(
            id=user_id, email=email, password_hash=password_hash, created_at=createdAt
        )

    @classmethod
    def db_to_domain(cls, user_entity: UserDB) -> User:
        return User(
            email=user_entity.email,
            password="",  # We leave this empty because we don't know the plain text!
            user_id=user_entity.id,
            createdAt=user_entity.created_at,
        )
