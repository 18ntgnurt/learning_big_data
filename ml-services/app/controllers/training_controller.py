#!/usr/bin/env python3
"""
🏗️ Training Pipeline Controller
Professional route handling for ML model training endpoints.

This controller handles:
- Model training requests
- Hyperparameter tuning
- Training job monitoring
- Model registration
"""

from flask import Blueprint, request, jsonify, current_app
import logging
from datetime import datetime
import uuid
from typing import Dict, List

# Import services
from services.training_service import TrainingService
from services.mlflow_service import MLflowService

# Import models
from models.training_job import TrainingJob
from models.training_config import TrainingConfig

# Import decorators
from decorators.rate_limit import rate_limit
from decorators.validate_json import validate_json
from decorators.monitor_performance import monitor_performance

# Create blueprint
training_bp = Blueprint('training', __name__)
logger = logging.getLogger(__name__)

# Initialize services (will be injected by service registry)
training_service = None
mlflow_service = None

def get_services():
    """Get services from application context"""
    global training_service, mlflow_service
    
    if training_service is None:
        service_registry = current_app.service_registry
        training_service = service_registry.get_service('training_service')
        mlflow_service = service_registry.get_service('mlflow_service')
    
    return training_service, mlflow_service

@training_bp.route('/train', methods=['POST'])
@rate_limit(limit=5, per_second=300)  # 5 training jobs per 5 minutes
@validate_json
@monitor_performance
def train_model():
    """
    Start a new model training job
    
    Expected payload:
    {
        "model_name": "random_forest",
        "algorithm": "fraud_detection",
        "hyperparameter_tuning": true,
        "dataset_version": "v1.2",
        "experiment_name": "fraud_model_experiment",
        "config": {
            "n_estimators": 100,
            "max_depth": 10
        }
    }
    """
    try:
        # Get services
        training_svc, mlflow_svc = get_services()
        
        # Get request data
        training_request = request.get_json()
        
        # Validate training request
        required_fields = ['model_name', 'algorithm']
        for field in required_fields:
            if field not in training_request:
                return jsonify({
                    'error': 'Missing required field',
                    'field': field,
                    'timestamp': datetime.now().isoformat()
                }), 400
        
        # Create training configuration
        config = TrainingConfig.from_dict(training_request)
        
        # Validate configuration
        if not config.is_valid():
            return jsonify({
                'error': 'Invalid training configuration',
                'issues': config.get_validation_issues(),
                'timestamp': datetime.now().isoformat()
            }), 400
        
        # Start training job
        job_id = str(uuid.uuid4())
        training_job = training_svc.start_training(job_id, config)
        
        return jsonify({
            'job_id': job_id,
            'status': training_job.status,
            'model_name': training_job.model_name,
            'algorithm': training_job.algorithm,
            'started_at': training_job.started_at.isoformat(),
            'estimated_completion': training_job.estimated_completion.isoformat() if training_job.estimated_completion else None,
            'tracking_url': f"/api/v1/training/jobs/{job_id}",
            'mlflow_run_id': training_job.mlflow_run_id,
            'message': 'Training job started successfully'
        }), 202  # Accepted
        
    except ValueError as e:
        logger.warning(f"Invalid training request: {e}")
        return jsonify({
            'error': 'Invalid request',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 400
        
    except Exception as e:
        logger.error(f"Training job failed to start: {e}")
        return jsonify({
            'error': 'Training job failed',
            'message': 'Failed to start training job',
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/jobs/<job_id>', methods=['GET'])
@monitor_performance
def get_training_job(job_id):
    """Get training job status and details"""
    try:
        # Get services
        training_svc, _ = get_services()
        
        # Get job details
        job = training_svc.get_training_job(job_id)
        
        if not job:
            return jsonify({
                'error': 'Job not found',
                'job_id': job_id,
                'timestamp': datetime.now().isoformat()
            }), 404
        
        return jsonify(job.to_dict()), 200
        
    except Exception as e:
        logger.error(f"Failed to get training job {job_id}: {e}")
        return jsonify({
            'error': 'Failed to get job details',
            'job_id': job_id,
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/jobs/<job_id>/cancel', methods=['POST'])
@monitor_performance
def cancel_training_job(job_id):
    """Cancel a running training job"""
    try:
        # Get services
        training_svc, _ = get_services()
        
        # Cancel job
        success = training_svc.cancel_training_job(job_id)
        
        if not success:
            return jsonify({
                'error': 'Job not found or cannot be cancelled',
                'job_id': job_id,
                'timestamp': datetime.now().isoformat()
            }), 404
        
        return jsonify({
            'job_id': job_id,
            'status': 'cancelled',
            'message': 'Training job cancelled successfully',
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to cancel training job {job_id}: {e}")
        return jsonify({
            'error': 'Failed to cancel job',
            'job_id': job_id,
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/jobs', methods=['GET'])
@monitor_performance
def list_training_jobs():
    """List training jobs with optional filtering"""
    try:
        # Get services
        training_svc, _ = get_services()
        
        # Get query parameters
        status = request.args.get('status')
        limit = min(int(request.args.get('limit', 50)), 200)  # Max 200
        offset = int(request.args.get('offset', 0))
        
        # Get jobs
        jobs = training_svc.list_training_jobs(
            status=status,
            limit=limit,
            offset=offset
        )
        
        return jsonify({
            'jobs': [job.to_dict() for job in jobs],
            'pagination': {
                'limit': limit,
                'offset': offset,
                'total': len(jobs)
            },
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to list training jobs: {e}")
        return jsonify({
            'error': 'Failed to list jobs',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/hyperparameter-tuning', methods=['POST'])
@rate_limit(limit=2, per_second=3600)  # 2 tuning jobs per hour
@validate_json
@monitor_performance
def hyperparameter_tuning():
    """
    Start hyperparameter tuning job
    
    Expected payload:
    {
        "model_name": "random_forest",
        "algorithm": "fraud_detection",
        "tuning_strategy": "bayesian",
        "max_trials": 50,
        "timeout_hours": 2,
        "parameter_space": {
            "n_estimators": {"min": 50, "max": 500, "step": 50},
            "max_depth": {"values": [5, 10, 15, 20, null]},
            "min_samples_split": {"min": 2, "max": 20}
        }
    }
    """
    try:
        # Get services
        training_svc, _ = get_services()
        
        # Get request data
        tuning_request = request.get_json()
        
        # Validate tuning request
        required_fields = ['model_name', 'algorithm', 'parameter_space']
        for field in required_fields:
            if field not in tuning_request:
                return jsonify({
                    'error': 'Missing required field',
                    'field': field,
                    'timestamp': datetime.now().isoformat()
                }), 400
        
        # Start hyperparameter tuning
        job_id = str(uuid.uuid4())
        tuning_job = training_svc.start_hyperparameter_tuning(job_id, tuning_request)
        
        return jsonify({
            'job_id': job_id,
            'status': tuning_job.status,
            'tuning_strategy': tuning_job.tuning_strategy,
            'max_trials': tuning_job.max_trials,
            'started_at': tuning_job.started_at.isoformat(),
            'estimated_completion': tuning_job.estimated_completion.isoformat() if tuning_job.estimated_completion else None,
            'tracking_url': f"/api/v1/training/jobs/{job_id}",
            'message': 'Hyperparameter tuning started successfully'
        }), 202
        
    except Exception as e:
        logger.error(f"Hyperparameter tuning failed to start: {e}")
        return jsonify({
            'error': 'Tuning job failed',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/experiments', methods=['GET'])
@monitor_performance
def list_experiments():
    """List MLflow experiments"""
    try:
        # Get services
        _, mlflow_svc = get_services()
        
        # Get experiments
        experiments = mlflow_svc.list_experiments()
        
        return jsonify({
            'experiments': experiments,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to list experiments: {e}")
        return jsonify({
            'error': 'Failed to list experiments',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/experiments/<experiment_id>/runs', methods=['GET'])
@monitor_performance
def list_experiment_runs(experiment_id):
    """List runs for a specific experiment"""
    try:
        # Get services
        _, mlflow_svc = get_services()
        
        # Get query parameters
        limit = min(int(request.args.get('limit', 20)), 100)
        
        # Get runs
        runs = mlflow_svc.list_experiment_runs(experiment_id, limit=limit)
        
        return jsonify({
            'experiment_id': experiment_id,
            'runs': runs,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to list experiment runs: {e}")
        return jsonify({
            'error': 'Failed to list runs',
            'experiment_id': experiment_id,
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/models/register', methods=['POST'])
@rate_limit(limit=10, per_second=300)  # 10 registrations per 5 minutes
@validate_json
@monitor_performance
def register_model():
    """
    Register a trained model to the model registry
    
    Expected payload:
    {
        "run_id": "abc123def456",
        "model_name": "fraud_detection_v2",
        "model_path": "model",
        "stage": "Staging",
        "description": "Improved fraud detection model with feature engineering v2"
    }
    """
    try:
        # Get services
        _, mlflow_svc = get_services()
        
        # Get request data
        registration_request = request.get_json()
        
        # Validate request
        required_fields = ['run_id', 'model_name']
        for field in required_fields:
            if field not in registration_request:
                return jsonify({
                    'error': 'Missing required field',
                    'field': field,
                    'timestamp': datetime.now().isoformat()
                }), 400
        
        # Register model
        model_version = mlflow_svc.register_model(
            run_id=registration_request['run_id'],
            model_name=registration_request['model_name'],
            model_path=registration_request.get('model_path', 'model'),
            description=registration_request.get('description', '')
        )
        
        # Transition to stage if specified
        stage = registration_request.get('stage')
        if stage:
            mlflow_svc.transition_model_stage(
                model_name=registration_request['model_name'],
                version=model_version.version,
                stage=stage
            )
        
        return jsonify({
            'model_name': model_version.name,
            'version': model_version.version,
            'stage': stage or 'None',
            'run_id': registration_request['run_id'],
            'registration_time': datetime.now().isoformat(),
            'message': 'Model registered successfully'
        }), 201
        
    except Exception as e:
        logger.error(f"Model registration failed: {e}")
        return jsonify({
            'error': 'Model registration failed',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/status', methods=['GET'])
def training_status():
    """Get training service status"""
    try:
        # Get services
        training_svc, mlflow_svc = get_services()
        
        # Get status
        training_status = training_svc.get_service_status()
        mlflow_status = mlflow_svc.get_service_status()
        
        return jsonify({
            'training_service': training_status,
            'mlflow_service': mlflow_status,
            'overall_status': 'healthy' if training_status['status'] == 'healthy' and mlflow_status['status'] == 'healthy' else 'degraded',
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to get training status: {e}")
        return jsonify({
            'error': 'Status unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@training_bp.route('/metrics', methods=['GET'])
@monitor_performance
def training_metrics():
    """Get training service metrics"""
    try:
        # Get services
        training_svc, _ = get_services()
        
        # Get metrics
        metrics = training_svc.get_performance_metrics()
        
        return jsonify(metrics), 200
        
    except Exception as e:
        logger.error(f"Failed to get training metrics: {e}")
        return jsonify({
            'error': 'Metrics unavailable',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500 