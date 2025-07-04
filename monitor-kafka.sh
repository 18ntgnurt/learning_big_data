#!/bin/bash

# Kafka Monitoring Script
# This script provides command-line monitoring for your Kafka setup

echo "🔍 Kafka Cluster Monitoring"
echo "============================"

# Function to run Kafka commands in Docker
kafka_cmd() {
    docker exec -it data_engineering_kafka kafka-$1 --bootstrap-server localhost:9092 "${@:2}"
}

echo ""
echo "📋 1. List Topics:"
kafka_cmd topics --list

echo ""
echo "📊 2. Topic Details (sales-events):"
kafka_cmd topics --describe --topic sales-events

echo ""
echo "👥 3. Consumer Groups:"
kafka_cmd consumer-groups --list

echo ""
echo "📈 4. Consumer Group Details (if exists):"
kafka_cmd consumer-groups --describe --group sales-processor-group

echo ""
echo "🔢 5. Topic Message Count (approximate):"
kafka_cmd run-class kafka.tools.GetOffsetShell --topic sales-events --time -1

echo ""
echo "📱 6. Broker Details:"
kafka_cmd broker-api-versions

echo ""
echo "💡 Tip: Open Kafka UI at http://localhost:9090 for visual monitoring"
echo "💡 Run this script anytime: ./monitor-kafka.sh" 