#!/usr/bin/env python3
"""
🔐 Authentication Middleware
Professional authentication handling.
"""

import logging
from functools import wraps
from flask import request, jsonify, current_app

logger = logging.getLogger(__name__)

class AuthMiddleware:
    """
    Authentication Middleware
    
    Handles authentication for protected endpoints.
    """
    
    def __init__(self, app=None):
        self.app = app
        if app is not None:
            self.init_app(app)
    
    def init_app(self, app):
        """Initialize middleware with Flask app"""
        # In development, disable auth by default
        if not app.config.get('ENABLE_AUTH', False):
            logger.info("🔐 Authentication disabled for development")
            return
        
        # Register middleware
        app.before_request(self.before_request)
        logger.info("🔐 Authentication middleware registered")
    
    def before_request(self):
        """Process request before routing"""
        # Skip auth for health endpoints
        if request.endpoint in ['health.basic_health', 'health.liveness', 'health.readiness']:
            return
        
        # Skip auth for root endpoint
        if request.endpoint == 'root':
            return
        
        # Check authorization header
        auth_header = request.headers.get('Authorization')
        if not auth_header:
            return jsonify({
                'error': 'Missing authorization header',
                'message': 'Authorization header is required',
                'timestamp': request.timestamp if hasattr(request, 'timestamp') else None
            }), 401
        
        # Validate token (simplified for demo)
        if not self.validate_token(auth_header):
            return jsonify({
                'error': 'Invalid authorization token',
                'message': 'Please provide a valid authorization token',
                'timestamp': request.timestamp if hasattr(request, 'timestamp') else None
            }), 401
    
    def validate_token(self, auth_header: str) -> bool:
        """Validate authorization token"""
        try:
            # Simple token validation (in production, use JWT or similar)
            if not auth_header.startswith('Bearer '):
                return False
            
            token = auth_header.replace('Bearer ', '')
            
            # For demo purposes, accept any non-empty token
            # In production, validate against JWT, database, etc.
            return len(token) > 0
            
        except Exception as e:
            logger.error(f"Token validation error: {e}")
            return False

def require_auth(f):
    """Decorator to require authentication for specific endpoints"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Check if auth is enabled
        if not current_app.config.get('ENABLE_AUTH', False):
            return f(*args, **kwargs)
        
        # Check authorization
        auth_header = request.headers.get('Authorization')
        if not auth_header or not validate_auth_token(auth_header):
            return jsonify({
                'error': 'Authentication required',
                'message': 'Valid authorization token required',
                'timestamp': request.timestamp if hasattr(request, 'timestamp') else None
            }), 401
        
        return f(*args, **kwargs)
    
    return decorated_function

def validate_auth_token(auth_header: str) -> bool:
    """Standalone token validation function"""
    try:
        if not auth_header.startswith('Bearer '):
            return False
        
        token = auth_header.replace('Bearer ', '')
        return len(token) > 0
    except Exception:
        return False 