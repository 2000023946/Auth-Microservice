import unittest
from unittest.mock import MagicMock, patch
from src.repository.outbound.sqlExecuter import UserSQLExecuter
from src.app.domain.exceptions import EmailAlreadyExistsError


class TestUserSQLExecuter(unittest.TestCase):

    def setUp(self):
        # Configuration mock
        self.db_config = {
            "host": "localhost",
            "user": "root",
            "password": "password",
            "database": "auth_db",
        }
        # Initialize the executer
        self.executer = UserSQLExecuter(self.db_config)

    @patch("mysql.connector.connect")
    def test_create_user_success(self, mock_connect):
        # Arrange
        mock_conn = MagicMock()
        mock_cursor = mock_conn.cursor.return_value
        mock_connect.return_value = mock_conn

        # Mock stored_results for create_user to return the generated ID
        mock_result = MagicMock()
        mock_result.fetchone.return_value = ("uuid-123",)
        mock_cursor.stored_results.return_value = [mock_result]

        # Act
        result = self.executer.create_user("uuid-123", "test@gt.edu", "hash")

        # Assert
        mock_cursor.callproc.assert_called_once_with(
            "create_user", ("uuid-123", "test@gt.edu", "hash")
        )
        self.assertEqual(result, None)
        mock_conn.commit.assert_called_once()
        mock_conn.close.assert_called_once()

    @patch("mysql.connector.connect")
    def test_login_user_success(self, mock_connect):
        # Arrange
        mock_conn = MagicMock()
        mock_cursor = mock_conn.cursor.return_value
        mock_connect.return_value = mock_conn

        # Mock procedure returning (id, password_hash)
        mock_result = MagicMock()
        mock_result.fetchone.return_value = ("uuid-456", "$2b$12$hash")
        mock_cursor.stored_results.return_value = [mock_result]

        # Act
        result = self.executer.login_user("test@gt.edu")

        # Assert
        mock_cursor.callproc.assert_called_once_with("login_user", ("test@gt.edu",))
        self.assertEqual(result["id"], "uuid-456")
        self.assertEqual(result["password_hash"], "$2b$12$hash")

    @patch("mysql.connector.connect")
    def test_create_user_duplicate_email(self, mock_connect):
        # Arrange
        import mysql.connector

        mock_conn = MagicMock()
        mock_cursor = mock_conn.cursor.return_value
        mock_connect.return_value = mock_conn

        # Simulate the MySQL error for duplicate entry (1062)
        error = mysql.connector.Error("Email already registered")
        error.errno = 1062
        mock_cursor.callproc.side_effect = error

        # Act & Assert
        with self.assertRaises(EmailAlreadyExistsError):
            self.executer.create_user("uuid", "taken@gt.edu", "hash")

    @patch("mysql.connector.connect")
    def test_get_user_by_id_not_found(self, mock_connect):
        # Arrange
        mock_conn = MagicMock()
        mock_cursor = mock_conn.cursor.return_value
        mock_connect.return_value = mock_conn

        # Mock empty results
        mock_cursor.stored_results.return_value = []

        # Act
        result = self.executer.get_user_by_id("missing-uuid")

        # Assert
        self.assertIsNone(result)


if __name__ == "__main__":
    unittest.main()
