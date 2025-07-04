#!/usr/bin/env python3
"""
💳 Transaction Model
Data model for financial transactions.
"""

from datetime import datetime
from typing import Dict, Any, Optional, List
import uuid
import json

class Transaction:
    """
    Transaction data model
    
    Represents a financial transaction with all necessary fields
    for fraud detection analysis.
    """
    
    def __init__(
        self,
        transaction_id: str,
        customer_id: str,
        merchant_id: str,
        amount: float,
        currency: str = 'USD',
        transaction_type: str = 'purchase',
        timestamp: Optional[str] = None,
        merchant_category: Optional[str] = None,
        payment_method: Optional[str] = None,
        location: Optional[Dict] = None,
        **kwargs
    ):
        self.transaction_id = transaction_id
        self.customer_id = customer_id
        self.merchant_id = merchant_id
        self.amount = amount
        self.currency = currency
        self.transaction_type = transaction_type
        self.timestamp = timestamp or datetime.now().isoformat()
        self.merchant_category = merchant_category or 'general'
        self.payment_method = payment_method or 'card'
        self.location = location or {}
        
        # Additional fields
        for key, value in kwargs.items():
            setattr(self, key, value)
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'Transaction':
        """Create Transaction from dictionary"""
        return cls(**data)
    
    @classmethod
    def from_json(cls, json_str: str) -> 'Transaction':
        """Create Transaction from JSON string"""
        data = json.loads(json_str)
        return cls.from_dict(data)
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert Transaction to dictionary"""
        return {
            'transaction_id': self.transaction_id,
            'customer_id': self.customer_id,
            'merchant_id': self.merchant_id,
            'amount': self.amount,
            'currency': self.currency,
            'transaction_type': self.transaction_type,
            'timestamp': self.timestamp,
            'merchant_category': self.merchant_category,
            'payment_method': self.payment_method,
            'location': self.location
        }
    
    def to_json(self) -> str:
        """Convert Transaction to JSON string"""
        return json.dumps(self.to_dict(), indent=2)
    
    def validate(self) -> bool:
        """Validate transaction data"""
        required_fields = ['transaction_id', 'customer_id', 'merchant_id', 'amount']
        
        for field in required_fields:
            if not hasattr(self, field) or getattr(self, field) is None:
                return False
        
        # Amount validation
        if not isinstance(self.amount, (int, float)) or self.amount < 0:
            return False
        
        return True
    
    def get_validation_errors(self) -> List[str]:
        """Get list of validation errors"""
        errors = []
        
        required_fields = ['transaction_id', 'customer_id', 'merchant_id', 'amount']
        for field in required_fields:
            if not hasattr(self, field) or getattr(self, field) is None:
                errors.append(f"Missing required field: {field}")
        
        if hasattr(self, 'amount'):
            if not isinstance(self.amount, (int, float)):
                errors.append("Amount must be a number")
            elif self.amount < 0:
                errors.append("Amount must be positive")
        
        return errors
    
    def __str__(self) -> str:
        return f"Transaction({self.transaction_id}, ${self.amount}, {self.customer_id})"
    
    def __repr__(self) -> str:
        return self.__str__() 