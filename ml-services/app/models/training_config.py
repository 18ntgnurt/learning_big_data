#!/usr/bin/env python3
"""
⚙️ Training Configuration Model
Data model for ML training configurations.
"""

from typing import Dict, Any, List, Optional
import json

class TrainingConfig:
    """
    Training Configuration data model
    
    Represents configuration for an ML training job.
    """
    
    def __init__(
        self,
        model_name: str,
        algorithm: str,
        dataset_version: str = 'latest',
        experiment_name: Optional[str] = None,
        hyperparameter_tuning: bool = False,
        config: Optional[Dict[str, Any]] = None,
        **kwargs
    ):
        self.model_name = model_name
        self.algorithm = algorithm
        self.dataset_version = dataset_version
        self.experiment_name = experiment_name or f"{model_name}_experiment"
        self.hyperparameter_tuning = hyperparameter_tuning
        self.config = config or {}
        
        # Additional configuration
        for key, value in kwargs.items():
            setattr(self, key, value)
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'TrainingConfig':
        """Create TrainingConfig from dictionary"""
        return cls(**data)
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert TrainingConfig to dictionary"""
        return {
            'model_name': self.model_name,
            'algorithm': self.algorithm,
            'dataset_version': self.dataset_version,
            'experiment_name': self.experiment_name,
            'hyperparameter_tuning': self.hyperparameter_tuning,
            'config': self.config
        }
    
    def is_valid(self) -> bool:
        """Validate configuration"""
        # Check required fields
        if not self.model_name or not self.algorithm:
            return False
        
        # Check algorithm validity
        valid_algorithms = ['random_forest', 'logistic_regression', 'xgboost', 'neural_network', 'fraud_detection']
        if self.algorithm not in valid_algorithms:
            return False
        
        return True
    
    def get_validation_issues(self) -> List[str]:
        """Get list of validation issues"""
        issues = []
        
        if not self.model_name:
            issues.append("Model name is required")
        
        if not self.algorithm:
            issues.append("Algorithm is required")
        
        valid_algorithms = ['random_forest', 'logistic_regression', 'xgboost', 'neural_network', 'fraud_detection']
        if self.algorithm and self.algorithm not in valid_algorithms:
            issues.append(f"Algorithm must be one of: {valid_algorithms}")
        
        return issues 