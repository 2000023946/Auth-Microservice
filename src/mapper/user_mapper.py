from ..app.domain.user import User
from ..repository.dbEntity.user import UserDB


class UserMapper:
    @classmethod
    def to_db(cls, user: User) -> UserDB:
        """
        Maps Domain -> Data Class
        """
        return UserDB(
            id=getattr(user, "id", None),  # Safe access in case ID is missing
            email=user.email,
            password_hash=user.password_hash,
        )

    @classmethod
    def to_domain(cls, row: tuple) -> User | None:
        """
        Maps Row Tuple -> Domain
        """
        if not row:
            return None

        # Unpack securely
        user_id, email, password_hash = row

        # Use your factory method (User.retrieve) to bypass validation
        print("mapping the user", user_id, email, password_hash)
        return User.retrieve(email, password_hash, user_id)
