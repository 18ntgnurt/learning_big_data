#!/usr/bin/env python3
"""
📦 Model Management Controller
Professional route handling for model management endpoints.
"""

from flask import Blueprint, request, jsonify, current_app
import logging
from datetime import datetime

# Import decorators
from decorators.rate_limit import rate_limit
from decorators.monitor_performance import monitor_performance

# Create blueprint
model_bp = Blueprint('models', __name__)
logger = logging.getLogger(__name__)

# Initialize services (will be injected by service registry)
mlflow_service = None

def get_services():
    """Get services from application context"""
    global mlflow_service
    
    if mlflow_service is None:
        service_registry = current_app.service_registry
        mlflow_service = service_registry.get_service('mlflow_service')
    
    return mlflow_service

@model_bp.route('/', methods=['GET'])
@monitor_performance
def list_models():
    """List all registered models"""
    try:
        mlflow_svc = get_services()
        models = mlflow_svc.list_registered_models()
        
        return jsonify({
            'models': models,
            'count': len(models),
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to list models: {e}")
        return jsonify({
            'error': 'Failed to list models',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@model_bp.route('/<model_name>/versions', methods=['GET'])
@monitor_performance
def list_model_versions(model_name: str):
    """List versions of a specific model"""
    try:
        mlflow_svc = get_services()
        models = mlflow_svc.list_registered_models()
        
        # Find the model
        model_info = None
        for model in models:
            if model['name'] == model_name:
                model_info = model
                break
        
        if not model_info:
            return jsonify({
                'error': 'Model not found',
                'model_name': model_name,
                'timestamp': datetime.now().isoformat()
            }), 404
        
        return jsonify({
            'model_name': model_name,
            'versions': model_info.get('versions', []),
            'latest_version': model_info.get('latest_version'),
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to list model versions: {e}")
        return jsonify({
            'error': 'Failed to list model versions',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@model_bp.route('/<model_name>/versions/<version>', methods=['GET'])
@monitor_performance
def get_model_version(model_name: str, version: str):
    """Get specific model version"""
    try:
        mlflow_svc = get_services()
        model_version = mlflow_svc.get_model_version(model_name, version)
        
        if not model_version:
            return jsonify({
                'error': 'Model version not found',
                'model_name': model_name,
                'version': version,
                'timestamp': datetime.now().isoformat()
            }), 404
        
        return jsonify({
            'model_name': model_name,
            'version_info': model_version,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to get model version: {e}")
        return jsonify({
            'error': 'Failed to get model version',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@model_bp.route('/<model_name>/latest', methods=['GET'])
@monitor_performance
def get_latest_model_version(model_name: str):
    """Get latest model version, optionally filtered by stage"""
    try:
        stage = request.args.get('stage')
        
        mlflow_svc = get_services()
        latest_version = mlflow_svc.get_latest_model_version(model_name, stage)
        
        if not latest_version:
            return jsonify({
                'error': 'No model version found',
                'model_name': model_name,
                'stage': stage,
                'timestamp': datetime.now().isoformat()
            }), 404
        
        return jsonify({
            'model_name': model_name,
            'latest_version': latest_version,
            'stage_filter': stage,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"Failed to get latest model version: {e}")
        return jsonify({
            'error': 'Failed to get latest model version',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500 