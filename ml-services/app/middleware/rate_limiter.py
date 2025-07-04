#!/usr/bin/env python3
"""
🚦 Rate Limiter Middleware
Global rate limiting middleware.
"""

import logging
import time
from typing import Dict, Any
from flask import request, jsonify, current_app

logger = logging.getLogger(__name__)

class RateLimiter:
    """
    Rate Limiter Middleware
    
    Provides global rate limiting functionality.
    """
    
    def __init__(self, app=None):
        self.app = app
        self.clients = {}  # In-memory storage (use Redis in production)
        
        if app is not None:
            self.init_app(app)
    
    def init_app(self, app):
        """Initialize middleware with Flask app"""
        if not app.config.get('ENABLE_RATE_LIMITING', True):
            logger.info("🚦 Rate limiting disabled")
            return
        
        # Register middleware
        app.before_request(self.before_request)
        logger.info("🚦 Rate limiting middleware registered")
    
    def before_request(self):
        """Process request before routing"""
        # Skip rate limiting for health endpoints
        if request.endpoint in ['health.basic_health', 'health.liveness', 'health.readiness']:
            return
        
        # Get client identifier
        client_id = self.get_client_id()
        
        # Check rate limit
        if self.is_rate_limited(client_id):
            return jsonify({
                'error': 'Rate limit exceeded',
                'message': 'Too many requests',
                'retry_after': 60,
                'timestamp': time.time()
            }), 429
        
        # Record request
        self.record_request(client_id)
    
    def get_client_id(self) -> str:
        """Get client identifier"""
        # Try to get from headers first
        client_ip = request.environ.get('HTTP_X_FORWARDED_FOR', request.remote_addr)
        if client_ip:
            client_ip = client_ip.split(',')[0].strip()
        
        return client_ip or 'unknown'
    
    def is_rate_limited(self, client_id: str) -> bool:
        """Check if client is rate limited"""
        current_time = time.time()
        window = 60  # 1 minute window
        limit = 100  # 100 requests per minute
        
        # Get client requests
        if client_id not in self.clients:
            self.clients[client_id] = []
        
        client_requests = self.clients[client_id]
        
        # Clean old requests
        cutoff = current_time - window
        client_requests[:] = [req_time for req_time in client_requests if req_time > cutoff]
        
        # Check limit
        return len(client_requests) >= limit
    
    def record_request(self, client_id: str):
        """Record a request"""
        current_time = time.time()
        
        if client_id not in self.clients:
            self.clients[client_id] = []
        
        self.clients[client_id].append(current_time) 