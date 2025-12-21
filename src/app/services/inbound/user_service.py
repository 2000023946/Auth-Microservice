from abc import ABC, abstractmethod
from src.controller.outbound.user_dto import UserDTO


class IUserService(ABC):
    def register(self, email: str, password: str) -> UserDTO:
        raise NotImplementedError()

    def login(self, email: str, password: str) -> bool:
        raise NotImplementedError()

    def fetchUser(self, id: str) -> UserDTO:
        raise NotImplementedError()
