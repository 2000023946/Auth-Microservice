import mysql.connector


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

            # 2. Fetch Generated ID (if procedure returns one via SELECT LAST_INSERT_ID)
            for result in cursor.stored_results():
                row = result.fetchone()
                if row:
                    generated_id = row[0]

            # 3. Commit (Crucial for writes)
            conn.commit()
            return generated_id

        except mysql.connector.IntegrityError as e:
            # Re-raise so Repo can handle business logic (duplicate email)
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

            # Fetch the first result set
            for result in cursor.stored_results():
                row = result.fetchone()
                if row:
                    return row  # Return raw DB tuple

            return None

        finally:
            cursor.close()
            conn.close()
