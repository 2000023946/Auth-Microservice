from dataclasses import dataclass


@dataclass
class UserDTO:
    email: str
    id: int = None

    @classmethod
    def from_domain(cls, user):
        # Maps the Rich Domain User -> Safe DTO
        return cls(id=getattr(user, "id", None), email=user.email)
