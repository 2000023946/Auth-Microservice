from abc import ABC, abstractmethod


class Cache(ABC):
    @abstractmethod
    def blacklist_token(self, jti: str, expiry_seconds: int) -> None:
        pass

    def is_blacklisted(self, jti: str) -> bool:
        pass
