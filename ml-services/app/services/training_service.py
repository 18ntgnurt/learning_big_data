#!/usr/bin/env python3
"""
🏗️ Training Service
ML model training and management service.
"""

import logging
import time
import uuid
from datetime import datetime, timedelta
from typing import Dict, Any, List, Optional
import threading
import queue

logger = logging.getLogger(__name__)

class TrainingService:
    """
    Training Service
    
    Handles ML model training including:
    - Training job management
    - Hyperparameter tuning
    - Model versioning
    - Training monitoring
    """
    
    def __init__(self, config: Dict, mlflow_service=None):
        self.config = config
        self.mlflow_service = mlflow_service
        self.training_jobs = {}
        self.job_queue = queue.Queue()
        self.max_concurrent_jobs = config.get('max_concurrent_jobs', 3)
        self.running_jobs = {}
        
        # Start training worker threads
        self._start_workers()
        
        logger.info("🏗️ Training Service initialized")
    
    def _start_workers(self):
        """Start worker threads for training jobs"""
        for i in range(self.max_concurrent_jobs):
            worker = threading.Thread(target=self._training_worker, daemon=True)
            worker.start()
    
    def _training_worker(self):
        """Worker thread that processes training jobs"""
        while True:
            try:
                job_id = self.job_queue.get(timeout=1)
                if job_id in self.training_jobs:
                    self._execute_training_job(job_id)
                self.job_queue.task_done()
            except queue.Empty:
                continue
            except Exception as e:
                logger.error(f"Training worker error: {e}")
    
    def start_training(self, job_id: str, config: 'TrainingConfig') -> 'TrainingJob':
        """Start a new training job"""
        from models.training_job import TrainingJob
        
        # Create training job
        training_job = TrainingJob(
            job_id=job_id,
            model_name=config.model_name,
            algorithm=config.algorithm,
            config=config.to_dict(),
            status='queued',
            started_at=datetime.now(),
            created_by='system'
        )
        
        # Store job
        self.training_jobs[job_id] = training_job
        
        # Add to queue
        self.job_queue.put(job_id)
        
        logger.info(f"Training job {job_id} queued for {config.model_name}")
        
        return training_job
    
    def _execute_training_job(self, job_id: str):
        """Execute a training job"""
        job = self.training_jobs.get(job_id)
        if not job:
            logger.error(f"Training job {job_id} not found")
            return
        
        try:
            # Update job status
            job.status = 'running'
            job.started_at = datetime.now()
            self.running_jobs[job_id] = job
            
            logger.info(f"Starting training job {job_id}")
            
            # Simulate training process
            self._simulate_training(job)
            
            # Update job status
            job.status = 'completed'
            job.completed_at = datetime.now()
            job.progress = 100
            
            # Create model artifacts
            job.model_path = f"models/{job.model_name}_{job_id}"
            job.metrics = {
                'accuracy': 0.95 + (hash(job_id) % 50) / 1000,
                'precision': 0.93 + (hash(job_id) % 70) / 1000,
                'recall': 0.92 + (hash(job_id) % 80) / 1000,
                'f1_score': 0.94 + (hash(job_id) % 60) / 1000
            }
            
            logger.info(f"Training job {job_id} completed successfully")
            
        except Exception as e:
            logger.error(f"Training job {job_id} failed: {e}")
            job.status = 'failed'
            job.error_message = str(e)
            job.completed_at = datetime.now()
        
        finally:
            # Remove from running jobs
            self.running_jobs.pop(job_id, None)
    
    def _simulate_training(self, job: 'TrainingJob'):
        """Simulate training process with progress updates"""
        total_steps = 100
        
        for step in range(total_steps):
            # Simulate training step
            time.sleep(0.1)  # Simulate work
            
            # Update progress
            job.progress = int((step + 1) / total_steps * 100)
            
            # Simulate some training metrics
            if step % 10 == 0:
                job.current_metrics = {
                    'loss': 1.0 - (step / total_steps) * 0.8,
                    'accuracy': 0.5 + (step / total_steps) * 0.4,
                    'epoch': step // 10
                }
    
    def start_hyperparameter_tuning(self, job_id: str, tuning_request: Dict) -> 'TrainingJob':
        """Start hyperparameter tuning job"""
        from models.training_job import TrainingJob
        
        # Create tuning job
        tuning_job = TrainingJob(
            job_id=job_id,
            model_name=tuning_request['model_name'],
            algorithm=tuning_request['algorithm'],
            config=tuning_request,
            status='queued',
            started_at=datetime.now(),
            created_by='system',
            job_type='hyperparameter_tuning'
        )
        
        # Set tuning-specific attributes
        tuning_job.tuning_strategy = tuning_request.get('tuning_strategy', 'random')
        tuning_job.max_trials = tuning_request.get('max_trials', 50)
        
        # Estimate completion time
        estimated_duration = tuning_job.max_trials * 2  # 2 minutes per trial
        tuning_job.estimated_completion = datetime.now() + timedelta(minutes=estimated_duration)
        
        # Store job
        self.training_jobs[job_id] = tuning_job
        
        # Add to queue
        self.job_queue.put(job_id)
        
        logger.info(f"Hyperparameter tuning job {job_id} queued")
        
        return tuning_job
    
    def get_training_job(self, job_id: str) -> Optional['TrainingJob']:
        """Get training job by ID"""
        return self.training_jobs.get(job_id)
    
    def list_training_jobs(self, limit: int = 100, status: str = None) -> List['TrainingJob']:
        """List training jobs"""
        jobs = list(self.training_jobs.values())
        
        # Filter by status if specified
        if status:
            jobs = [job for job in jobs if job.status == status]
        
        # Sort by creation time (newest first)
        jobs.sort(key=lambda x: x.started_at, reverse=True)
        
        # Apply limit
        return jobs[:limit]
    
    def cancel_training_job(self, job_id: str) -> bool:
        """Cancel a training job"""
        job = self.training_jobs.get(job_id)
        if not job:
            return False
        
        if job.status in ['queued', 'running']:
            job.status = 'cancelled'
            job.completed_at = datetime.now()
            
            # Remove from running jobs if applicable
            self.running_jobs.pop(job_id, None)
            
            logger.info(f"Training job {job_id} cancelled")
            return True
        
        return False
    
    def get_training_statistics(self) -> Dict[str, Any]:
        """Get training service statistics"""
        total_jobs = len(self.training_jobs)
        
        # Count jobs by status
        status_counts = {}
        for job in self.training_jobs.values():
            status_counts[job.status] = status_counts.get(job.status, 0) + 1
        
        # Calculate success rate
        completed_jobs = status_counts.get('completed', 0)
        failed_jobs = status_counts.get('failed', 0)
        success_rate = completed_jobs / (completed_jobs + failed_jobs) if (completed_jobs + failed_jobs) > 0 else 0
        
        return {
            'total_jobs': total_jobs,
            'status_breakdown': status_counts,
            'success_rate': success_rate,
            'currently_running': len(self.running_jobs),
            'queue_size': self.job_queue.qsize(),
            'max_concurrent_jobs': self.max_concurrent_jobs,
            'timestamp': datetime.now().isoformat()
        }
    
    def get_service_status(self) -> Dict[str, Any]:
        """Get training service status"""
        return {
            'status': 'healthy',
            'total_jobs': len(self.training_jobs),
            'running_jobs': len(self.running_jobs),
            'queue_size': self.job_queue.qsize(),
            'timestamp': datetime.now().isoformat()
        } 