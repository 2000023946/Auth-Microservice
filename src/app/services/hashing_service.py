from .inbound.hashing_service import Hasher
import bcrypt


import bcrypt


class BcryptHasher(Hasher):
    def hash_password(self, password: str) -> str:
        # returns bytes, but usually we store as string in DB
        salt = bcrypt.gensalt()
        hashed = bcrypt.hashpw(password.encode("utf-8"), salt)
        return hashed.decode("utf-8")

    def verify_password(self, password: str, password_hash: str) -> bool:
        """
        bcrypt.checkpw expects:
        1. Plaintext password (encoded to bytes)
        2. Stored Hash (encoded to bytes)
        """
        print("checking the password", password, password_hash)
        try:
            return bcrypt.checkpw(
                password.encode("utf-8"), password_hash.encode("utf-8")
            )
        except Exception as e:
            print(f"Hashing verification error: {e}")
            return False
