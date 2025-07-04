#!/usr/bin/env python3
"""
🔧 Service Registry
Professional dependency injection and service management.

This registry provides:
- Service lifecycle management
- Dependency injection
- Service discovery
- Configuration management
"""

import logging
from typing import Dict, Type, Any, Optional
from threading import Lock
import os

# Import all services
from services.fraud_service import FraudService
from services.training_service import TrainingService
from services.mlflow_service import MLflowService
from services.validation_service import ValidationService
from services.health_service import HealthService

logger = logging.getLogger(__name__)

class ServiceRegistry:
    """
    Service Registry implementing Dependency Injection pattern
    
    Features:
    - Singleton service instances
    - Lazy initialization
    - Service discovery
    - Configuration injection
    - Health monitoring
    """
    
    def __init__(self):
        self._services: Dict[str, Any] = {}
        self._service_configs: Dict[str, Dict] = {}
        self._lock = Lock()
        
        # Initialize service configurations
        self._init_service_configs()
        
        logger.info("🔧 Service Registry initialized")
    
    def _init_service_configs(self):
        """Initialize service configurations from environment"""
        
        # Fraud Detection Service Config
        self._service_configs['fraud_service'] = {
            'redis_host': os.getenv('REDIS_HOST', 'localhost'),
            'redis_port': int(os.getenv('REDIS_PORT', 6379)),
            'redis_db': int(os.getenv('REDIS_DB', 0)),
            'mlflow_tracking_uri': os.getenv('MLFLOW_TRACKING_URI', 'http://localhost:5002'),
            'kafka_bootstrap_servers': os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092').split(','),
            'enable_kafka_streaming': os.getenv('ENABLE_KAFKA_STREAMING', 'false').lower() == 'true',
            'model_cache_ttl': int(os.getenv('MODEL_CACHE_TTL', 3600)),
            'feature_store_ttl': int(os.getenv('FEATURE_STORE_TTL', 1800))
        }
        
        # Training Service Config
        self._service_configs['training_service'] = {
            'mlflow_tracking_uri': os.getenv('MLFLOW_TRACKING_URI', 'http://localhost:5002'),
            'data_path': os.getenv('TRAINING_DATA_PATH', './data'),
            'model_output_path': os.getenv('MODEL_OUTPUT_PATH', './models'),
            'max_concurrent_jobs': int(os.getenv('MAX_CONCURRENT_TRAINING_JOBS', 3)),
            'job_timeout_hours': int(os.getenv('TRAINING_JOB_TIMEOUT_HOURS', 24)),
            'enable_gpu': os.getenv('ENABLE_GPU_TRAINING', 'false').lower() == 'true'
        }
        
        # MLflow Service Config
        self._service_configs['mlflow_service'] = {
            'tracking_uri': os.getenv('MLFLOW_TRACKING_URI', 'http://localhost:5002'),
            'artifact_location': os.getenv('MLFLOW_ARTIFACT_LOCATION', './mlruns'),
            'backend_store_uri': os.getenv('MLFLOW_BACKEND_STORE_URI', 'sqlite:///mlflow.db'),
            'registry_uri': os.getenv('MLFLOW_REGISTRY_URI', None),
            'default_experiment_name': os.getenv('MLFLOW_DEFAULT_EXPERIMENT', 'Default')
        }
        
        # Validation Service Config
        self._service_configs['validation_service'] = {
            'enable_strict_validation': os.getenv('ENABLE_STRICT_VALIDATION', 'true').lower() == 'true',
            'max_transaction_amount': float(os.getenv('MAX_TRANSACTION_AMOUNT', 1000000)),
            'required_fields': ['transaction_id', 'customer_id', 'amount'],
            'validation_timeout_seconds': int(os.getenv('VALIDATION_TIMEOUT', 5))
        }
        
        # Health Service Config
        self._service_configs['health_service'] = {
            'check_timeout': int(os.getenv('HEALTH_CHECK_TIMEOUT', 10)),
            'dependency_check_interval': int(os.getenv('DEPENDENCY_CHECK_INTERVAL', 30)),
            'enable_detailed_checks': os.getenv('ENABLE_DETAILED_HEALTH_CHECKS', 'true').lower() == 'true',
            'postgres_dsn': os.getenv('DATABASE_URL', 'postgresql://user:password@localhost:5432/bigdata'),
            'redis_url': os.getenv('REDIS_URL', 'redis://localhost:6379/0')
        }
        
        logger.info("📋 Service configurations initialized")
    
    def get_service(self, service_name: str) -> Any:
        """
        Get or create service instance
        
        Args:
            service_name: Name of the service to retrieve
            
        Returns:
            Service instance
            
        Raises:
            ValueError: If service name is not recognized
        """
        
        if service_name in self._services:
            return self._services[service_name]
        
        with self._lock:
            # Double-check pattern
            if service_name in self._services:
                return self._services[service_name]
            
            # Create service instance
            service_instance = self._create_service(service_name)
            self._services[service_name] = service_instance
            
            logger.info(f"✅ Service '{service_name}' created and registered")
            
            return service_instance
    
    def _create_service(self, service_name: str) -> Any:
        """Create service instance with dependency injection"""
        
        config = self._service_configs.get(service_name, {})
        
        if service_name == 'fraud_service':
            return FraudService(config)
            
        elif service_name == 'training_service':
            # Inject MLflow service dependency
            mlflow_service = self.get_service('mlflow_service')
            return TrainingService(config, mlflow_service)
            
        elif service_name == 'mlflow_service':
            return MLflowService(config)
            
        elif service_name == 'validation_service':
            return ValidationService(config)
            
        elif service_name == 'health_service':
            return HealthService(config, self)
            
        else:
            raise ValueError(f"Unknown service: {service_name}")
    
    def register_service(self, service_name: str, service_instance: Any):
        """
        Manually register a service instance
        
        Args:
            service_name: Name of the service
            service_instance: Service instance to register
        """
        with self._lock:
            self._services[service_name] = service_instance
            logger.info(f"📝 Service '{service_name}' manually registered")
    
    def list_services(self) -> Dict[str, str]:
        """List all available services and their status"""
        
        services_status = {}
        
        for service_name in self._service_configs.keys():
            if service_name in self._services:
                try:
                    service = self._services[service_name]
                    if hasattr(service, 'health_check'):
                        health = service.health_check()
                        services_status[service_name] = health.get('status', 'unknown')
                    else:
                        services_status[service_name] = 'active'
                except Exception as e:
                    services_status[service_name] = f'error: {str(e)}'
            else:
                services_status[service_name] = 'not_initialized'
        
        return services_status
    
    def shutdown_services(self):
        """Gracefully shutdown all services"""
        
        logger.info("🔄 Shutting down services...")
        
        for service_name, service in self._services.items():
            try:
                if hasattr(service, 'shutdown'):
                    service.shutdown()
                    logger.info(f"✅ Service '{service_name}' shut down successfully")
            except Exception as e:
                logger.error(f"❌ Error shutting down service '{service_name}': {e}")
        
        self._services.clear()
        logger.info("🔄 All services shut down")
    
    def get_service_config(self, service_name: str) -> Dict:
        """Get configuration for a specific service"""
        return self._service_configs.get(service_name, {})
    
    def update_service_config(self, service_name: str, config_updates: Dict):
        """Update service configuration (requires service restart)"""
        
        if service_name in self._service_configs:
            self._service_configs[service_name].update(config_updates)
            
            # Remove service instance to force recreation with new config
            if service_name in self._services:
                with self._lock:
                    if hasattr(self._services[service_name], 'shutdown'):
                        self._services[service_name].shutdown()
                    del self._services[service_name]
                
                logger.info(f"🔄 Service '{service_name}' configuration updated and reset")
    
    def health_check(self) -> Dict:
        """Health check for the service registry"""
        
        try:
            services_status = self.list_services()
            
            healthy_services = sum(1 for status in services_status.values() if status == 'active' or status == 'healthy')
            total_services = len(services_status)
            
            overall_health = 'healthy' if healthy_services == total_services else 'degraded'
            
            return {
                'status': overall_health,
                'services': services_status,
                'healthy_count': healthy_services,
                'total_count': total_services,
                'registry_status': 'operational'
            }
            
        except Exception as e:
            logger.error(f"Service registry health check failed: {e}")
            return {
                'status': 'unhealthy',
                'error': str(e),
                'registry_status': 'error'
            } 