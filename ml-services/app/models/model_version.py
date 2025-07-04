#!/usr/bin/env python3
"""
📦 Model Version
Data model for ML model versions.
"""

from datetime import datetime
from typing import Dict, Any, Optional
import json

class ModelVersion:
    """
    Model Version data model
    
    Represents a version of a registered ML model.
    """
    
    def __init__(
        self,
        name: str,
        version: str,
        stage: str,
        run_id: str,
        creation_time: str,
        description: Optional[str] = None,
        **kwargs
    ):
        self.name = name
        self.version = version
        self.stage = stage  # None, Staging, Production, Archived
        self.run_id = run_id
        self.creation_time = creation_time
        self.description = description or ''
        
        # Additional fields
        for key, value in kwargs.items():
            setattr(self, key, value)
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'ModelVersion':
        """Create ModelVersion from dictionary"""
        return cls(**data)
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert ModelVersion to dictionary"""
        return {
            'name': self.name,
            'version': self.version,
            'stage': self.stage,
            'run_id': self.run_id,
            'creation_time': self.creation_time,
            'description': self.description
        }
    
    def to_json(self) -> str:
        """Convert ModelVersion to JSON string"""
        return json.dumps(self.to_dict(), indent=2)
    
    def is_production(self) -> bool:
        """Check if model version is in production"""
        return self.stage == 'Production'
    
    def is_staging(self) -> bool:
        """Check if model version is in staging"""
        return self.stage == 'Staging'
    
    def __str__(self) -> str:
        return f"ModelVersion({self.name} v{self.version}, {self.stage})"
    
    def __repr__(self) -> str:
        return self.__str__() 