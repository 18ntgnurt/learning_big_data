#!/usr/bin/env python3
"""
🏥 Health & Monitoring Controller
Professional route handling for system health checks and monitoring.

This controller handles:
- Application health checks
- Service status monitoring
- System metrics
- Dependency checks
"""

from flask import Blueprint, jsonify, current_app
import logging
from datetime import datetime
import psutil
import os
from typing import Dict

# Import services
from services.health_service import HealthService

# Import decorators
from decorators.monitor_performance import monitor_performance

# Create blueprint
health_bp = Blueprint('health', __name__)
logger = logging.getLogger(__name__)

# Initialize services
health_service = None

def get_services():
    """Get services from application context"""
    global health_service
    
    if health_service is None:
        service_registry = current_app.service_registry
        health_service = service_registry.get_service('health_service')
    
    return health_service

@health_bp.route('/health', methods=['GET'])
@monitor_performance
def health_check():
    """
    Comprehensive health check for load balancers and monitoring systems
    
    Returns detailed health status including:
    - Overall application health
    - Individual service status
    - Database connectivity
    - External dependencies
    """
    try:
        # Get services
        health_svc = get_services()
        
        # Perform comprehensive health check
        health_status = health_svc.comprehensive_health_check()
        
        # Determine HTTP status code based on health
        status_code = 200 if health_status['status'] == 'healthy' else 503
        
        return jsonify(health_status), status_code
        
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return jsonify({
            'status': 'unhealthy',
            'error': 'Health check service failure',
            'message': str(e),
            'timestamp': datetime.now().isoformat(),
            'components': {
                'application': 'unhealthy',
                'database': 'unknown',
                'cache': 'unknown',
                'external_services': 'unknown'
            }
        }), 503

@health_bp.route('/health/live', methods=['GET'])
def liveness_probe():
    """
    Kubernetes liveness probe endpoint
    
    Simple check to determine if the application is running
    """
    try:
        return jsonify({
            'status': 'alive',
            'timestamp': datetime.now().isoformat(),
            'pid': os.getpid()
        }), 200
        
    except Exception as e:
        logger.error(f"Liveness probe failed: {e}")
        return jsonify({
            'status': 'dead',
            'error': str(e),
            'timestamp': datetime.now().isoformat()
        }), 503

