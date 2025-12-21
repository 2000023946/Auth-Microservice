from abc import ABC, abstractmethod
from ...app.domain.user import User


class UserRepoBase(ABC):
    @abstractmethod
    def save(self, user: User):
        pass

    @abstractmethod
    def validate_credentials(self, email: str, password: str):
        pass

    @abstractmethod
    def get_user_by_id(self, id: str):
        pass
