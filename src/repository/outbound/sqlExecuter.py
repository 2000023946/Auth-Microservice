from ..inbound.dbExecuter import UserDBExecuter

# Ensure this matches the exception name used in your Controller!


import mysql.connector

# Ensure this matches the exception name used in your Controller!
from src.app.domain.exceptions import EmailAlreadyExistsError


class SQLExecutor:
    def __init__(self, db_config):
        self.db_config = db_config

    def _get_connection(self):
        return mysql.connector.connect(**self.db_config)

    def execute_write(self, procedure_name: str, args: tuple) -> int | None:
        """
        Executes an INSERT/UPDATE procedure.
        Returns: The generated ID (if any), or None.
        """
        conn = self._get_connection()
        cursor = conn.cursor()
        generated_id = None

        try:
            # 1. Execute
            cursor.callproc(procedure_name, args)

            # 2. Fetch Generated ID
            for result in cursor.stored_results():
                row = result.fetchone()
                if row:
                    generated_id = row[0]

            # 3. Commit
            conn.commit()
            return generated_id

        # CRITICAL FIX: Catch the base 'Error' class, not just IntegrityError.
        # Custom signals (SQLSTATE 45000) often raise generic Errors.
        except mysql.connector.Error as e:

            error_msg = str(e).lower()

            # Check for your custom signal OR the standard MySQL duplicate code (1062)
            if "email already registered" in error_msg or e.errno == 1062:
                raise EmailAlreadyExistsError("User with this email already exists")

            # If it's a different error, let it crash (results in 500)
            raise e

        finally:
            cursor.close()
            conn.close()

    def execute_read_one(self, procedure_name: str, args: tuple) -> tuple | None:
        """
        Executes a SELECT procedure.
        Returns: A single raw row (tuple) or None.
        """
        conn = self._get_connection()
        cursor = conn.cursor()

        try:
            cursor.callproc(procedure_name, args)

            for result in cursor.stored_results():
                row = result.fetchone()
                if row:
                    return row

            return None

        finally:
            cursor.close()
            conn.close()


class UserSQLExecuter(SQLExecutor, UserDBExecuter):

    def create_user(
        self,
        user_id: str,
        email: str,
        password_hash: str,
    ) -> dict | None:
        # 1. Use execute_write because we are calling an INSERT procedure
        # 2. Map the procedure name and args
        self.execute_write("create_user", (user_id, email, password_hash))

    def login_user(self, email: str) -> dict | None:
        # Use execute_read_one as it expects a single user row back
        print("stargin the login user procuedure")
        row = self.execute_read_one("login_user", (email,))

        if not row:
            return None

        # IMPORTANT: MySQL raw tuples need mapping to dict keys for your Service layer
        # Assuming your procedure returns (id, email, password_hash)
        return {"id": row[0], "password_hash": row[1]}

    def get_user_by_id(self, id: int) -> dict | None:
        row = self.execute_read_one("get_user_by_id", (id,))

        if not row:
            return None

        # Standard mapping
        return {"id": row[0], "createdAt": row[1]}
