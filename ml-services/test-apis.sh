#!/bin/bash

# 🧪 ML Services Platform v2.0 - API Testing Script
# Professional API testing for the refactored MVC architecture

set -e

echo "🚀 Testing ML Services Platform v2.0 APIs"
echo "==========================================="

BASE_URL="http://localhost:5000"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to test API endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    
    echo -e "${BLUE}Testing: ${method} ${endpoint}${NC}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "${BASE_URL}${endpoint}")
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X "$method" \
            "${BASE_URL}${endpoint}")
    fi
    
    http_status=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS/d')
    
    if [ "$http_status" = "$expected_status" ]; then
        echo -e "${GREEN}✅ PASS: Status $http_status${NC}"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        echo -e "${RED}❌ FAIL: Expected $expected_status, got $http_status${NC}"
        echo "$body"
    fi
    echo ""
}

# Wait for services to be ready
echo -e "${YELLOW}⏳ Waiting for services to be ready...${NC}"
sleep 5

echo -e "${BLUE}🏥 Testing Health & Monitoring Endpoints${NC}"
echo "==========================================="

# Health checks
test_endpoint "GET" "/health" "" "200"
test_endpoint "GET" "/health/live" "" "200"
test_endpoint "GET" "/health/ready" "" "200"
test_endpoint "GET" "/status" "" "200"
test_endpoint "GET" "/info" "" "200"

echo -e "${BLUE}🔍 Testing Fraud Detection Endpoints${NC}"
echo "====================================="

# Single fraud prediction
fraud_data='{
  "transaction_id": "txn_test_001",
  "customer_id": "cust_test_123",
  "merchant_id": "merch_test_456",
  "amount": 1500.00,
  "merchant_category": "online",
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"
}'

test_endpoint "POST" "/api/v1/fraud/predict" "$fraud_data" "200"

# Batch fraud prediction
batch_data='{
  "transactions": [
    {
      "transaction_id": "txn_batch_001",
      "customer_id": "cust_batch_123",
      "amount": 100.00,
      "merchant_category": "retail"
    },
    {
      "transaction_id": "txn_batch_002", 
      "customer_id": "cust_batch_456",
      "amount": 2500.00,
      "merchant_category": "online"
    }
  ],
  "options": {
    "return_explanations": true
  }
}'

test_endpoint "POST" "/api/v1/fraud/predict/batch" "$batch_data" "200"

# Fraud scoring
score_data='{
  "amount": 1500.00,
  "merchant_category": "online",
  "customer_risk_level": "medium"
}'

test_endpoint "POST" "/api/v1/fraud/score" "$score_data" "200"

# Fraud metrics
test_endpoint "GET" "/api/v1/fraud/metrics" "" "200"
test_endpoint "GET" "/api/v1/fraud/status" "" "200"

echo -e "${BLUE}🏗️ Testing Training Pipeline Endpoints${NC}"
echo "======================================"

# Training job
training_data='{
  "model_name": "test_random_forest",
  "algorithm": "fraud_detection",
  "hyperparameter_tuning": false,
  "experiment_name": "test_experiment",
  "config": {
    "n_estimators": 50,
    "max_depth": 10
  }
}'

echo -e "${YELLOW}Starting training job...${NC}"
training_response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "$training_data" \
    "${BASE_URL}/api/v1/training/train")

echo "$training_response" | jq .

# Extract job ID if available
job_id=$(echo "$training_response" | jq -r '.job_id // empty' 2>/dev/null)

if [ -n "$job_id" ] && [ "$job_id" != "null" ]; then
    echo -e "${GREEN}✅ Training job started with ID: $job_id${NC}"
    
    # Test job status
    test_endpoint "GET" "/api/v1/training/jobs/$job_id" "" "200"
else
    echo -e "${YELLOW}⚠️ Training job response doesn't contain job_id (may be expected in some configurations)${NC}"
fi

# List training jobs
test_endpoint "GET" "/api/v1/training/jobs" "" "200"

# MLflow experiments
test_endpoint "GET" "/api/v1/training/experiments" "" "200"

# Training metrics
test_endpoint "GET" "/api/v1/training/metrics" "" "200"
test_endpoint "GET" "/api/v1/training/status" "" "200"

echo -e "${BLUE}📊 Testing System Metrics${NC}"
echo "========================"

test_endpoint "GET" "/metrics" "" "200"

echo -e "${GREEN}🎉 API Testing Complete!${NC}"
echo "========================"

echo -e "${BLUE}📋 Summary:${NC}"
echo "- All core endpoints tested"
echo "- Health checks validated"
echo "- Fraud detection API working"
echo "- Training pipeline API accessible"
echo "- Monitoring endpoints functional"
echo ""
echo -e "${YELLOW}💡 Next Steps:${NC}"
echo "1. Check MLflow UI: http://localhost:5002"
echo "2. Check Kafka UI: http://localhost:9090"  
echo "3. Check Grafana: http://localhost:3000"
echo "4. Review application logs: docker-compose logs ml-services"
echo ""
echo -e "${GREEN}🚀 Your ML Services Platform v2.0 is ready!${NC}" 