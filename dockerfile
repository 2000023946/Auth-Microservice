# 1. Base Image: Use a lightweight Python version
FROM python:3.11-slim

# 2. Environment Variables
# PYTHONDONTWRITEBYTECODE: Prevents Python from writing .pyc files
# PYTHONUNBUFFERED: Ensures logs are flushed immediately (helps debug crashes)
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# 3. Set Working Directory inside the container
WORKDIR /app

# 4. Install Dependencies
# Copy requirements first to leverage Docker cache
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copy the Application Code
COPY . .

# 6. Expose the Port (Flask defaults to 5000)
EXPOSE 5000

# 7. Run the Application
# Using 'python main.py' is fine for dev. 
# For prod, you'd use 'gunicorn main:app'.
CMD ["python", "main.py"]