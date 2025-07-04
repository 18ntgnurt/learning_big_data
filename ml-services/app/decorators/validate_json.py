#!/usr/bin/env python3
"""
✅ JSON Validation Decorator
Professional input validation for API endpoints.
"""

import json
import logging
from functools import wraps
from typing import Dict, Any, List, Optional
from flask import request, jsonify

logger = logging.getLogger(__name__)

def validate_json(required_fields: Optional[List[str]] = None, max_size: int = 1024*1024):
    """
    JSON validation decorator
    
    Args:
        required_fields: List of required field names
        max_size: Maximum JSON payload size in bytes
    """
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            # Check content type
            if not request.is_json:
                return jsonify({
                    'error': 'Invalid content type',
                    'message': 'Content-Type must be application/json',
                    'expected': 'application/json',
                    'received': request.content_type
                }), 400
            
            # Check payload size
            if request.content_length and request.content_length > max_size:
                return jsonify({
                    'error': 'Payload too large',
                    'message': f'Maximum payload size is {max_size} bytes',
                    'received_size': request.content_length
                }), 413
            
            # Parse JSON
            try:
                json_data = request.get_json(force=True)
            except Exception as e:
                logger.warning(f"Invalid JSON received: {e}")
                return jsonify({
                    'error': 'Invalid JSON',
                    'message': 'Request body must be valid JSON',
                    'details': str(e)
                }), 400
            
            # Check if JSON is empty
            if json_data is None:
                return jsonify({
                    'error': 'Empty JSON',
                    'message': 'Request body cannot be empty'
                }), 400
            
            # Validate required fields
            if required_fields:
                missing_fields = []
                for field in required_fields:
                    if field not in json_data or json_data[field] is None:
                        missing_fields.append(field)
                
                if missing_fields:
                    return jsonify({
                        'error': 'Missing required fields',
                        'missing_fields': missing_fields,
                        'required_fields': required_fields
                    }), 400
            
            # Validate field types and values
            validation_errors = validate_field_types(json_data)
            if validation_errors:
                return jsonify({
                    'error': 'Validation errors',
                    'validation_errors': validation_errors
                }), 400
            
            return f(*args, **kwargs)
        
        return decorated_function
    
    if callable(required_fields):
        # Decorator used without arguments
        func = required_fields
        required_fields = None
        return decorator(func)
    
    return decorator

def validate_field_types(data: Dict[str, Any]) -> List[str]:
    """Validate field types and common constraints"""
    errors = []
    
    # Common field validations
    validations = {
        'amount': lambda x: isinstance(x, (int, float)) and x >= 0,
        'transaction_id': lambda x: isinstance(x, str) and len(x) > 0,
        'customer_id': lambda x: isinstance(x, str) and len(x) > 0,
        'merchant_id': lambda x: isinstance(x, str) and len(x) > 0,
        'email': lambda x: isinstance(x, str) and '@' in x,
        'phone': lambda x: isinstance(x, str) and len(x) >= 10,
        'timestamp': lambda x: isinstance(x, str),
        'currency': lambda x: isinstance(x, str) and len(x) == 3,
        'model_name': lambda x: isinstance(x, str) and len(x) > 0,
        'algorithm': lambda x: isinstance(x, str) and len(x) > 0
    }
    
    for field, value in data.items():
        if field in validations:
            try:
                if not validations[field](value):
                    errors.append(f"Invalid value for field '{field}': {value}")
            except Exception as e:
                errors.append(f"Validation error for field '{field}': {str(e)}")
    
    # Custom validations
    if 'amount' in data:
        amount = data['amount']
        if isinstance(amount, (int, float)):
            if amount < 0:
                errors.append("Amount must be positive")
            elif amount > 1000000:  # $1M limit
                errors.append("Amount exceeds maximum limit of $1,000,000")
    
    if 'fraud_probability' in data:
        prob = data['fraud_probability']
        if isinstance(prob, (int, float)):
            if not 0 <= prob <= 1:
                errors.append("Fraud probability must be between 0 and 1")
    
    if 'risk_level' in data:
        risk_level = data['risk_level']
        if isinstance(risk_level, str):
            valid_levels = ['LOW', 'MEDIUM', 'HIGH']
            if risk_level not in valid_levels:
                errors.append(f"Risk level must be one of: {valid_levels}")
    
    return errors

def sanitize_json_data(data: Dict[str, Any]) -> Dict[str, Any]:
    """Sanitize JSON data for security"""
    sanitized = {}
    
    for key, value in data.items():
        # Remove potentially dangerous keys
        if key.startswith('_'):
            continue
        
        # Sanitize string values
        if isinstance(value, str):
            # Remove null bytes and control characters
            value = ''.join(char for char in value if ord(char) >= 32 or char in '\t\n\r')
            # Limit string length
            value = value[:1000]
        
        # Sanitize nested dictionaries
        elif isinstance(value, dict):
            value = sanitize_json_data(value)
        
        # Sanitize lists
        elif isinstance(value, list):
            value = [sanitize_json_data(item) if isinstance(item, dict) 
                    else item for item in value[:100]]  # Limit list size
        
        sanitized[key] = value
    
    return sanitized 