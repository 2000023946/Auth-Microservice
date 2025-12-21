# domain/services.py
class Hasher:
    """An interface/abstract class defined in the Domain"""

    def hash_password(self, password: str) -> str:
        raise NotImplementedError()

    def verify_password(self, password_hash: str) -> bool:
        raise NotImplementedError()
