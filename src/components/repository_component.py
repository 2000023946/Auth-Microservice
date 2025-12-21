from src.repository.outbound.userRepo import UserRepo
from src.app.services.inbound.hashing_service import Hasher
from src.app.services.hashing_service import BcryptHasher


class RepositoryComponent:
    def __init__(self, infra):
        self.user_repo = UserRepo(infra.db.executor, infra.db.hasher)
