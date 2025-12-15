import os

# Check if we are in "Development" mode
IS_PRODUCTION = os.getenv("FLASK_ENV") == "production"
