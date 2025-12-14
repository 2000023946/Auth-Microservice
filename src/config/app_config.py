import os
from dotenv import load_dotenv

load_dotenv()


class AppConfig:
    DB = {
        "user": os.getenv("DB_USER", "root"),
        "password": os.getenv("DB_PASS", "password"),
        "host": os.getenv("DB_HOST", "localhost"),
        "database": os.getenv("DB_NAME", "auth_db"),
    }

    REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
    REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))

    JWT_SECRET = os.getenv("JWT_SECRET", "super-secret-dev-key")
