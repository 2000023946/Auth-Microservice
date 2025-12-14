import pytest
from unittest.mock import MagicMock, patch
from mysql.connector import IntegrityError  # type: ignore
from src.repository.outbound.sqlExecuter import SqlExecutor  # type: ignore

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_driver():
    """
    Patches the actual MySQL driver so we don't need a running DB.
    """
    with patch("mysql.connector.connect") as mock_connect:
        yield mock_connect


@pytest.fixture
def sql_executor():
    """
    Initializes the executor with a dummy config.
    """
    return SqlExecutor(db_config={"user": "test", "password": "123"})


# ----------------------------------------------------------------
# Write Tests (INSERT / UPDATE)
# ----------------------------------------------------------------


def test_execute_write_commits_transaction(sql_executor, mock_driver):
    """
    Scenario: Successful insert.
    Expected:
    1. Opens connection.
    2. Calls Procedure.
    3. Commits transaction (Crucial!).
    4. Returns generated ID.
    5. Closes connection.
    """
    # Arrange: Setup Mocks
    mock_conn = MagicMock()
    mock_cursor = MagicMock()

    mock_driver.return_value = mock_conn
    mock_conn.cursor.return_value = mock_cursor

    # Simulate DB returning a "Last Insert ID" of 101
    mock_result = MagicMock()
    mock_result.fetchone.return_value = (101,)
    mock_cursor.stored_results.return_value = [mock_result]

    # Act
    result_id = sql_executor.execute_write("create_user", ("email@gt", "hash"))

    # Assert
    # 1. Logic Check
    assert result_id == 101

    # 2. Driver Check
    mock_cursor.callproc.assert_called_with("create_user", ("email@gt", "hash"))
    mock_conn.commit.assert_called_once()  # <--- Critical check for writes

    # 3. Cleanup Check
    mock_cursor.close.assert_called_once()
    mock_conn.close.assert_called_once()


def test_execute_write_bubbles_integrity_error(sql_executor, mock_driver):
    """
    Scenario: Database throws "Duplicate Entry".
    Expected: Executor allows the error to bubble up (so Repo can catch it).
    """
    # Arrange
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_driver.return_value = mock_conn
    mock_conn.cursor.return_value = mock_cursor

    # Simulate Driver Crash
    mock_cursor.callproc.side_effect = IntegrityError("Duplicate")

    # Act & Assert
    with pytest.raises(IntegrityError):
        sql_executor.execute_write("create_user", ("dupe", "hash"))

    # Cleanup should still happen!
    mock_cursor.close.assert_called_once()
    mock_conn.close.assert_called_once()


# ----------------------------------------------------------------
# Read Tests (SELECT)
# ----------------------------------------------------------------


def test_execute_read_one_fetches_data(sql_executor, mock_driver):
    """
    Scenario: Successful select.
    Expected: Returns the raw tuple row.
    """
    # Arrange
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_driver.return_value = mock_conn
    mock_conn.cursor.return_value = mock_cursor

    # Simulate DB returning a row
    mock_result = MagicMock()
    mock_result.fetchone.return_value = (1, "found@gt", "hash")
    mock_cursor.stored_results.return_value = [mock_result]

    # Act
    row = sql_executor.execute_read_one("login_user", ("found@gt",))

    # Assert
    assert row == (1, "found@gt", "hash")

    # READS should NOT commit (performance)
    mock_conn.commit.assert_not_called()


def test_execute_read_one_returns_none_if_empty(sql_executor, mock_driver):
    """
    Scenario: No user found.
    Expected: Returns None.
    """
    # Arrange
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_driver.return_value = mock_conn
    mock_conn.cursor.return_value = mock_cursor

    # Simulate Empty Result Set
    mock_result = MagicMock()
    mock_result.fetchone.return_value = None
    mock_cursor.stored_results.return_value = [mock_result]

    # Act
    row = sql_executor.execute_read_one("login_user", ("ghost",))

    # Assert
    assert row is None
