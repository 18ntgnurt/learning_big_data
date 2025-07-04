#!/usr/bin/env python3
"""
🚨 Fraud Prediction Model
Data model for fraud detection predictions.
"""

from datetime import datetime
from typing import Dict, Any, List, Optional
import json

class FraudPrediction:
    """
    Fraud Prediction data model
    
    Represents the result of a fraud detection prediction
    including probability, risk level, and explanations.
    """
    
    def __init__(
        self,
        transaction_id: str,
        fraud_probability: float,
        risk_level: str,
        confidence_score: float,
        model_version: str,
        prediction_time: str,
        features_used: List[str],
        explanation: Dict[str, Any],
        recommendation: Optional[str] = None,
        **kwargs
    ):
        self.transaction_id = transaction_id
        self.fraud_probability = fraud_probability
        self.risk_level = risk_level  # LOW, MEDIUM, HIGH
        self.confidence_score = confidence_score
        self.model_version = model_version
        self.prediction_time = prediction_time
        self.features_used = features_used
        self.explanation = explanation
        self.recommendation = recommendation or self._generate_recommendation()
        
        # Additional fields
        for key, value in kwargs.items():
            setattr(self, key, value)
    
    def _generate_recommendation(self) -> str:
        """Generate recommendation based on risk level"""
        if self.risk_level == 'HIGH':
            return 'BLOCK - Review transaction immediately'
        elif self.risk_level == 'MEDIUM':
            return 'REVIEW - Additional verification recommended'
        else:
            return 'APPROVE - Transaction appears legitimate'
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'FraudPrediction':
        """Create FraudPrediction from dictionary"""
        return cls(**data)
    
    @classmethod
    def from_json(cls, json_str: str) -> 'FraudPrediction':
        """Create FraudPrediction from JSON string"""
        data = json.loads(json_str)
        return cls.from_dict(data)
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert FraudPrediction to dictionary"""
        return {
            'transaction_id': self.transaction_id,
            'fraud_probability': self.fraud_probability,
            'risk_level': self.risk_level,
            'confidence_score': self.confidence_score,
            'model_version': self.model_version,
            'prediction_time': self.prediction_time,
            'features_used': self.features_used,
            'explanation': self.explanation,
            'recommendation': self.recommendation
        }
    
    def to_json(self) -> str:
        """Convert FraudPrediction to JSON string"""
        return json.dumps(self.to_dict(), indent=2)
    
    def is_fraud_likely(self) -> bool:
        """Check if fraud is likely based on probability threshold"""
        return self.fraud_probability > 0.5
    
    def is_high_risk(self) -> bool:
        """Check if prediction indicates high risk"""
        return self.risk_level == 'HIGH'
    
    def should_block(self) -> bool:
        """Check if transaction should be blocked"""
        return self.risk_level == 'HIGH' and self.confidence_score > 0.7
    
    def get_risk_score(self) -> int:
        """Get numeric risk score (0-100)"""
        return int(self.fraud_probability * 100)
    
    def __str__(self) -> str:
        return f"FraudPrediction({self.transaction_id}, {self.risk_level}, {self.fraud_probability:.3f})"
    
    def __repr__(self) -> str:
        return self.__str__() 