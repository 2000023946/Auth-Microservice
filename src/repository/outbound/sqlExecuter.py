import mysql.connector

# Ensure this matches the exception name used in your Controller!
from src.app.domain.exceptions import EmailAlreadyExistsError


class SqlExecutor:
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
