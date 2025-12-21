from typing import Protocol


class ICache(Protocol):
    """
    The Domain/Application Interface.
    Defines WHAT we do with a cache, not HOW it's implemented.
    """

    def blacklist_token(self, jti: str, expiry_seconds: int) -> None: ...
    def is_blacklisted(self, jti: str) -> bool: ...


class ICacheProvider(Protocol):
    """
    The Infrastructure Interface.
    Handles the technical connection to the physical cache.
    """

    def get_cache(self) -> ICache:
        """Returns the specific cache implementation."""
        ...
