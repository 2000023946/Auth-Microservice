from src.repository.inbound.dbExecuter import UserDBExecuter
from src.app.services.inbound.hashing_service import Hasher


class DatabaseProvider:
    def __init__(
        self, db_config: dict, db_executer: UserDBExecuter, hasher_class: type[Hasher]
    ):
        self.executor = db_executer(db_config)
        self.hasher = hasher_class()
