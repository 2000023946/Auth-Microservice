from src.repository.outbound.userRepo import UserRepo


class RepositoryComponent:
    def __init__(self, infra):
        self.user_repo = UserRepo(infra.db.executor)
