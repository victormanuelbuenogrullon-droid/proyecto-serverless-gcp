FROM python:3.11-slim

WORKDIR /app

# Install system dependencies if required for compiling psycopg2 or cryptographic libs
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy backend source code
COPY . .

# Create uploads and static directories
RUN mkdir -p estaticos/avatares

# Expose FastAPI port
EXPOSE 8000

# Run uvicorn server
CMD ["uvicorn", "app.principal:app", "--host", "0.0.0.0", "--port", "8000"]
