from abc import ABC, abstractmethod
from ...app.domain.user import User


class UserRepoBase(ABC):
    @abstractmethod
    def save(self, user: User):
        pass

    @abstractmethod
    def validate_user(self, user: User):
        pass
