#!/usr/bin/env python3
"""
🚀 ML Services Application Factory
Professional Flask application with proper MVC architecture and separation of concerns.

This follows the application factory pattern used by senior engineers at:
- Netflix, Uber, Airbnb (microservices)
- Google, Meta, Apple (scalable ML platforms)
"""

from flask import Flask
from flask_cors import CORS
import logging
import os
from datetime import datetime

# Import controllers
from controllers.fraud_controller import fraud_bp
from controllers.training_controller import training_bp
from controllers.model_controller import model_bp
from controllers.health_controller import health_bp

# Import middleware
from middleware.auth import AuthMiddleware
from middleware.rate_limiter import RateLimiter
from middleware.metrics import MetricsMiddleware

# Import configuration
from config.app_config import Config

def create_app(config_name=None):
    """
    Application Factory Pattern
    
    Creates and configures Flask application with proper structure:
    - Controllers for route handling
    - Services for business logic
    - Models for data structures
    - Middleware for cross-cutting concerns
    """
    
    # Initialize Flask app
    app = Flask(__name__)
    
    # Load configuration
    config = Config(config_name or os.getenv('FLASK_ENV', 'development'))
    app.config.from_object(config)
    
    # Enable CORS for frontend integration
    CORS(app)
    
    # Configure logging
    configure_logging(app)
    
    # Register middleware
    register_middleware(app)
    
    # Register blueprints (controllers)
    register_blueprints(app)
    
    # Register error handlers
    register_error_handlers(app)
    
    # Initialize services
    initialize_services(app)
    
    # Create root route
    create_root_route(app)
    
    app.logger.info("🚀 ML Services Application initialized successfully")
    
    return app

def configure_logging(app):
    """Configure application logging"""
    logging.basicConfig(
        level=getattr(logging, app.config.get('LOG_LEVEL', 'INFO')),
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    app.logger.info("📝 Logging configured")

def register_middleware(app):
    """Register application middleware"""
    
    # Authentication middleware
    if app.config.get('ENABLE_AUTH', False):
        AuthMiddleware(app)
        app.logger.info("🔐 Authentication middleware enabled")
    
    # Rate limiting
    if app.config.get('ENABLE_RATE_LIMITING', True):
        RateLimiter(app)
        app.logger.info("⚡ Rate limiting enabled")
    
    # Metrics collection
    if app.config.get('ENABLE_METRICS', True):
        MetricsMiddleware(app)
        app.logger.info("📊 Metrics middleware enabled")

def register_blueprints(app):
    """Register application blueprints (controllers)"""
    
    # Health and monitoring
    app.register_blueprint(health_bp, url_prefix='/')
    
    # Fraud detection API
    app.register_blueprint(fraud_bp, url_prefix='/api/v1/fraud')
    
    # Training pipeline API
    app.register_blueprint(training_bp, url_prefix='/api/v1/training')
    
    # Model management API
    app.register_blueprint(model_bp, url_prefix='/api/v1/models')
    
    app.logger.info("🎛️ All controllers registered")

def register_error_handlers(app):
    """Register global error handlers"""
    
    @app.errorhandler(400)
    def bad_request(error):
        return {
            'error': 'Bad Request',
            'message': 'Invalid request format or parameters',
            'timestamp': datetime.now().isoformat(),
            'status_code': 400
        }, 400
    
    @app.errorhandler(401)
    def unauthorized(error):
        return {
            'error': 'Unauthorized',
            'message': 'Authentication required',
            'timestamp': datetime.now().isoformat(),
            'status_code': 401
        }, 401
    
    @app.errorhandler(403)
    def forbidden(error):
        return {
            'error': 'Forbidden',
            'message': 'Insufficient permissions',
            'timestamp': datetime.now().isoformat(),
            'status_code': 403
        }, 403
    
    @app.errorhandler(404)
    def not_found(error):
        return {
            'error': 'Not Found',
            'message': 'The requested resource was not found',
            'timestamp': datetime.now().isoformat(),
            'status_code': 404
        }, 404
    
    @app.errorhandler(429)
    def rate_limit_exceeded(error):
        return {
            'error': 'Rate Limit Exceeded',
            'message': 'Too many requests. Please try again later.',
            'timestamp': datetime.now().isoformat(),
            'status_code': 429
        }, 429
    
    @app.errorhandler(500)
    def internal_error(error):
        app.logger.error(f"Internal server error: {error}")
        return {
            'error': 'Internal Server Error',
            'message': 'An unexpected error occurred',
            'timestamp': datetime.now().isoformat(),
            'status_code': 500
        }, 500
    
    app.logger.info("🚨 Error handlers registered")

def initialize_services(app):
    """Initialize core services"""
    
    # Initialize service registry
    from services.service_registry import ServiceRegistry
    service_registry = ServiceRegistry()
    
    # Register with app context
    app.service_registry = service_registry
    
    app.logger.info("🔧 Core services initialized")

def create_root_route(app):
    """Create root endpoint"""
    @app.route('/')
    def root():
        """API root with service information"""
        return {
            'service': 'ML Services Platform',
            'version': '2.0.0',
            'architecture': 'Microservices with MVC',
            'status': 'operational',
            'timestamp': datetime.now().isoformat(),
            'endpoints': {
                'fraud_detection': '/api/v1/fraud/',
                'training_pipeline': '/api/v1/training/',
                'model_management': '/api/v1/models/',
                'health': '/health',
                'metrics': '/metrics',
                'documentation': '/docs'
            },
            'features': [
                'Fraud Detection with ML',
                'Automated Training Pipeline',
                'Model Registry & Versioning',
                'Real-time Feature Store',
                'Comprehensive Monitoring',
                'Rate Limiting & Security',
                'Microservices Architecture'
            ]
        }

if __name__ == '__main__':
    """Development server entry point"""
    
    # Create application
    app = create_app()
    
    # Configuration from environment
    host = os.getenv('FLASK_HOST', '0.0.0.0')
    port = int(os.getenv('FLASK_PORT', 5000))
    debug = os.getenv('FLASK_DEBUG', 'false').lower() == 'true'
    
    app.logger.info(f"🌐 Starting ML Services Platform")
    app.logger.info(f"📍 Server: {host}:{port}")
    app.logger.info(f"🐛 Debug: {debug}")
    app.logger.info(f"🏗️ Architecture: MVC + Microservices")
    
    # Start server
    app.run(host=host, port=port, debug=debug, threaded=True) 