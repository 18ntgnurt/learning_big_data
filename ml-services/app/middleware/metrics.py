#!/usr/bin/env python3
"""
📊 Metrics Middleware
Application metrics collection.
"""

import logging
import time
from flask import request, current_app

logger = logging.getLogger(__name__)

class MetricsMiddleware:
    """
    Metrics Middleware
    
    Collects application metrics for monitoring.
    """
    
    def __init__(self, app=None):
        self.app = app
        self.metrics = {
            'requests_total': 0,
            'requests_by_endpoint': {},
            'response_times': [],
            'errors_total': 0
        }
        
        if app is not None:
            self.init_app(app)
    
    def init_app(self, app):
        """Initialize middleware with Flask app"""
        if not app.config.get('ENABLE_METRICS', True):
            logger.info("📊 Metrics collection disabled")
            return
        
        # Register middleware
        app.before_request(self.before_request)
        app.after_request(self.after_request)
        logger.info("📊 Metrics middleware registered")
    
    def before_request(self):
        """Record request start time"""
        request.start_time = time.time()
    
    def after_request(self, response):
        """Record request completion metrics"""
        try:
            # Calculate response time
            if hasattr(request, 'start_time'):
                response_time = (time.time() - request.start_time) * 1000
                self.metrics['response_times'].append(response_time)
                
                # Keep only recent response times
                if len(self.metrics['response_times']) > 1000:
                    self.metrics['response_times'] = self.metrics['response_times'][-1000:]
            
            # Count total requests
            self.metrics['requests_total'] += 1
            
            # Count by endpoint
            endpoint = request.endpoint or 'unknown'
            if endpoint not in self.metrics['requests_by_endpoint']:
                self.metrics['requests_by_endpoint'][endpoint] = 0
            self.metrics['requests_by_endpoint'][endpoint] += 1
            
            # Count errors
            if response.status_code >= 400:
                self.metrics['errors_total'] += 1
            
        except Exception as e:
            logger.error(f"Metrics collection error: {e}")
        
        return response
    
    def get_metrics(self):
        """Get collected metrics"""
        response_times = self.metrics['response_times']
        
        metrics = {
            'requests_total': self.metrics['requests_total'],
            'errors_total': self.metrics['errors_total'],
            'requests_by_endpoint': self.metrics['requests_by_endpoint'],
            'avg_response_time_ms': sum(response_times) / len(response_times) if response_times else 0,
            'error_rate': self.metrics['errors_total'] / max(1, self.metrics['requests_total'])
        }
        
        return metrics 