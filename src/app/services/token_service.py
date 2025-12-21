import uuid
from datetime import datetime, timedelta, timezone
from typing import Tuple
from src.app.domain.exceptions import TokenError
from .inbound.token_service import ITokenService
from ...cache.inbound.cache import ICache
from ..jwt.Ijwt import ITokenAdapter


class TokenService(ITokenService):
    def __init__(self, redis_cache: ICache, token_provider: ITokenAdapter):
        # We no longer need secret_key or algorithm here;
        # those are technical details for the provider.
        self.redis_cache = redis_cache
        self.token_provider = token_provider

        self.ACCESS_EXP_MINUTES = 15
        self.REFRESH_EXP_DAYS = 7

    def create_jwt(self, user_id: int) -> Tuple[str, str]:
        print("makign the jwt for the user")
        access_token = self._generate_token(user_id, "access")
        refresh_token = self._generate_token(user_id, "refresh")
        print("fniished encoding the otkens")
        return access_token, refresh_token

    def refresh_token(self, token: str) -> Tuple[str, str]:
        """
        Rotates tokens: Blacklists old refresh token, returns NEW pair.
        """
        try:
            # The service doesn't know HOW to decode; it just asks the provider
            payload = self.token_provider.decode(token)

            if payload.get("type") != "refresh":
                raise TokenError("Invalid token type")

            jti = payload.get("jti")
            exp = payload.get("exp")
            user_id = payload.get("sub")

            if self.redis_cache.is_blacklisted(jti):
                raise TokenError("Token is blacklisted")

            # Orchestrate blacklisting logic
            now = datetime.now(timezone.utc).timestamp()
            ttl = int(exp - now)
            if ttl > 0:
                self.redis_cache.blacklist_token(jti, ttl)

            # Issue new pair (Rotation)
            return self.create_jwt(user_id)

        except Exception as e:
            # We catch general provider errors and wrap them in Domain Exceptions
            raise TokenError(f"Token validation failed: {str(e)}")

    def logout(self, token: str) -> None:
        try:
            payload = self.token_provider.decode(token)
            jti = payload.get("jti")
            exp = payload.get("exp")

            if jti and exp:
                now = datetime.now(timezone.utc).timestamp()
                ttl = int(exp - now)
                if ttl > 0:
                    self.redis_cache.blacklist_token(jti, ttl)
        except Exception:
            pass

    def validate_and_get_user_id(self, token: str) -> int:
        payload = self.token_provider.decode(token)
        if payload.get("type") != "access":
            raise TokenError("Invalid token type")
        return payload["sub"]

    def _generate_token(self, user_id: int | str, token_type: str) -> str:
        """
        Orchestrates the payload creation, but delegates the
        actual signing/encoding to the provider.
        """
        delta = (
            timedelta(minutes=self.ACCESS_EXP_MINUTES)
            if token_type == "access"
            else timedelta(days=self.REFRESH_EXP_DAYS)
        )

        now = datetime.now(timezone.utc)

        # The service defines WHAT goes in the token
        payload = {
            "sub": str(user_id),
            "type": token_type,
            "jti": str(uuid.uuid4()),
            "exp": (
                now + delta
            ).timestamp(),  # Numeric timestamp for cross-provider compatibility
            "iat": now.timestamp(),
        }

        # The provider handles the technical encoding
        print("now encoding", self.token_provider)
        return self.token_provider.encode(payload)
