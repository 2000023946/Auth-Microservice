from dataclasses import dataclass, field
from datetime import date


@dataclass
class UserDB:
    id: str
    email: str
    password_hash: str
    created_at: date = field(default_factory=date.today)
