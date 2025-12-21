from dataclasses import dataclass
from ...app.domain.user import User


@dataclass
class UserDTO:
    email: str
    user_id: str

    @classmethod
    def from_domain(cls, user: User) -> "UserDTO":
        # Maps the Rich Domain User -> Safe DTO
        # removed the password field
        return cls(user_id=user.get_user_id, email=user.email)

    @classmethod
    def create(cls, email: str, user_id: str):
        return UserDTO(email, user_id)
