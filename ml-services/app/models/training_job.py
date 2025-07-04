#!/usr/bin/env python3
"""
🏗️ Training Job Model
Data model for ML training jobs.
"""

from datetime import datetime
from typing import Dict, Any, Optional
import json

class TrainingJob:
    """
    Training Job data model
    
    Represents an ML training job with all necessary tracking information.
    """
    
    def __init__(
        self,
        job_id: str,
        model_name: str,
        algorithm: str,
        config: Dict[str, Any],
        status: str = 'queued',
        started_at: Optional[datetime] = None,
        completed_at: Optional[datetime] = None,
        created_by: str = 'system',
        job_type: str = 'training',
        **kwargs
    ):
        self.job_id = job_id
        self.model_name = model_name
        self.algorithm = algorithm
        self.config = config
        self.status = status  # queued, running, completed, failed, cancelled
        self.started_at = started_at or datetime.now()
        self.completed_at = completed_at
        self.created_by = created_by
        self.job_type = job_type  # training, hyperparameter_tuning
        
        # Training progress and metrics
        self.progress = 0
        self.current_metrics = {}
        self.final_metrics = {}
        self.model_path = None
        self.error_message = None
        
        # Hyperparameter tuning specific
        self.tuning_strategy = None
        self.max_trials = None
        self.estimated_completion = None
        
        # Additional fields
        for key, value in kwargs.items():
            setattr(self, key, value)
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'TrainingJob':
        """Create TrainingJob from dictionary"""
        # Convert datetime strings back to datetime objects
        if 'started_at' in data and isinstance(data['started_at'], str):
            data['started_at'] = datetime.fromisoformat(data['started_at'])
        if 'completed_at' in data and isinstance(data['completed_at'], str):
            data['completed_at'] = datetime.fromisoformat(data['completed_at'])
        if 'estimated_completion' in data and isinstance(data['estimated_completion'], str):
            data['estimated_completion'] = datetime.fromisoformat(data['estimated_completion'])
        
        return cls(**data)
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert TrainingJob to dictionary"""
        return {
            'job_id': self.job_id,
            'model_name': self.model_name,
            'algorithm': self.algorithm,
            'config': self.config,
            'status': self.status,
            'started_at': self.started_at.isoformat() if self.started_at else None,
            'completed_at': self.completed_at.isoformat() if self.completed_at else None,
            'created_by': self.created_by,
            'job_type': self.job_type,
            'progress': self.progress,
            'current_metrics': self.current_metrics,
            'final_metrics': self.final_metrics,
            'model_path': self.model_path,
            'error_message': self.error_message,
            'tuning_strategy': self.tuning_strategy,
            'max_trials': self.max_trials,
            'estimated_completion': self.estimated_completion.isoformat() if self.estimated_completion else None
        }
    
    def to_json(self) -> str:
        """Convert TrainingJob to JSON string"""
        return json.dumps(self.to_dict(), indent=2)
    
    def is_completed(self) -> bool:
        """Check if job is completed"""
        return self.status in ['completed', 'failed', 'cancelled']
    
    def is_running(self) -> bool:
        """Check if job is currently running"""
        return self.status == 'running'
    
    def is_successful(self) -> bool:
        """Check if job completed successfully"""
        return self.status == 'completed'
    
    def get_duration_seconds(self) -> Optional[float]:
        """Get job duration in seconds"""
        if not self.started_at:
            return None
        
        end_time = self.completed_at or datetime.now()
        return (end_time - self.started_at).total_seconds()
    
    def get_progress_percentage(self) -> int:
        """Get progress as percentage"""
        return max(0, min(100, self.progress))
    
    def __str__(self) -> str:
        duration = self.get_duration_seconds()
        duration_str = f"{duration:.1f}s" if duration else "N/A"
        return f"TrainingJob({self.job_id}, {self.status}, {self.progress}%, {duration_str})"
    
    def __repr__(self) -> str:
        return self.__str__() 