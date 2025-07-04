#!/usr/bin/env python3
"""
🔍 Fraud Detection Controller
Professional route handling for fraud detection endpoints.

This controller handles:
- Fraud prediction requests
- Batch prediction processing
- Real-time fraud scoring
- Model performance metrics
"""

from flask import Blueprint, request, jsonify, current_app
import logging
from datetime import datetime
import time
from typing import Dict, List

# Import services
from services.fraud_service import FraudService
from services.validation_service import ValidationService

# Import models
from models.fraud_prediction import FraudPrediction
from models.transaction import Transaction

# Import decorators
from decorators.rate_limit import rate_limit
from decorators.validate_json import validate_json
from decorators.monitor_performance import monitor_performance

# Create blueprint
fraud_bp = Blueprint('fraud', __name__)
logger = logging.getLogger(__name__)

# Initialize services (will be injected by service registry)
fraud_service = None
validation_service = None

def get_services():
    """Get services from application context"""
    global fraud_service, validation_service
    
    if fraud_service is None:
        service_registry = current_app.service_registry
        fraud_service = service_registry.get_service('fraud_service')
        validation_service = service_registry.get_service('validation_service')
    
    return fraud_service, validation_service

@fraud_bp.route('/predict', methods=['POST'])
@rate_limit(limit=100, per_second=60)  # 100 requests per minute
@validate_json
@monitor_performance
def predict_fraud():
    """
    Predict fraud probability for a single transaction
    
    Expected payload:
    {
        "transaction_id": "txn_001",
        "customer_id": "cust_123", 
        "merchant_id": "merch_456",
        "amount": 1500.00,
        "merchant_category": "online",
        "timestamp": "2025-01-04T15:30:00Z"
    }
    """
    try:
        # Get services
        fraud_svc, validation_svc = get_services()
        
        # Get request data
        transaction_data = request.get_json()
        
        # Validate transaction data
        is_valid, validation_errors = validation_svc.validate_transaction(transaction_data)
        if not is_valid:
            return jsonify({
                'error': 'Validation failed',
                'validation_errors': validation_errors,
                'timestamp': datetime.now().isoformat()
            }), 400
        
        # Create transaction model
        transaction = Transaction.from_dict(transaction_data)
        
        # Make prediction
        prediction = fraud_svc.predict_fraud(transaction)
        
        # Return prediction
        return jsonify(prediction.to_dict()), 200
        
    except ValueError as e:
        logger.warning(f"Invalid request data: {e}")
        return jsonify({
            'error': 'Invalid request',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 400
        
    except Exception as e:
        logger.error(f"Fraud prediction failed: {e}")
        return jsonify({
            'error': 'Prediction failed',
            'message': 'Internal processing error',
            'timestamp': datetime.now().isoformat()
        }), 500

@fraud_bp.route('/predict/batch', methods=['POST'])
@rate_limit(limit=10, per_second=60)  # 10 batch requests per minute
@validate_json
@monitor_performance
def predict_fraud_batch():
    """
    Predict fraud for multiple transactions
    
    Expected payload:
    {
        "transactions": [
            {"transaction_id": "txn_001", ...},
            {"transaction_id": "txn_002", ...}
        ],
        "options": {
            "return_explanations": true,
            "async_processing": false
        }
    }
    """
    try:
        # Get services
        fraud_svc, validation_svc = get_services()
        
        # Get request data
        request_data = request.get_json()
        transactions_data = request_data.get('transactions', [])
        options = request_data.get('options', {})
        
        # Validate batch size
        max_batch_size = current_app.config.get('MAX_BATCH_SIZE', 100)
        if len(transactions_data) > max_batch_size:
            return jsonify({
                'error': 'Batch too large',
                'message': f'Maximum batch size is {max_batch_size}',
                'provided_size': len(transactions_data),
                'timestamp': datetime.now().isoformat()
            }), 400
        
        # Process transactions
        results = []
        errors = []
        
        for i, transaction_data in enumerate(transactions_data):
            try:
                # Validate transaction
                is_valid, validation_errors = validation_svc.validate_transaction(transaction_data)
                if not is_valid:
                    errors.append({
                        'index': i,
                        'transaction_id': transaction_data.get('transaction_id', f'index_{i}'),
                        'errors': validation_errors
                    })
                    continue
                
                # Create transaction and predict
                transaction = Transaction.from_dict(transaction_data)
                prediction = fraud_svc.predict_fraud(transaction)
                
                results.append(prediction.to_dict())
                
            except Exception as e:
                errors.append({
                    'index': i,
                    'transaction_id': transaction_data.get('transaction_id', f'index_{i}'),
                    'error': str(e)
                })
        
        return jsonify({
            'predictions': results,
            'errors': errors,
            'summary': {
                'total_processed': len(transactions_data),
                'successful_predictions': len(results),
                'failed_predictions': len(errors),
                'fraud_detected': sum(1 for r in results if r['risk_level'] == 'HIGH'),
                'processing_time_ms': time.time() * 1000 - request.start_time * 1000
            },
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Batch prediction failed: {e}")
        return jsonify({
            'error': 'Batch prediction failed',
            'message': 'Internal processing error',
            'timestamp': datetime.now().isoformat()
        }), 500

@fraud_bp.route('/score', methods=['POST'])
@rate_limit(limit=200, per_second=60)  # High frequency scoring
@validate_json
@monitor_performance
def fraud_score():
    """
    Get real-time fraud score (lightweight prediction)
    
    Expected payload:
    {
        "amount": 1500.00,
        "merchant_category": "online",
        "customer_risk_level": "medium"
    }
    """
    try:
        # Get services
        fraud_svc, _ = get_services()
        
        # Get request data
        scoring_data = request.get_json()
        
        # Get lightweight fraud score
        score = fraud_svc.calculate_fraud_score(scoring_data)
        
        return jsonify({
            'fraud_score': score,
            'risk_level': 'HIGH' if score > 0.7 else 'MEDIUM' if score > 0.3 else 'LOW',
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Fraud scoring failed: {e}")
        return jsonify({
            'error': 'Scoring failed',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@fraud_bp.route('/explain', methods=['POST'])
@rate_limit(limit=50, per_second=60)
@validate_json
@monitor_performance
def explain_prediction():
    """
    Explain a fraud prediction with feature importance
    
    Expected payload:
    {
        "transaction_id": "txn_001",
        "prediction_id": "pred_123"  # Optional
    }
    """
    try:
        # Get services
        fraud_svc, _ = get_services()
        
        # Get request data
        explain_data = request.get_json()
        transaction_id = explain_data.get('transaction_id')
        
        if not transaction_id:
            return jsonify({
                'error': 'Missing transaction_id',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        # Get explanation
        explanation = fraud_svc.explain_prediction(transaction_id)
        
        return jsonify(explanation), 200
        
    except Exception as e:
        logger.error(f"Prediction explanation failed: {e}")
        return jsonify({
            'error': 'Explanation failed',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@fraud_bp.route('/feedback', methods=['POST'])
@rate_limit(limit=20, per_second=60)
@validate_json
@monitor_performance
def feedback():
    """
    Submit feedback on fraud predictions for model improvement
    
    Expected payload:
    {
        "transaction_id": "txn_001",
        "prediction_id": "pred_123",
        "actual_fraud": true,
        "feedback_source": "manual_review",
        "notes": "Confirmed fraudulent activity"
    }
    """
    try:
        # Get services
        fraud_svc, _ = get_services()
        
        # Get request data
        feedback_data = request.get_json()
        
        # Process feedback
        feedback_id = fraud_svc.process_feedback(feedback_data)
        
        return jsonify({
            'feedback_id': feedback_id,
            'status': 'accepted',
            'message': 'Feedback received and will be used for model improvement',
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Feedback processing failed: {e}")
        return jsonify({
            'error': 'Feedback processing failed',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@fraud_bp.route('/metrics', methods=['GET'])
@monitor_performance
def fraud_metrics():
    """Get fraud detection metrics and statistics"""
    try:
        # Get services
        fraud_svc, _ = get_services()
        
        # Get metrics
        metrics = fraud_svc.get_performance_metrics()
        
        return jsonify(metrics), 200
        
    except Exception as e:
        logger.error(f"Failed to get fraud metrics: {e}")
        return jsonify({
            'error': 'Metrics unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@fraud_bp.route('/status', methods=['GET'])
def fraud_status():
    """Get fraud detection service status"""
    try:
        # Get services
        fraud_svc, _ = get_services()
        
        # Get status
        status = fraud_svc.get_service_status()
        
        return jsonify(status), 200
        
    except Exception as e:
        logger.error(f"Failed to get fraud status: {e}")
        return jsonify({
            'error': 'Status unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500 