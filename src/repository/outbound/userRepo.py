from mysql.connector import IntegrityError
from src.app.domain.user import User
from src.app.domain.exceptions import EmailAlreadyExistsError
from src.mapper.user_mapper import UserMapper


class UserRepo:
    def __init__(self, sql_executor):
        self.sql_executor = sql_executor

    def save(self, user: User) -> None:
        try:
            # 1. Domain -> UserDB (Data Class)
            user_db = UserMapper.to_db(user)

            # 2. Extract Tuple for SQL Executor (email, password_hash)
            # CRITICAL FIX: Extract the raw values from the object here
            db_params = (user_db.email, user_db.password_hash)

            # 3. Execute SQL (returns generated ID)
            # Make sure "create_user" matches your stored procedure name exactly
            new_id = self.sql_executor.execute_write("create_user", db_params)

            # 4. Update Domain Object
            # Ensure User.setId() exists in your domain model, otherwise use user.id = new_id
            if new_id:
                user.setId(new_id)

        except IntegrityError:
            raise EmailAlreadyExistsError(f"User {user.email} already exists.")

    def validate_credentials(self, email: str) -> User | None:
        # 1. Execute SQL
        row = self.sql_executor.execute_read_one("login_user", (email,))

        # 2. DB Row -> Domain Object
        return UserMapper.to_domain(row)

    def get_user_by_id(self, id):
        row = self.sql_executor.execute_read_one("get_user_by_id", (id,))

        return UserMapper.to_domain(row)
