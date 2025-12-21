from src.app.services.user_service import UserService  # type: ignore
from src.app.services.token_service import TokenService  # type: ignore
from src.config.app_config import AppConfig
from src.app.jwt.jwt_adapter import JwtAdapter


class ServiceComponent:
    def __init__(self, repos, infra):
        self.user_service = UserService(repos.user_repo)
        self.token_service = TokenService(
            infra.redis.cache,
            JwtAdapter(AppConfig.JWT_SECRET, "HS256"),
        )
