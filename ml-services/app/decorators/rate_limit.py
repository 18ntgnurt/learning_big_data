#!/usr/bin/env python3
"""
🚦 Rate Limiting Decorator
Professional rate limiting for API endpoints.
"""

import time
import logging
from functools import wraps
from typing import Dict, Any
from flask import request, jsonify, current_app
import hashlib

logger = logging.getLogger(__name__)

# In-memory rate limit storage (use Redis in production)
rate_limit_storage: Dict[str, Dict[str, Any]] = {}

def rate_limit(limit: int, per_second: int):
    """
    Rate limiting decorator
    
    Args:
        limit: Maximum number of requests
        per_second: Time window in seconds
    """
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            # Check if rate limiting is enabled
            if not current_app.config.get('ENABLE_RATE_LIMITING', True):
                return f(*args, **kwargs)
            
            # Get client identifier
            client_id = get_client_identifier()
            
            # Create rate limit key
            rate_limit_key = f"{f.__name__}:{client_id}"
            
            # Check rate limit
            if is_rate_limited(rate_limit_key, limit, per_second):
                logger.warning(f"Rate limit exceeded for {client_id} on {f.__name__}")
                return jsonify({
                    'error': 'Rate limit exceeded',
                    'message': f'Maximum {limit} requests per {per_second} seconds',
                    'retry_after': per_second,
                    'timestamp': time.time()
                }), 429
            
            # Record the request
            record_request(rate_limit_key)
            
            return f(*args, **kwargs)
        
        return decorated_function
    return decorator

def get_client_identifier() -> str:
    """Get unique identifier for the client"""
    # In production, use proper authentication tokens
    
    # Try to get from headers
    auth_header = request.headers.get('Authorization', '')
    if auth_header:
        # Use hash of auth token
        return hashlib.md5(auth_header.encode()).hexdigest()[:16]
    
    # Fall back to IP address
    client_ip = request.environ.get('HTTP_X_FORWARDED_FOR', request.remote_addr)
    if client_ip:
        # Handle comma-separated IPs from load balancers
        client_ip = client_ip.split(',')[0].strip()
    
    return client_ip or 'unknown'

def is_rate_limited(key: str, limit: int, window: int) -> bool:
    """Check if the client is rate limited"""
    current_time = time.time()
    
    # Get or create rate limit entry
    if key not in rate_limit_storage:
        rate_limit_storage[key] = {
            'count': 0,
            'window_start': current_time,
            'requests': []
        }
    
    entry = rate_limit_storage[key]
    
    # Clean old requests outside the window
    cutoff_time = current_time - window
    entry['requests'] = [req_time for req_time in entry['requests'] if req_time > cutoff_time]
    
    # Check if limit is exceeded
    return len(entry['requests']) >= limit

def record_request(key: str):
    """Record a request for rate limiting"""
    current_time = time.time()
    
    if key not in rate_limit_storage:
        rate_limit_storage[key] = {
            'count': 0,
            'window_start': current_time,
            'requests': []
        }
    
    entry = rate_limit_storage[key]
    entry['requests'].append(current_time)
    entry['count'] += 1

def get_rate_limit_status(client_id: str, endpoint: str) -> Dict[str, Any]:
    """Get current rate limit status for a client/endpoint"""
    key = f"{endpoint}:{client_id}"
    
    if key not in rate_limit_storage:
        return {
            'requests_made': 0,
            'requests_remaining': float('inf'),
            'reset_time': None
        }
    
    entry = rate_limit_storage[key]
    current_time = time.time()
    
    # Clean old requests
    window = 3600  # Default 1 hour window
    cutoff_time = current_time - window
    entry['requests'] = [req_time for req_time in entry['requests'] if req_time > cutoff_time]
    
    return {
        'requests_made': len(entry['requests']),
        'requests_remaining': max(0, 100 - len(entry['requests'])),  # Default limit
        'reset_time': min(entry['requests']) + window if entry['requests'] else None
    }

def clear_rate_limits():
    """Clear all rate limits (for testing)"""
    global rate_limit_storage
    rate_limit_storage.clear() 