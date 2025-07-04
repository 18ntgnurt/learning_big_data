#!/usr/bin/env python3
"""
Simple ML API - Lightweight version for fast startup
No heavy ML dependencies, just basic API structure
"""

from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import random
import time
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Simple in-memory "model" for demonstration
class SimpleFraudDetector:
    def __init__(self):
        self.rules = {
            'high_amount_threshold': 10000,
            'suspicious_locations': ['Unknown', 'Blocked'],
            'max_daily_transactions': 10
        }
    
    def predict(self, transaction):
        """Simple rule-based fraud detection"""
        score = 0.1  # Base score
        
        # Amount-based rules
        amount = transaction.get('amount', 0)
        if amount > self.rules['high_amount_threshold']:
            score += 0.6
        elif amount > 5000:
            score += 0.3
        
        # Location-based rules
        location = transaction.get('location', 'Unknown')
        if location in self.rules['suspicious_locations']:
            score += 0.4
        
        # Time-based rules (simple)
        hour = datetime.now().hour
        if hour < 6 or hour > 22:  # Late night transactions
            score += 0.2
        
        # Random factor for demo
        score += random.uniform(-0.1, 0.1)
        
        return min(max(score, 0.0), 1.0)

# Initialize simple model
fraud_detector = SimpleFraudDetector()

@app.route('/')
def home():
    """Root endpoint"""
    return jsonify({
        'service': 'Simple ML API',
        'version': '1.0.0',
        'status': 'running',
        'timestamp': datetime.now().isoformat(),
        'endpoints': {
            'health': '/health',
            'fraud_detection': '/api/v1/fraud/predict',
            'metrics': '/metrics'
        }
    })

@app.route('/health')
def health():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.now().isoformat(),
        'uptime_seconds': time.time() - start_time,
        'service': 'simple-ml-api'
    })

@app.route('/api/v1/fraud/predict', methods=['POST'])
def predict_fraud():
    """Simple fraud prediction endpoint"""
    try:
        # Get transaction data
        transaction = request.json
        if not transaction:
            return jsonify({'error': 'No transaction data provided'}), 400
        
        # Predict fraud score
        fraud_score = fraud_detector.predict(transaction)
        
        # Determine risk level
        if fraud_score > 0.7:
            risk_level = 'HIGH'
        elif fraud_score > 0.4:
            risk_level = 'MEDIUM'
        else:
            risk_level = 'LOW'
        
        # Generate response
        response = {
            'transaction_id': transaction.get('id', f'txn_{int(time.time())}'),
            'fraud_score': round(fraud_score, 3),
            'risk_level': risk_level,
            'is_fraud': fraud_score > 0.5,
            'timestamp': datetime.now().isoformat(),
            'model_version': 'simple-rules-v1',
            'processing_time_ms': random.randint(10, 50)  # Simulate processing time
        }
        
        return jsonify(response)
    
    except Exception as e:
        return jsonify({
            'error': 'Prediction failed',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@app.route('/api/v1/fraud/batch', methods=['POST'])
def predict_fraud_batch():
    """Batch fraud prediction endpoint"""
    try:
        data = request.json
        transactions = data.get('transactions', [])
        
        if not transactions:
            return jsonify({'error': 'No transactions provided'}), 400
        
        results = []
        for txn in transactions:
            fraud_score = fraud_detector.predict(txn)
            risk_level = 'HIGH' if fraud_score > 0.7 else 'MEDIUM' if fraud_score > 0.4 else 'LOW'
            
            results.append({
                'transaction_id': txn.get('id', f'txn_{len(results)}'),
                'fraud_score': round(fraud_score, 3),
                'risk_level': risk_level,
                'is_fraud': fraud_score > 0.5
            })
        
        return jsonify({
            'predictions': results,
            'batch_size': len(results),
            'timestamp': datetime.now().isoformat(),
            'model_version': 'simple-rules-v1'
        })
    
    except Exception as e:
        return jsonify({
            'error': 'Batch prediction failed',
            'message': str(e)
        }), 500

@app.route('/metrics')
def metrics():
    """Simple metrics endpoint"""
    return jsonify({
        'uptime_seconds': time.time() - start_time,
        'requests_processed': random.randint(100, 1000),  # Mock data
        'fraud_detected': random.randint(5, 50),  # Mock data
        'model_accuracy': 0.95,  # Mock accuracy
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/v1/model/info')
def model_info():
    """Model information endpoint"""
    return jsonify({
        'model_name': 'Simple Rules Engine',
        'version': '1.0.0',
        'type': 'rule-based',
        'features': ['amount', 'location', 'time'],
        'rules': fraud_detector.rules,
        'last_updated': datetime.now().isoformat()
    })

if __name__ == '__main__':
    start_time = time.time()
    print("🚀 Starting Simple ML API...")
    print(f"📡 Server will be available at: http://0.0.0.0:5000")
    print(f"🔍 Health check: http://0.0.0.0:5000/health")
    print(f"🤖 Fraud API: http://0.0.0.0:5000/api/v1/fraud/predict")
    
    app.run(host='0.0.0.0', port=5000, debug=False) 