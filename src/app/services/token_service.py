import jwt
import uuid
from datetime import datetime, timedelta, timezone
from typing import Tuple
from src.app.domain.exceptions import TokenError


class TokenService:
    def __init__(self, redis_cache, secret_key: str):
        self.redis_cache = redis_cache
        self.secret_key = secret_key
        self.algorithm = "HS256"

        self.ACCESS_EXP_MINUTES = 15
        self.REFRESH_EXP_DAYS = 7

    def create_jwt(self, user_id: int) -> Tuple[str, str]:
        access_token = self._generate_token(user_id, "access")
        refresh_token = self._generate_token(user_id, "refresh")
        return access_token, refresh_token

    def refresh_token(self, token: str) -> Tuple[str, str]:  # <--- CHANGED RETURN TYPE
        """
        Rotates tokens: Blacklists old refresh token, returns NEW pair.
        """
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])

            if payload["type"] != "refresh":
                raise TokenError("Invalid token type")

            jti = payload.get("jti")
            exp = payload.get("exp")
            user_id = payload.get("sub")

            if self.redis_cache.is_blacklisted(jti):
                raise TokenError("Token is blacklisted")

            now = datetime.now(timezone.utc).timestamp()
            ttl = int(exp - now)

            if ttl > 0:
                self.redis_cache.blacklist_token(jti, ttl)

            # --- NEW LOGIC: ROTATION ---
            # We issue a new Refresh Token along with the Access Token
            new_access = self._generate_token(user_id, "access")
            new_refresh = self._generate_token(user_id, "refresh")

            return new_access, new_refresh

        except jwt.ExpiredSignatureError:
            raise TokenError("Token has expired")
        except jwt.InvalidTokenError:
            raise TokenError("Invalid token")

    def logout(self, token: str) -> None:
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])
            jti = payload.get("jti")
            exp = payload.get("exp")

            if jti and exp:
                now = datetime.now(timezone.utc).timestamp()
                ttl = int(exp - now)
                if ttl > 0:
                    self.redis_cache.blacklist_token(jti, ttl)
        except jwt.InvalidTokenError:
            pass

    # --- NEW HELPER METHOD 1 ---
    def validate_and_get_user_id(self, token: str) -> int:
        """
        Strictly validates an Access Token and returns the User ID.
        Used by Silent Auth to check if session is still alive.
        """
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])
            if payload["type"] != "access":
                raise TokenError("Invalid token type")
            return int(payload["sub"])
        except jwt.ExpiredSignatureError:
            raise TokenError("Token has expired")
        except jwt.InvalidTokenError:
            raise TokenError("Invalid token")

    # --- NEW HELPER METHOD 2 ---
    def get_user_id_from_token(self, token: str) -> int:
        """
        Extracts ID without checking DB blacklist (for safe tokens we just minted).
        """
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])
            return int(payload["sub"])
        except Exception:
            raise TokenError("Malformed token")

    def _generate_token(self, user_id: int | str, token_type: str) -> str:
        if token_type == "access":
            delta = timedelta(minutes=self.ACCESS_EXP_MINUTES)
        else:
            delta = timedelta(days=self.REFRESH_EXP_DAYS)

        now = datetime.now(timezone.utc)

        payload = {
            "sub": str(user_id),
            "type": token_type,
            "jti": str(uuid.uuid4()),
            "exp": now + delta,
            "iat": now,
        }

        return jwt.encode(payload, self.secret_key, algorithm=self.algorithm)
