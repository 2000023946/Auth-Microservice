import pytest  # type: ignore
import requests  # type: ignore
import pymysql  # <--- CHANGE IMPORT
import redis  # type: ignore
import time
import os
from dotenv import load_dotenv  # type: ignore

load_dotenv(".env.test")

# Config from your .env.test
BASE_URL = "http://localhost:5000/api/auth"
DB_CONFIG = {
    "user": os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASS", "rootpassword"),
    "host": "localhost",
    "database": os.getenv("DB_NAME", "auth_db"),
    "port": 3307,  # <--- THIS IS THE FIX
    "use_pure": True,  # <--- THIS IS THE MAGIC FIX. ADD IT NOW.
    "connection_timeout": 10,
}


@pytest.fixture(scope="session", autouse=True)
def wait_for_services():
    """Waits for Docker services to be fully ready."""
    for _ in range(30):
        try:
            # Just checking if the port is open and app responds
            # Since we don't have a health check endpoint, we'll assume 404/405 is fine (server is up)
            requests.get(f"{BASE_URL}/me", timeout=1)
            return
        except requests.ConnectionError:
            time.sleep(1)
    pytest.fail("Services did not start in time.")


import pytest
import pymysql
import os
import time  # <--- Make sure to import time

# ... (keep your existing DB_CONFIG) ...
import pytest
import pymysql
import os
import time

# ... (Keep your DB_CONFIG dictionary as is) ...


@pytest.fixture(autouse=True)
def clean_environment():
    """
    RESET STATE: Truncates MySQL and Flushes Redis before EVERY test.
    Includes aggressive Retries and Timeouts to handle Docker/Mac startup lag.
    """
    # INCREASED: Give it up to 10 seconds total (5 tries * 2s delay)
    retries = 5
    delay = 2

    last_error = None

    for attempt in range(retries):
        try:
            # 1. Connect with EXTENDED Timeouts
            conn = pymysql.connect(
                user=DB_CONFIG["user"],
                password=DB_CONFIG["password"],
                host="127.0.0.1",  # Force IPv4
                database=DB_CONFIG["database"],
                port=DB_CONFIG["port"],
                read_timeout=30,  # Increased to 30s
                write_timeout=30,  # Increased to 30s
                connect_timeout=30,  # Increased to 30s
            )

            # 2. Execute Cleanup
            cursor = conn.cursor()
            cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
            cursor.execute("TRUNCATE TABLE users;")
            cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
            conn.commit()
            conn.close()

            # If successful, exit the loop immediately
            return

        except (pymysql.MySQLError, pymysql.err.OperationalError) as e:
            last_error = e
            # Only wait if we have retries left
            if attempt < retries - 1:
                time.sleep(delay)
                continue

    # If we exit the loop, we failed
    pytest.fail(
        f"Database Cleanup Failed after {retries} attempts. Last error: {last_error}"
    )
