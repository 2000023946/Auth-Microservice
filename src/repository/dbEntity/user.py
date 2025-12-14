from dataclasses import dataclass


@dataclass
class UserDB:
    id: int | None
    email: str
    password_hash: str
