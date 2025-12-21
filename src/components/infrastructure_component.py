from src.config.app_config import AppConfig
from src.provider.db_provider import DatabaseProvider
from src.provider.redis_provider import RedisProvider
from src.repository.outbound.sqlExecuter import UserSQLExecuter
from src.app.services.hashing_service import BcryptHasher


class InfrastructureComponent:
    def __init__(self):
        self.db = DatabaseProvider(AppConfig.DB, UserSQLExecuter, BcryptHasher)
        self.redis = RedisProvider(
            AppConfig.REDIS_HOST,
            AppConfig.REDIS_PORT,
        )
