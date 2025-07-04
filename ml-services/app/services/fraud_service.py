#!/usr/bin/env python3
"""
🔍 Fraud Detection Service
Business logic for fraud detection and prediction.
"""

import logging
import time
from datetime import datetime
from typing import Dict, List, Any
import uuid
import json

# ML libraries
import numpy as np
from sklearn.ensemble import RandomForestClassifier, IsolationForest
from sklearn.preprocessing import StandardScaler

logger = logging.getLogger(__name__)

class FraudService:
    """
    Fraud Detection Service
    
    Handles all fraud detection business logic including:
    - Model loading and management
    - Feature engineering
    - Prediction processing
    - Performance monitoring
    """
    
    def __init__(self, config: Dict):
        self.config = config
        self.model = None
        self.scaler = None
        self.performance_metrics = {
            'total_predictions': 0,
            'fraud_detected': 0,
            'processing_times': [],
            'model_scores': []
        }
        
        # Initialize fallback model
        self._setup_fallback_model()
        
        logger.info("🔍 Fraud Detection Service initialized")
    
    def _setup_fallback_model(self):
        """Setup fallback model when MLflow is not available"""
        logger.info("Setting up fallback fraud detection model")
        
        # Create simple ensemble model
        rf_model = RandomForestClassifier(n_estimators=10, max_depth=5, random_state=42)
        isolation_model = IsolationForest(contamination=0.1, random_state=42)
        
        # Generate synthetic training data
        np.random.seed(42)
        n_samples = 1000
        
        # Create features that correlate with fraud
        normal_features = np.random.normal(0, 1, (int(n_samples * 0.95), 8))
        fraud_features = np.random.normal(2, 1.5, (int(n_samples * 0.05), 8))
        
        X = np.vstack([normal_features, fraud_features])
        y = np.hstack([np.zeros(len(normal_features)), np.ones(len(fraud_features))])
        
        # Train models
        rf_model.fit(X, y)
        isolation_model.fit(X)
        
        self.model = {
            'random_forest': rf_model,
            'isolation_forest': isolation_model,
            'scaler': StandardScaler().fit(X),
            'version': '1.0.0-fallback',
            'created_at': datetime.now().isoformat()
        }
        
        logger.info("Fallback model setup complete")
    
    def predict_fraud(self, transaction: 'Transaction') -> 'FraudPrediction':
        """Main fraud prediction method"""
        start_time = time.time()
        
        try:
            # Extract features
            features = self._engineer_features(transaction)
            
            # Make prediction
            prediction_result = self._make_prediction(features)
            
            # Determine risk level
            fraud_probability = prediction_result['fraud_probability']
            if fraud_probability > 0.7:
                risk_level = 'HIGH'
            elif fraud_probability > 0.3:
                risk_level = 'MEDIUM'
            else:
                risk_level = 'LOW'
            
            # Create prediction object
            from models.fraud_prediction import FraudPrediction
            prediction = FraudPrediction(
                transaction_id=transaction.transaction_id,
                fraud_probability=fraud_probability,
                risk_level=risk_level,
                confidence_score=prediction_result['confidence'],
                model_version=self.model.get('version', 'unknown'),
                prediction_time=datetime.now().isoformat(),
                features_used=list(features.keys()),
                explanation=self._generate_explanation(features, prediction_result)
            )
            
            # Update metrics
            processing_time = (time.time() - start_time) * 1000
            self._update_metrics(prediction, processing_time)
            
            return prediction
            
        except Exception as e:
            logger.error(f"Fraud prediction failed: {e}")
            # Return safe default
            from models.fraud_prediction import FraudPrediction
            return FraudPrediction(
                transaction_id=transaction.transaction_id,
                fraud_probability=0.5,
                risk_level='MEDIUM',
                confidence_score=0.1,
                model_version='error',
                prediction_time=datetime.now().isoformat(),
                features_used=[],
                explanation={'error': str(e)}
            )
    
    def _engineer_features(self, transaction: 'Transaction') -> Dict[str, float]:
        """Extract features from transaction"""
        features = {
            'amount': float(transaction.amount),
            'hour_of_day': datetime.now().hour,
            'is_weekend': datetime.now().weekday() >= 5,
            'is_night_transaction': 22 <= datetime.now().hour or datetime.now().hour <= 6,
            'amount_log': np.log1p(float(transaction.amount)),
            'merchant_risk_score': self._get_merchant_risk_score(transaction.merchant_id),
            'customer_risk_score': self._get_customer_risk_score(transaction.customer_id),
            'amount_vs_customer_avg': self._get_amount_vs_avg(transaction)
        }
        return features
    
    def _get_merchant_risk_score(self, merchant_id: str) -> float:
        """Get merchant risk score (mock implementation)"""
        # In real implementation, this would query historical data
        merchant_hash = hash(merchant_id) % 100
        return merchant_hash / 100.0
    
    def _get_customer_risk_score(self, customer_id: str) -> float:
        """Get customer risk score (mock implementation)"""
        customer_hash = hash(customer_id) % 100
        return customer_hash / 100.0
    
    def _get_amount_vs_avg(self, transaction: 'Transaction') -> float:
        """Get transaction amount vs customer average (mock implementation)"""
        # Mock: assume customer average is around $500
        customer_avg = 500.0
        return float(transaction.amount) / customer_avg
    
    def _make_prediction(self, features: Dict[str, float]) -> Dict:
        """Make fraud prediction using the model"""
        try:
            # Convert features to array
            feature_names = ['amount', 'hour_of_day', 'is_weekend', 'is_night_transaction',
                           'amount_log', 'merchant_risk_score', 'customer_risk_score', 'amount_vs_customer_avg']
            feature_array = np.array([[features.get(name, 0) for name in feature_names]])
            
            # Scale features
            scaler = self.model['scaler']
            scaled_features = scaler.transform(feature_array)
            
            # Random Forest prediction
            rf_model = self.model['random_forest']
            rf_proba = rf_model.predict_proba(scaled_features)[0, 1]
            
            # Isolation Forest anomaly score
            isolation_model = self.model['isolation_forest']
            isolation_score = isolation_model.decision_function(scaled_features)[0]
            isolation_proba = max(0, min(1, (1 - isolation_score) / 2))
            
            # Rule-based score
            rule_score = self._rule_based_score(features)
            
            # Ensemble prediction
            fraud_probability = (rf_proba * 0.5 + isolation_proba * 0.3 + rule_score * 0.2)
            confidence = min(0.95, abs(fraud_probability - 0.5) * 2)
            
            return {
                'fraud_probability': fraud_probability,
                'confidence': confidence,
                'model_type': 'ensemble'
            }
            
        except Exception as e:
            logger.error(f"Model prediction failed: {e}")
            return {
                'fraud_probability': 0.5,
                'confidence': 0.1,
                'model_type': 'fallback'
            }
    
    def _rule_based_score(self, features: Dict) -> float:
        """Rule-based fraud scoring"""
        score = 0.0
        
        # High amount rule
        amount = features.get('amount', 0)
        if amount > 5000:
            score += 0.4
        elif amount > 1000:
            score += 0.2
        
        # Time-based rules
        if features.get('is_night_transaction', False):
            score += 0.3
        
        # Customer behavior rules
        amount_vs_avg = features.get('amount_vs_customer_avg', 1)
        if amount_vs_avg > 5:
            score += 0.4
        elif amount_vs_avg > 3:
            score += 0.2
        
        # Merchant risk
        merchant_risk = features.get('merchant_risk_score', 0)
        score += merchant_risk * 0.3
        
        return min(1.0, score)
    
    def _generate_explanation(self, features: Dict, prediction_result: Dict) -> Dict:
        """Generate explanation for the prediction"""
        explanation = {
            'prediction_type': prediction_result['model_type'],
            'key_factors': [],
            'risk_indicators': []
        }
        
        # High amount
        if features.get('amount', 0) > 1000:
            explanation['risk_indicators'].append(f"High transaction amount: ${features['amount']:.2f}")
        
        # Time-based
        if features.get('is_night_transaction', False):
            explanation['risk_indicators'].append("Night-time transaction")
        
        # Customer behavior
        amount_vs_avg = features.get('amount_vs_customer_avg', 1)
        if amount_vs_avg > 2:
            explanation['risk_indicators'].append(f"Amount {amount_vs_avg:.1f}x customer average")
        
        # Merchant risk
        merchant_risk = features.get('merchant_risk_score', 0)
        if merchant_risk > 0.5:
            explanation['risk_indicators'].append(f"High-risk merchant (score: {merchant_risk:.2f})")
        
        return explanation
    
    def calculate_fraud_score(self, scoring_data: Dict) -> float:
        """Calculate lightweight fraud score"""
        try:
            score = 0.0
            
            # Amount-based scoring
            amount = scoring_data.get('amount', 0)
            if amount > 5000:
                score += 0.4
            elif amount > 1000:
                score += 0.2
            
            # Merchant category risk
            category = scoring_data.get('merchant_category', '').lower()
            high_risk_categories = ['online', 'gambling', 'crypto']
            if category in high_risk_categories:
                score += 0.3
            
            # Customer risk level
            customer_risk = scoring_data.get('customer_risk_level', 'low').lower()
            if customer_risk == 'high':
                score += 0.4
            elif customer_risk == 'medium':
                score += 0.2
            
            return min(1.0, score)
            
        except Exception as e:
            logger.error(f"Fraud scoring failed: {e}")
            return 0.5
    
    def explain_prediction(self, transaction_id: str) -> Dict:
        """Explain a previous prediction"""
        # In real implementation, would retrieve from cache/database
        return {
            'transaction_id': transaction_id,
            'explanation': 'Prediction explanation not available in cache',
            'timestamp': datetime.now().isoformat()
        }
    
    def process_feedback(self, feedback_data: Dict) -> str:
        """Process feedback on predictions"""
        feedback_id = str(uuid.uuid4())
        
        # In real implementation, would store feedback for model improvement
        logger.info(f"Received feedback for transaction {feedback_data.get('transaction_id')}: {feedback_data.get('actual_fraud')}")
        
        return feedback_id
    
    def _update_metrics(self, prediction: 'FraudPrediction', processing_time: float):
        """Update performance metrics"""
        self.performance_metrics['total_predictions'] += 1
        self.performance_metrics['processing_times'].append(processing_time)
        
        if prediction.risk_level == 'HIGH':
            self.performance_metrics['fraud_detected'] += 1
        
        # Keep only last 1000 processing times
        if len(self.performance_metrics['processing_times']) > 1000:
            self.performance_metrics['processing_times'] = self.performance_metrics['processing_times'][-1000:]
    
    def get_performance_metrics(self) -> Dict:
        """Get fraud detection performance metrics"""
        processing_times = self.performance_metrics['processing_times']
        
        metrics = {
            'total_predictions': self.performance_metrics['total_predictions'],
            'fraud_detected': self.performance_metrics['fraud_detected'],
            'fraud_rate': 0.0,
            'avg_processing_time_ms': 0.0,
            'p95_processing_time_ms': 0.0,
            'model_version': self.model.get('version', 'unknown'),
            'timestamp': datetime.now().isoformat()
        }
        
        if self.performance_metrics['total_predictions'] > 0:
            metrics['fraud_rate'] = self.performance_metrics['fraud_detected'] / self.performance_metrics['total_predictions']
        
        if processing_times:
            metrics['avg_processing_time_ms'] = np.mean(processing_times)
            metrics['p95_processing_time_ms'] = np.percentile(processing_times, 95)
        
        return metrics
    
    def get_service_status(self) -> Dict:
        """Get fraud service status"""
        return {
            'status': 'healthy',
            'model_loaded': self.model is not None,
            'model_version': self.model.get('version', 'unknown') if self.model else None,
            'total_predictions': self.performance_metrics['total_predictions'],
            'timestamp': datetime.now().isoformat()
        } 