#!/bin/bash

# 1. Exit immediately if a command exits with a non-zero status (except tests)
echo "----------------------------------------------------"
echo "🚀 STARTING INFRASTRUCTURE..."
echo "----------------------------------------------------"

# 2. Start Docker in Detached mode (-d)
docker-compose --env-file .env.test up -d --build

# 3. Wait for the Server to be ready
echo "⏳ Waiting for Auth Service to launch..."

RETRIES=0
# FIX: Added semicolon ';' before 'do'
# FIX: Added '|| [ $RETRIES -eq 60 ]' back so the loop doesn't run forever if Docker crashes
until curl -s -o /dev/null "http://localhost:5000/api/auth/me" || [ $RETRIES -eq 1000 ]; do
  sleep 1
  ((RETRIES++))
  echo -n "."
done
echo "" 

# Check if we exited the loop because of timeout
if [ $RETRIES -eq 1000 ]; then
    echo "❌ Timeout waiting for Docker!"
    docker-compose logs app
    docker-compose down
    exit 1
fi

echo "✅ Docker is UP!"

echo "----------------------------------------------------"
echo "🧪 RUNNING TESTS WITH COVERAGE..."
echo "----------------------------------------------------"

# 4. Run Pytest with Coverage
pytest --cov=src src/tests
TEST_EXIT_CODE=$?

echo "----------------------------------------------------"
echo "📊 GENERATING REPORT..."
echo "----------------------------------------------------"

# 5. Generate HTML Report
coverage html

echo "----------------------------------------------------"
echo "🧹 CLEANING UP..."
echo "----------------------------------------------------"

# 6. Stop Containers
docker-compose down -v 2>/dev/null

# 7. Check status
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ SUCCESS: All tests passed."
    exit 0
else
    echo "❌ FAILURE: Some tests failed."
    exit 1
fi