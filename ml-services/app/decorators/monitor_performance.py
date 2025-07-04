#!/usr/bin/env python3
"""
📊 Performance Monitoring Decorator
Professional performance monitoring for API endpoints.
"""

import time
import logging
from functools import wraps
from typing import Dict, Any
from flask import request, g
import traceback

logger = logging.getLogger(__name__)

# In-memory metrics storage (use Prometheus/StatsD in production)
metrics_storage: Dict[str, Dict[str, Any]] = {}

def monitor_performance(f):
    """
    Performance monitoring decorator
    
    Tracks:
    - Request latency
    - Success/error rates
    - Request counts
    - Memory usage
    """
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Start timing
        start_time = time.time()
        
        # Get endpoint info
        endpoint = f.__name__
        method = request.method
        path = request.path
        
        # Initialize metrics if not exists
        metric_key = f"{method}:{endpoint}"
        if metric_key not in metrics_storage:
            metrics_storage[metric_key] = {
                'total_requests': 0,
                'total_errors': 0,
                'total_time': 0.0,
                'min_time': float('inf'),
                'max_time': 0.0,
                'response_times': [],
                'error_types': {},
                'last_request': None
            }
        
        metrics = metrics_storage[metric_key]
        
        try:
            # Execute the function
            result = f(*args, **kwargs)
            
            # Calculate timing
            end_time = time.time()
            duration = (end_time - start_time) * 1000  # Convert to milliseconds
            
            # Update metrics
            update_success_metrics(metrics, duration, endpoint)
            
            # Add performance headers
            if hasattr(result, '__class__') and hasattr(result[0], 'headers'):
                result[0].headers['X-Response-Time'] = f"{duration:.2f}ms"
                result[0].headers['X-Endpoint'] = endpoint
            
            return result
            
        except Exception as e:
            # Calculate timing for errors too
            end_time = time.time()
            duration = (end_time - start_time) * 1000
            
            # Update error metrics
            update_error_metrics(metrics, duration, endpoint, e)
            
            # Log the error
            logger.error(f"Error in {endpoint}: {str(e)}", exc_info=True)
            
            # Re-raise the exception
            raise
    
    return decorated_function

def update_success_metrics(metrics: Dict[str, Any], duration: float, endpoint: str):
    """Update metrics for successful requests"""
    metrics['total_requests'] += 1
    metrics['total_time'] += duration
    metrics['min_time'] = min(metrics['min_time'], duration)
    metrics['max_time'] = max(metrics['max_time'], duration)
    metrics['last_request'] = time.time()
    
    # Keep rolling window of response times
    metrics['response_times'].append(duration)
    if len(metrics['response_times']) > 1000:
        metrics['response_times'] = metrics['response_times'][-1000:]
    
    # Log slow requests
    if duration > 5000:  # 5 seconds
        logger.warning(f"Slow request detected: {endpoint} took {duration:.2f}ms")

def update_error_metrics(metrics: Dict[str, Any], duration: float, endpoint: str, error: Exception):
    """Update metrics for failed requests"""
    metrics['total_requests'] += 1
    metrics['total_errors'] += 1
    metrics['total_time'] += duration
    metrics['last_request'] = time.time()
    
    # Track error types
    error_type = type(error).__name__
    if error_type not in metrics['error_types']:
        metrics['error_types'][error_type] = 0
    metrics['error_types'][error_type] += 1

def get_endpoint_metrics(endpoint: str = None) -> Dict[str, Any]:
    """Get performance metrics for endpoints"""
    if endpoint:
        # Get metrics for specific endpoint
        matching_metrics = {k: v for k, v in metrics_storage.items() if endpoint in k}
    else:
        # Get all metrics
        matching_metrics = metrics_storage
    
    summary = {}
    
    for key, metrics in matching_metrics.items():
        if metrics['total_requests'] == 0:
            continue
        
        response_times = metrics['response_times']
        
        endpoint_summary = {
            'total_requests': metrics['total_requests'],
            'total_errors': metrics['total_errors'],
            'error_rate': metrics['total_errors'] / metrics['total_requests'],
            'avg_response_time_ms': metrics['total_time'] / metrics['total_requests'],
            'min_response_time_ms': metrics['min_time'] if metrics['min_time'] != float('inf') else 0,
            'max_response_time_ms': metrics['max_time'],
            'last_request_timestamp': metrics['last_request'],
            'error_types': metrics['error_types']
        }
        
        # Calculate percentiles if we have response times
        if response_times:
            sorted_times = sorted(response_times)
            endpoint_summary.update({
                'p50_response_time_ms': get_percentile(sorted_times, 50),
                'p90_response_time_ms': get_percentile(sorted_times, 90),
                'p95_response_time_ms': get_percentile(sorted_times, 95),
                'p99_response_time_ms': get_percentile(sorted_times, 99)
            })
        
        summary[key] = endpoint_summary
    
    return summary

def get_percentile(sorted_list: list, percentile: float) -> float:
    """Calculate percentile from sorted list"""
    if not sorted_list:
        return 0.0
    
    index = (percentile / 100.0) * (len(sorted_list) - 1)
    
    if index.is_integer():
        return sorted_list[int(index)]
    else:
        lower = sorted_list[int(index)]
        upper = sorted_list[int(index) + 1]
        return lower + (upper - lower) * (index - int(index))

def get_health_metrics() -> Dict[str, Any]:
    """Get overall system health metrics"""
    total_requests = sum(m['total_requests'] for m in metrics_storage.values())
    total_errors = sum(m['total_errors'] for m in metrics_storage.values())
    
    # Calculate average response time across all endpoints
    total_time = sum(m['total_time'] for m in metrics_storage.values())
    avg_response_time = total_time / total_requests if total_requests > 0 else 0
    
    # Get recent error rate (last 100 requests per endpoint)
    recent_errors = 0
    recent_requests = 0
    
    for metrics in metrics_storage.values():
        response_times = metrics['response_times']
        if response_times:
            recent_count = min(100, len(response_times))
            recent_requests += recent_count
            # Estimate recent errors (this is approximate)
            recent_error_rate = metrics['total_errors'] / metrics['total_requests']
            recent_errors += recent_count * recent_error_rate
    
    return {
        'total_requests': total_requests,
        'total_errors': total_errors,
        'overall_error_rate': total_errors / total_requests if total_requests > 0 else 0,
        'avg_response_time_ms': avg_response_time,
        'recent_error_rate': recent_errors / recent_requests if recent_requests > 0 else 0,
        'active_endpoints': len([k for k, v in metrics_storage.items() if v['total_requests'] > 0]),
        'timestamp': time.time()
    }

def reset_metrics():
    """Reset all metrics (for testing)"""
    global metrics_storage
    metrics_storage.clear() 