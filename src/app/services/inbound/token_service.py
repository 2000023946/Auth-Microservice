from typing import Tuple
from abc import ABC, abstractmethod


class ITokenService(ABC):
    @abstractmethod
    def create_jwt(self, user_id: int) -> Tuple[str, str]:
        raise NotImplementedError()

    @abstractmethod
    def refresh_token(self, token: str) -> Tuple[str, str]:
        raise NotImplementedError()

    @abstractmethod
    def logout(self, token: str) -> None:
        raise NotImplementedError()
