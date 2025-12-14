import redis
from src.cache.outbound.redis_cache import RedisCache


class RedisProvider:
    def __init__(self, host: str, port: int):
        client = redis.Redis(
            host=host,
            port=port,
            decode_responses=True,
        )
        self.cache = RedisCache(client)
