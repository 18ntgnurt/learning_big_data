#!/usr/bin/env python3
"""
✅ Validation Service
Data validation and sanitization service.
"""

import logging
import re
from datetime import datetime
from typing import Dict, Any, List, Optional, Union

logger = logging.getLogger(__name__)

class ValidationService:
    """
    Validation Service
    
    Provides comprehensive data validation including:
    - Transaction validation
    - Input sanitization
    - Business rule validation
    - Data type validation
    """
    
    def __init__(self, config: Dict):
        self.config = config
        self.strict_validation = config.get('enable_strict_validation', True)
        self.max_transaction_amount = config.get('max_transaction_amount', 1000000)
        self.required_fields = config.get('required_fields', [])
        
        logger.info("✅ Validation Service initialized")
    
    def validate_transaction(self, transaction_data: Dict[str, Any]) -> Dict[str, Any]:
        """Validate transaction data"""
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': [],
            'sanitized_data': {}
        }
        
        try:
            # Required field validation
            missing_fields = self._check_required_fields(transaction_data)
            if missing_fields:
                validation_result['errors'].extend(
                    [f"Missing required field: {field}" for field in missing_fields]
                )
                validation_result['valid'] = False
            
            # Data type validation
            type_errors = self._validate_data_types(transaction_data)
            if type_errors:
                validation_result['errors'].extend(type_errors)
                validation_result['valid'] = False
            
            # Business rule validation
            business_errors = self._validate_business_rules(transaction_data)
            if business_errors:
                validation_result['errors'].extend(business_errors)
                validation_result['valid'] = False
            
            # Data sanitization
            validation_result['sanitized_data'] = self._sanitize_transaction_data(transaction_data)
            
            # Generate warnings
            warnings = self._generate_warnings(transaction_data)
            validation_result['warnings'].extend(warnings)
            
        except Exception as e:
            logger.error(f"Transaction validation failed: {e}")
            validation_result['valid'] = False
            validation_result['errors'].append(f"Validation error: {str(e)}")
        
        return validation_result
    
    def _check_required_fields(self, data: Dict[str, Any]) -> List[str]:
        """Check for missing required fields"""
        missing_fields = []
        
        # Default required fields for transactions
        required_fields = ['transaction_id', 'customer_id', 'merchant_id', 'amount']
        
        # Add configured required fields
        required_fields.extend(self.required_fields)
        
        for field in required_fields:
            if field not in data or data[field] is None or data[field] == '':
                missing_fields.append(field)
        
        return missing_fields
    
    def _validate_data_types(self, data: Dict[str, Any]) -> List[str]:
        """Validate data types"""
        errors = []
        
        # Amount validation
        if 'amount' in data:
            if not isinstance(data['amount'], (int, float)):
                errors.append("Amount must be a number")
            elif data['amount'] < 0:
                errors.append("Amount must be positive")
            elif data['amount'] > self.max_transaction_amount:
                errors.append(f"Amount exceeds maximum limit of ${self.max_transaction_amount:,.2f}")
        
        # String field validation
        string_fields = ['transaction_id', 'customer_id', 'merchant_id', 'currency', 
                        'payment_method', 'merchant_category']
        
        for field in string_fields:
            if field in data and not isinstance(data[field], str):
                errors.append(f"{field} must be a string")
        
        # Currency validation
        if 'currency' in data:
            currency = data['currency']
            if isinstance(currency, str) and len(currency) != 3:
                errors.append("Currency must be a 3-letter ISO code")
            if currency and not re.match(r'^[A-Z]{3}$', currency):
                errors.append("Currency must be uppercase letters only")
        
        # Email validation (if present)
        if 'email' in data:
            email = data['email']
            if email and not self._validate_email(email):
                errors.append("Invalid email format")
        
        # Phone validation (if present)
        if 'phone' in data:
            phone = data['phone']
            if phone and not self._validate_phone(phone):
                errors.append("Invalid phone number format")
        
        return errors
    
    def _validate_business_rules(self, data: Dict[str, Any]) -> List[str]:
        """Validate business rules"""
        errors = []
        
        # Transaction amount rules
        if 'amount' in data and isinstance(data['amount'], (int, float)):
            amount = data['amount']
            
            # Minimum amount check
            if amount < 0.01:
                errors.append("Transaction amount too small (minimum $0.01)")
            
            # Suspicious large amounts
            if amount > 50000:
                # This is a warning, not an error, but we'll include it here
                pass
        
        # Currency rules
        if 'currency' in data:
            currency = data['currency']
            valid_currencies = ['USD', 'EUR', 'GBP', 'JPY', 'CAD', 'AUD']
            if currency and currency not in valid_currencies:
                if self.strict_validation:
                    errors.append(f"Unsupported currency: {currency}")
        
        # Merchant category rules
        if 'merchant_category' in data:
            category = data['merchant_category']
            if category:
                # Validate merchant category format
                if not re.match(r'^[a-zA-Z_]+$', category):
                    errors.append("Merchant category must contain only letters and underscores")
        
        # Payment method validation
        if 'payment_method' in data:
            payment_method = data['payment_method']
            valid_methods = ['card', 'bank_transfer', 'digital_wallet', 'cash', 'check']
            if payment_method and payment_method not in valid_methods:
                errors.append(f"Invalid payment method: {payment_method}")
        
        return errors
    
    def _sanitize_transaction_data(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Sanitize transaction data"""
        sanitized = {}
        
        for key, value in data.items():
            if isinstance(value, str):
                # Remove potentially dangerous characters
                sanitized_value = self._sanitize_string(value)
                sanitized[key] = sanitized_value
            elif isinstance(value, (int, float)):
                # Ensure numeric values are within reasonable bounds
                if key == 'amount':
                    sanitized[key] = max(0, min(value, self.max_transaction_amount))
                else:
                    sanitized[key] = value
            elif isinstance(value, dict):
                # Recursively sanitize nested dictionaries
                sanitized[key] = self._sanitize_transaction_data(value)
            elif isinstance(value, list):
                # Sanitize lists
                sanitized[key] = [
                    self._sanitize_string(item) if isinstance(item, str) else item
                    for item in value[:100]  # Limit list size
                ]
            else:
                sanitized[key] = value
        
        return sanitized
    
    def _sanitize_string(self, value: str) -> str:
        """Sanitize string value"""
        if not isinstance(value, str):
            return value
        
        # Remove null bytes and control characters
        sanitized = ''.join(char for char in value if ord(char) >= 32 or char in '\t\n\r')
        
        # Limit string length
        sanitized = sanitized[:1000]
        
        # Strip whitespace
        sanitized = sanitized.strip()
        
        return sanitized
    
    def _generate_warnings(self, data: Dict[str, Any]) -> List[str]:
        """Generate validation warnings"""
        warnings = []
        
        # Large transaction warning
        if 'amount' in data and isinstance(data['amount'], (int, float)):
            amount = data['amount']
            if amount > 10000:
                warnings.append(f"Large transaction amount: ${amount:,.2f}")
        
        # Missing optional but recommended fields
        recommended_fields = ['timestamp', 'merchant_category', 'payment_method']
        for field in recommended_fields:
            if field not in data or not data[field]:
                warnings.append(f"Recommended field '{field}' is missing")
        
        # Unusual merchant categories
        if 'merchant_category' in data:
            category = data['merchant_category']
            high_risk_categories = ['gambling', 'crypto', 'adult_entertainment']
            if category and category.lower() in high_risk_categories:
                warnings.append(f"High-risk merchant category: {category}")
        
        return warnings
    
    def _validate_email(self, email: str) -> bool:
        """Validate email format"""
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        return re.match(email_pattern, email) is not None
    
    def _validate_phone(self, phone: str) -> bool:
        """Validate phone number format"""
        # Remove common separators
        cleaned_phone = re.sub(r'[\s\-\(\)]', '', phone)
        
        # Check if it's a valid phone number (10-15 digits)
        phone_pattern = r'^\+?[1-9]\d{9,14}$'
        return re.match(phone_pattern, cleaned_phone) is not None
    
    def validate_fraud_prediction_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """Validate fraud prediction request"""
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        try:
            # Check for transaction data
            if 'transaction' not in request_data:
                validation_result['errors'].append("Missing transaction data")
                validation_result['valid'] = False
                return validation_result
            
            # Validate transaction data
            transaction_validation = self.validate_transaction(request_data['transaction'])
            
            if not transaction_validation['valid']:
                validation_result['valid'] = False
                validation_result['errors'].extend(transaction_validation['errors'])
            
            validation_result['warnings'].extend(transaction_validation['warnings'])
            
        except Exception as e:
            logger.error(f"Fraud prediction request validation failed: {e}")
            validation_result['valid'] = False
            validation_result['errors'].append(f"Validation error: {str(e)}")
        
        return validation_result
    
    def validate_training_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """Validate training request"""
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        try:
            # Required fields for training
            required_fields = ['model_name', 'algorithm']
            missing_fields = []
            
            for field in required_fields:
                if field not in request_data or not request_data[field]:
                    missing_fields.append(field)
            
            if missing_fields:
                validation_result['errors'].extend(
                    [f"Missing required field: {field}" for field in missing_fields]
                )
                validation_result['valid'] = False
            
            # Validate model name
            if 'model_name' in request_data:
                model_name = request_data['model_name']
                if not re.match(r'^[a-zA-Z0-9_-]+$', model_name):
                    validation_result['errors'].append(
                        "Model name must contain only letters, numbers, hyphens, and underscores"
                    )
            
            # Validate algorithm
            if 'algorithm' in request_data:
                algorithm = request_data['algorithm']
                valid_algorithms = ['random_forest', 'logistic_regression', 'xgboost', 'neural_network']
                if algorithm not in valid_algorithms:
                    validation_result['errors'].append(f"Unsupported algorithm: {algorithm}")
            
        except Exception as e:
            logger.error(f"Training request validation failed: {e}")
            validation_result['valid'] = False
            validation_result['errors'].append(f"Validation error: {str(e)}")
        
        return validation_result
    
    def get_service_status(self) -> Dict[str, Any]:
        """Get validation service status"""
        return {
            'status': 'healthy',
            'strict_validation': self.strict_validation,
            'max_transaction_amount': self.max_transaction_amount,
            'timestamp': datetime.now().isoformat()
        } 