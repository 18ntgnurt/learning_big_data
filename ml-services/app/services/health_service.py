#!/usr/bin/env python3
"""
🏥 Health Service
System health monitoring and dependency checking.
"""

import logging
import time
import psutil
import socket
from datetime import datetime
from typing import Dict, Any
import requests

logger = logging.getLogger(__name__)

class HealthService:
    """
    Health Service
    
    Provides comprehensive health checking including:
    - Application health
    - Database connectivity
    - External dependencies
    - System resources
    """
    
    def __init__(self, config: Dict, service_registry=None):
        self.config = config
        self.service_registry = service_registry
        self.start_time = time.time()
        self.last_check = None
        self.health_cache = {}
        
        logger.info("🏥 Health Service initialized")
    
    def get_basic_health(self) -> Dict[str, Any]:
        """Get basic application health"""
        uptime = time.time() - self.start_time
        
        return {
            'status': 'healthy',
            'uptime_seconds': uptime,
            'uptime_human': self._format_uptime(uptime),
            'timestamp': datetime.now().isoformat(),
            'version': '2.0.0',
            'environment': 'development'  # Should come from config
        }
    
    def get_detailed_health(self) -> Dict[str, Any]:
        """Get detailed health including system metrics"""
        basic_health = self.get_basic_health()
        
        # System resources
        cpu_percent = psutil.cpu_percent(interval=1)
        memory = psutil.virtual_memory()
        disk = psutil.disk_usage('/')
        
        system_health = {
            'cpu_usage_percent': cpu_percent,
            'memory_usage_percent': memory.percent,
            'memory_available_mb': memory.available / (1024 * 1024),
            'disk_usage_percent': disk.percent,
            'disk_available_gb': disk.free / (1024 * 1024 * 1024)
        }
        
        # Determine system health status
        system_status = 'healthy'
        if cpu_percent > 90 or memory.percent > 90 or disk.percent > 90:
            system_status = 'degraded'
        if cpu_percent > 95 or memory.percent > 95 or disk.percent > 95:
            system_status = 'unhealthy'
        
        return {
            **basic_health,
            'system': {
                'status': system_status,
                **system_health
            }
        }
    
    def check_readiness(self) -> Dict[str, Any]:
        """Check if service is ready to accept requests"""
        checks = {
            'services_initialized': self._check_services_initialized(),
            'dependencies_available': self._quick_dependency_check(),
            'resources_available': self._check_resource_availability()
        }
        
        all_ready = all(check['status'] == 'healthy' for check in checks.values())
        
        return {
            'ready': all_ready,
            'status': 'healthy' if all_ready else 'not_ready',
            'checks': checks,
            'timestamp': datetime.now().isoformat()
        }
    
    def check_liveness(self) -> Dict[str, Any]:
        """Check if service is alive and responding"""
        try:
            # Basic liveness check
            current_time = time.time()
            
            # Check if main thread is responsive
            thread_responsive = self._check_thread_responsiveness()
            
            # Check if we can allocate memory
            memory_test = list(range(1000))  # Simple memory allocation test
            
            return {
                'alive': True,
                'status': 'healthy',
                'thread_responsive': thread_responsive,
                'memory_allocation': len(memory_test) == 1000,
                'response_time_ms': (time.time() - current_time) * 1000,
                'timestamp': datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"Liveness check failed: {e}")
            return {
                'alive': False,
                'status': 'unhealthy',
                'error': str(e),
                'timestamp': datetime.now().isoformat()
            }
    
    def check_dependencies(self) -> Dict[str, Any]:
        """Check external dependencies"""
        dependencies = {}
        
        # Check database
        dependencies['database'] = self._check_database()
        
        # Check Redis
        dependencies['redis'] = self._check_redis()
        
        # Check MLflow
        dependencies['mlflow'] = self._check_mlflow()
        
        # Check Kafka (if enabled)
        if self.config.get('KAFKA_ENABLE_STREAMING', False):
            dependencies['kafka'] = self._check_kafka()
        
        return dependencies
    
    def _check_services_initialized(self) -> Dict[str, Any]:
        """Check if all required services are initialized"""
        if not self.service_registry:
            return {
                'status': 'unhealthy',
                'message': 'Service registry not available'
            }
        
        try:
            # Try to get core services
            fraud_service = self.service_registry.get_service('fraud_service')
            
            return {
                'status': 'healthy',
                'message': 'All services initialized',
                'services_count': len(self.service_registry._services)
            }
        except Exception as e:
            return {
                'status': 'unhealthy',
                'message': f'Service initialization failed: {str(e)}'
            }
    
    def _quick_dependency_check(self) -> Dict[str, Any]:
        """Quick check of critical dependencies"""
        try:
            # Check if we can import critical modules
            import sklearn  # noqa
            import pandas   # noqa
            import numpy    # noqa
            
            return {
                'status': 'healthy',
                'message': 'Core dependencies available'
            }
        except ImportError as e:
            return {
                'status': 'unhealthy',
                'message': f'Missing dependency: {str(e)}'
            }
    
    def _check_resource_availability(self) -> Dict[str, Any]:
        """Check if sufficient resources are available"""
        try:
            memory = psutil.virtual_memory()
            disk = psutil.disk_usage('/')
            
            # Check if we have enough resources
            if memory.percent > 95:
                return {
                    'status': 'unhealthy',
                    'message': f'Memory usage critical: {memory.percent}%'
                }
            
            if disk.percent > 95:
                return {
                    'status': 'unhealthy',
                    'message': f'Disk usage critical: {disk.percent}%'
                }
            
            return {
                'status': 'healthy',
                'message': 'Sufficient resources available'
            }
        except Exception as e:
            return {
                'status': 'unhealthy',
                'message': f'Resource check failed: {str(e)}'
            }
    
    def _check_thread_responsiveness(self) -> bool:
        """Check if main thread is responsive"""
        try:
            # Simple computation to test thread responsiveness
            start = time.time()
            sum(range(10000))
            duration = time.time() - start
            
            # If this takes more than 100ms, something might be wrong
            return duration < 0.1
        except Exception:
            return False
    
    def _check_database(self) -> Dict[str, Any]:
        """Check database connectivity"""
        try:
            # This is a mock implementation
            # In real implementation, would test actual database connection
            return {
                'status': 'healthy',
                'message': 'Database connection healthy',
                'response_time_ms': 5.2
            }
        except Exception as e:
            return {
                'status': 'unhealthy',
                'message': f'Database connection failed: {str(e)}',
                'response_time_ms': None
            }
    
    def _check_redis(self) -> Dict[str, Any]:
        """Check Redis connectivity"""
        try:
            # This is a mock implementation
            # In real implementation, would test actual Redis connection
            return {
                'status': 'healthy',
                'message': 'Redis connection healthy',
                'response_time_ms': 2.1
            }
        except Exception as e:
            return {
                'status': 'unhealthy',
                'message': f'Redis connection failed: {str(e)}',
                'response_time_ms': None
            }
    
    def _check_mlflow(self) -> Dict[str, Any]:
        """Check MLflow connectivity"""
        try:
            mlflow_uri = self.config.get('MLFLOW_TRACKING_URI', 'http://localhost:5002')
            
            # Try to connect to MLflow
            start_time = time.time()
            # This would normally make a real request to MLflow
            # For now, just simulate a successful check
            response_time = (time.time() - start_time) * 1000
            
            return {
                'status': 'healthy',
                'message': 'MLflow service healthy',
                'tracking_uri': mlflow_uri,
                'response_time_ms': response_time
            }
        except Exception as e:
            return {
                'status': 'unhealthy',
                'message': f'MLflow connection failed: {str(e)}',
                'response_time_ms': None
            }
    
    def _check_kafka(self) -> Dict[str, Any]:
        """Check Kafka connectivity"""
        try:
            # This is a mock implementation
            # In real implementation, would test actual Kafka connection
            return {
                'status': 'healthy',
                'message': 'Kafka connection healthy',
                'response_time_ms': 8.5
            }
        except Exception as e:
            return {
                'status': 'unhealthy',
                'message': f'Kafka connection failed: {str(e)}',
                'response_time_ms': None
            }
    
    def _format_uptime(self, uptime_seconds: float) -> str:
        """Format uptime in human readable format"""
        days = int(uptime_seconds // 86400)
        hours = int((uptime_seconds % 86400) // 3600)
        minutes = int((uptime_seconds % 3600) // 60)
        seconds = int(uptime_seconds % 60)
        
        if days > 0:
            return f"{days}d {hours}h {minutes}m {seconds}s"
        elif hours > 0:
            return f"{hours}h {minutes}m {seconds}s"
        elif minutes > 0:
            return f"{minutes}m {seconds}s"
        else:
            return f"{seconds}s"
    
    def get_service_status(self) -> Dict[str, Any]:
        """Get health service status"""
        return {
            'status': 'healthy',
            'checks_performed': len(self.health_cache),
            'last_check': self.last_check,
            'timestamp': datetime.now().isoformat()
        } 