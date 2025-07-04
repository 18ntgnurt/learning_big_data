#!/usr/bin/env python3
"""
⚙️ Application Configuration
Professional configuration management for different environments.

This module provides:
- Environment-specific configurations
- Security settings
- Database configurations
- Rate limiting settings
- Feature flags
"""

import os
from typing import Dict, Any
from datetime import timedelta

class BaseConfig:
    """Base configuration with common settings"""
    
    # Application settings
    SECRET_KEY = os.getenv('SECRET_KEY', 'dev-secret-key-change-in-production')
    
    # Flask settings
    JSON_SORT_KEYS = False
    JSONIFY_PRETTYPRINT_REGULAR = True
    MAX_CONTENT_LENGTH = 16 * 1024 * 1024  # 16MB max request size
    
    # Security settings
    ENABLE_AUTH = os.getenv('ENABLE_AUTH', 'false').lower() == 'true'
    AUTH_TOKEN_EXPIRE_HOURS = int(os.getenv('AUTH_TOKEN_EXPIRE_HOURS', 24))
    CORS_ORIGINS = os.getenv('CORS_ORIGINS', '*').split(',')
    
    # Rate limiting
    ENABLE_RATE_LIMITING = os.getenv('ENABLE_RATE_LIMITING', 'true').lower() == 'true'
    RATE_LIMIT_STORAGE_URL = os.getenv('RATE_LIMIT_STORAGE_URL', 'redis://localhost:6379/1')
    
    # Monitoring and metrics
    ENABLE_METRICS = os.getenv('ENABLE_METRICS', 'true').lower() == 'true'
    METRICS_PORT = int(os.getenv('METRICS_PORT', 8000))
    
    # Logging
    LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
    LOG_FORMAT = os.getenv('LOG_FORMAT', '%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    
    # Database settings
    DATABASE_URL = os.getenv('DATABASE_URL', 'postgresql://user:password@localhost:5432/bigdata')
    DATABASE_POOL_SIZE = int(os.getenv('DATABASE_POOL_SIZE', 10))
    DATABASE_POOL_TIMEOUT = int(os.getenv('DATABASE_POOL_TIMEOUT', 30))
    
    # Redis settings
    REDIS_URL = os.getenv('REDIS_URL', 'redis://localhost:6379/0')
    REDIS_TIMEOUT = int(os.getenv('REDIS_TIMEOUT', 5))
    
    # MLflow settings
    MLFLOW_TRACKING_URI = os.getenv('MLFLOW_TRACKING_URI', 'http://localhost:5002')
    MLFLOW_ARTIFACT_LOCATION = os.getenv('MLFLOW_ARTIFACT_LOCATION', './mlruns')
    MLFLOW_REGISTRY_URI = os.getenv('MLFLOW_REGISTRY_URI', None)
    
    # Kafka settings
    KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092').split(',')
    KAFKA_ENABLE_STREAMING = os.getenv('ENABLE_KAFKA_STREAMING', 'false').lower() == 'true'
    KAFKA_CONSUMER_GROUP = os.getenv('KAFKA_CONSUMER_GROUP', 'ml-services')
    
    # Business logic settings
    MAX_BATCH_SIZE = int(os.getenv('MAX_BATCH_SIZE', 100))
    MAX_CONCURRENT_TRAINING_JOBS = int(os.getenv('MAX_CONCURRENT_TRAINING_JOBS', 3))
    TRAINING_JOB_TIMEOUT_HOURS = int(os.getenv('TRAINING_JOB_TIMEOUT_HOURS', 24))
    
    # Feature flags
    ENABLE_FEATURE_STORE = os.getenv('ENABLE_FEATURE_STORE', 'true').lower() == 'true'
    ENABLE_MODEL_CACHING = os.getenv('ENABLE_MODEL_CACHING', 'true').lower() == 'true'
    ENABLE_ASYNC_PROCESSING = os.getenv('ENABLE_ASYNC_PROCESSING', 'false').lower() == 'true'
    
    # Health check settings
    HEALTH_CHECK_TIMEOUT = int(os.getenv('HEALTH_CHECK_TIMEOUT', 10))
    DEPENDENCY_CHECK_INTERVAL = int(os.getenv('DEPENDENCY_CHECK_INTERVAL', 30))
    
    @classmethod
    def get_config_dict(cls) -> Dict[str, Any]:
        """Get configuration as dictionary"""
        config_dict = {}
        for attr in dir(cls):
            if not attr.startswith('_') and not callable(getattr(cls, attr)):
                config_dict[attr] = getattr(cls, attr)
        return config_dict

class DevelopmentConfig(BaseConfig):
    """Development environment configuration"""
    
    DEBUG = True
    TESTING = False
    
    # More verbose logging in development
    LOG_LEVEL = 'DEBUG'
    
    # Relaxed security for development
    ENABLE_AUTH = False
    CORS_ORIGINS = ['*']
    
    # Disabled rate limiting for easier testing
    ENABLE_RATE_LIMITING = False
    
    # Local development URLs
    MLFLOW_TRACKING_URI = 'http://localhost:5002'
    DATABASE_URL = 'postgresql://user:password@localhost:5432/bigdata'
    REDIS_URL = 'redis://localhost:6379/0'
    
    # Enable all features for development
    ENABLE_FEATURE_STORE = True
    ENABLE_MODEL_CACHING = True
    ENABLE_ASYNC_PROCESSING = True

class TestingConfig(BaseConfig):
    """Testing environment configuration"""
    
    DEBUG = False
    TESTING = True
    
    # Use in-memory databases for testing
    DATABASE_URL = 'sqlite:///:memory:'
    REDIS_URL = 'redis://localhost:6379/15'  # Use different Redis DB
    
    # Disable external services for testing
    KAFKA_ENABLE_STREAMING = False
    ENABLE_METRICS = False
    ENABLE_RATE_LIMITING = False
    
    # Smaller limits for testing
    MAX_BATCH_SIZE = 10
    MAX_CONCURRENT_TRAINING_JOBS = 1
    
    # Fast timeouts for testing
    HEALTH_CHECK_TIMEOUT = 1
    DEPENDENCY_CHECK_INTERVAL = 5

class StagingConfig(BaseConfig):
    """Staging environment configuration"""
    
    DEBUG = False
    TESTING = False
    
    # Enable auth and security
    ENABLE_AUTH = True
    ENABLE_RATE_LIMITING = True
    
    # Production-like settings but with more relaxed limits
    MAX_BATCH_SIZE = 50
    MAX_CONCURRENT_TRAINING_JOBS = 2
    
    # Staging-specific URLs (should be set via environment variables)
    MLFLOW_TRACKING_URI = os.getenv('MLFLOW_TRACKING_URI', 'http://mlflow-staging:5002')
    DATABASE_URL = os.getenv('DATABASE_URL', 'postgresql://user:password@postgres-staging:5432/bigdata')
    REDIS_URL = os.getenv('REDIS_URL', 'redis://redis-staging:6379/0')

class ProductionConfig(BaseConfig):
    """Production environment configuration"""
    
    DEBUG = False
    TESTING = False
    
    # Strict security in production
    ENABLE_AUTH = True
    ENABLE_RATE_LIMITING = True
    SECRET_KEY = os.getenv('SECRET_KEY')  # Must be set in production
    
    # Production limits
    MAX_BATCH_SIZE = 100
    MAX_CONCURRENT_TRAINING_JOBS = 5
    
    # Longer timeouts for production stability
    TRAINING_JOB_TIMEOUT_HOURS = 48
    DATABASE_POOL_TIMEOUT = 60
    
    # Conservative feature flags
    ENABLE_ASYNC_PROCESSING = True
    ENABLE_FEATURE_STORE = True
    ENABLE_MODEL_CACHING = True
    
    # Production logging
    LOG_LEVEL = 'WARNING'
    
    @classmethod
    def validate(cls):
        """Validate production configuration"""
        required_env_vars = [
            'SECRET_KEY',
            'DATABASE_URL',
            'REDIS_URL',
            'MLFLOW_TRACKING_URI'
        ]
        
        missing_vars = []
        for var in required_env_vars:
            if not os.getenv(var):
                missing_vars.append(var)
        
        if missing_vars:
            raise ValueError(f"Missing required environment variables: {', '.join(missing_vars)}")

class Config:
    """Configuration factory"""
    
    _configs = {
        'development': DevelopmentConfig,
        'testing': TestingConfig,
        'staging': StagingConfig,
        'production': ProductionConfig
    }
    
    def __new__(cls, config_name: str = None):
        """
        Create configuration instance based on environment
        
        Args:
            config_name: Configuration environment name
            
        Returns:
            Configuration class instance
        """
        if config_name is None:
            config_name = os.getenv('FLASK_ENV', 'development')
        
        if config_name not in cls._configs:
            raise ValueError(f"Unknown configuration: {config_name}. Available: {list(cls._configs.keys())}")
        
        config_class = cls._configs[config_name]
        
        # Validate production configuration
        if config_name == 'production':
            config_class.validate()
        
        return config_class
    
    @classmethod
    def get_available_configs(cls) -> list:
        """Get list of available configuration names"""
        return list(cls._configs.keys())
    
    @classmethod
    def get_current_config_name(cls) -> str:
        """Get current configuration name from environment"""
        return os.getenv('FLASK_ENV', 'development') 