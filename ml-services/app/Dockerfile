FROM python:3.9-slim

LABEL maintainer="Big Data Learning Platform"
LABEL description="ML Services Platform with Professional MVC Architecture"
LABEL version="2.0.0"

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    curl \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better Docker layer caching
# Use lightweight requirements for faster builds
COPY ../../requirements-fast.txt /app/requirements.txt

# Install Python dependencies with optimization
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app/ /app/

# Create necessary directories
RUN mkdir -p /app/data /app/models /app/logs

# Set environment variables
ENV PYTHONPATH=/app
ENV FLASK_APP=main.py
ENV FLASK_ENV=production
ENV PYTHONUNBUFFERED=1

# Create non-root user for security
RUN adduser --disabled-password --gecos '' appuser && \
    chown -R appuser:appuser /app
USER appuser

# Expose ports
EXPOSE 5000 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Default command
CMD ["python", "main.py"] 