@health_bp.route('/health/ready', methods=['GET'])
@monitor_performance
def readiness_probe():
    """
    Kubernetes readiness probe endpoint
    
    Check if the application is ready to serve traffic
    """
    try:
        # Get services
        health_svc = get_services()
        
        # Check readiness
        readiness_status = health_svc.readiness_check()
        
        status_code = 200 if readiness_status['ready'] else 503
        
        return jsonify(readiness_status), status_code
        
    except Exception as e:
        logger.error(f"Readiness probe failed: {e}")
        return jsonify({
            'ready': False,
            'error': 'Readiness check failure',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 503

@health_bp.route('/metrics', methods=['GET'])
@monitor_performance
def system_metrics():
    """
    System performance metrics
    
    Returns:
    - CPU usage
    - Memory usage
    - Disk usage
    - Network statistics
    - Application metrics
    """
    try:
        # Get system metrics
        cpu_percent = psutil.cpu_percent(interval=1)
        memory = psutil.virtual_memory()
        disk = psutil.disk_usage('/')
        
        # Get application metrics
        health_svc = get_services()
        app_metrics = health_svc.get_application_metrics()
        
        metrics = {
            'system': {
                'cpu': {
                    'usage_percent': cpu_percent,
                    'count': psutil.cpu_count(),
                    'count_logical': psutil.cpu_count(logical=True)
                },
                'memory': {
                    'total_bytes': memory.total,
                    'available_bytes': memory.available,
                    'used_bytes': memory.used,
                    'usage_percent': memory.percent
                },
                'disk': {
                    'total_bytes': disk.total,
                    'free_bytes': disk.free,
                    'used_bytes': disk.used,
                    'usage_percent': (disk.used / disk.total) * 100
                }
            },
            'application': app_metrics,
            'timestamp': datetime.now().isoformat()
        }
        
        return jsonify(metrics), 200
        
    except Exception as e:
        logger.error(f"Failed to get system metrics: {e}")
        return jsonify({
            'error': 'Metrics unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@health_bp.route('/metrics/prometheus', methods=['GET'])
def prometheus_metrics():
    """
    Prometheus-compatible metrics endpoint
    
    Returns metrics in Prometheus format for scraping
    """
    try:
        from prometheus_client import generate_latest, CONTENT_TYPE_LATEST
        
        # Generate Prometheus metrics
        metrics_output = generate_latest()
        
        return metrics_output, 200, {'Content-Type': CONTENT_TYPE_LATEST}
        
    except ImportError:
        return jsonify({
            'error': 'Prometheus client not available',
            'message': 'Install prometheus_client package for Prometheus metrics',
            'timestamp': datetime.now().isoformat()
        }), 503
    except Exception as e:
        logger.error(f"Failed to generate Prometheus metrics: {e}")
        return jsonify({
            'error': 'Prometheus metrics unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@health_bp.route('/status', methods=['GET'])
@monitor_performance
def service_status():
    """
    Detailed service status information
    
    Returns:
    - Individual service status
    - Version information
    - Configuration details
    - Uptime statistics
    """
    try:
        # Get services
        health_svc = get_services()
        
        # Get detailed status
        status = health_svc.get_detailed_service_status()
        
        return jsonify(status), 200
        
    except Exception as e:
        logger.error(f"Failed to get service status: {e}")
        return jsonify({
            'error': 'Service status unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@health_bp.route('/status/dependencies', methods=['GET'])
@monitor_performance
def dependency_status():
    """
    Check status of external dependencies
    
    Returns:
    - Database connectivity
    - Redis connectivity
    - MLflow availability
    - Kafka connectivity
    - External API status
    """
    try:
        # Get services
        health_svc = get_services()
        
        # Check dependencies
        dependencies = health_svc.check_dependencies()
        
        # Determine overall status
        all_healthy = all(
            dep['status'] == 'healthy' 
            for dep in dependencies.values()
        )
        
        response = {
            'overall_status': 'healthy' if all_healthy else 'degraded',
            'dependencies': dependencies,
            'timestamp': datetime.now().isoformat()
        }
        
        status_code = 200 if all_healthy else 503
        
        return jsonify(response), status_code
        
    except Exception as e:
        logger.error(f"Failed to check dependencies: {e}")
        return jsonify({
            'overall_status': 'unhealthy',
            'error': 'Dependency check failure',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 503

@health_bp.route('/info', methods=['GET'])
def application_info():
    """
    Application information endpoint
    
    Returns:
    - Application version
    - Build information
    - Environment details
    - Configuration summary
    """
    try:
        app_info = {
            'application': {
                'name': 'ML Services Platform',
                'version': '2.0.0',
                'architecture': 'Microservices with MVC',
                'python_version': os.sys.version,
                'environment': os.getenv('FLASK_ENV', 'production')
            },
            'build': {
                'timestamp': datetime.now().isoformat(),
                'platform': os.name,
                'architecture': os.uname().machine if hasattr(os, 'uname') else 'unknown'
            },
            'configuration': {
                'debug_mode': current_app.debug,
                'testing_mode': current_app.testing,
                'max_content_length': current_app.config.get('MAX_CONTENT_LENGTH'),
                'rate_limiting_enabled': current_app.config.get('ENABLE_RATE_LIMITING', True)
            },
            'endpoints': {
                'fraud_detection': '/api/v1/fraud/',
                'training_pipeline': '/api/v1/training/',
                'model_management': '/api/v1/models/',
                'health': '/health',
                'metrics': '/metrics'
            }
        }
        
        return jsonify(app_info), 200
        
    except Exception as e:
        logger.error(f"Failed to get application info: {e}")
        return jsonify({
            'error': 'Application info unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500 