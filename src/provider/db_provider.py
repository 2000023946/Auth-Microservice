from src.repository.outbound.sqlExecuter import SqlExecutor


class DatabaseProvider:
    def __init__(self, db_config: dict):
        self.executor = SqlExecutor(db_config)
