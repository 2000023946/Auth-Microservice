from ..inbound.cache import Cache


class RedisCache(Cache):
    def __init__(self, redis_client):
        """
        Dependency Injection: Pass in the configured redis.Redis() client.
        """
        self.client = redis_client

    def blacklist_token(self, jti: str, expiry_seconds: int) -> None:
        """
        Saves a JTI to Redis with a TTL matching the token's remaining life.
        """
        # setex = SET with Expiry
        # We don't care about the value ("blacklisted"), only the key existence.
        self.client.setex(jti, expiry_seconds, "blacklisted")

    def is_blacklisted(self, jti: str) -> bool:
        """
        Checks if the JTI exists in Redis.
        Returns: True if blacklisted, False otherwise.
        """
        # Redis .exists() returns 1 if found, 0 if not.
        return self.client.exists(jti) > 0
