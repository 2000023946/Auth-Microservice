from abc import ABC, abstractmethod


class UserDBExecuter(ABC):
    @abstractmethod
    def create_user(self, email: str, password_hash: str):
        pass

    @abstractmethod
    def login_user(self, email: str, password: str):
        pass

    @abstractmethod
    def get_user_by_id(self, id: str):
        pass
