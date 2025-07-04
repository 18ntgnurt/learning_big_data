# 🔍 Kafka Monitoring Complete Guide

This guide covers all the monitoring options available for your Kafka setup.

## 🎯 Quick Access

- **🌐 Web UI**: http://localhost:9090 (Already running!)
- **🔧 Shell Script**: `./monitor-kafka.sh`
- **☕ Java Monitoring**: `mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.MonitoringTest"`

## 📊 Monitoring Options Overview

### 1. 🌐 **Kafka UI (Web Interface)** - **RECOMMENDED**

**Access**: http://localhost:9090

**Features:**
- ✅ Real-time cluster overview
- ✅ Topic management and browsing
- ✅ Consumer group monitoring with lag
- ✅ Message browsing and searching
- ✅ Schema Registry integration
- ✅ Broker performance metrics
- ✅ Producer/Consumer statistics

**How to Use:**
1. Open http://localhost:9090 in your browser
2. Navigate through:
   - **Topics** - View all topics, partitions, message counts
   - **Consumers** - Monitor consumer groups and lag
   - **Brokers** - Check broker health
   - **Messages** - Browse actual message content

---

### 2. 🔧 **Command Line Monitoring**

**Script**: `./monitor-kafka.sh`

```bash
# Run the monitoring script
./monitor-kafka.sh
```

**What it shows:**
- 📋 All Kafka topics
- 📊 Topic details and partitions
- 👥 Consumer groups
- 📈 Consumer group offsets and lag
- 🔢 Message counts
- 📱 Broker API details

---

### 3. ☕ **Java Application Monitoring**

#### Quick Monitoring Test
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.MonitoringTest"
```

#### One-time Monitoring
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.KafkaMonitor"
```

#### Live Monitoring (Refreshes every 5 seconds)
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.KafkaMonitor" -Dexec.args="live 5"
```

**Features:**
- 🏢 Cluster information
- 📁 Topic details with message counts
- 👥 Consumer group details with lag calculation
- 🔄 Live monitoring with auto-refresh
- ✅ Connection testing

---

### 4. 🐳 **Docker Container Monitoring**

#### Check Container Status
```bash
docker ps | grep kafka
```

#### View Kafka Logs
```bash
docker logs data_engineering_kafka
```

#### View Zookeeper Logs
```bash
docker logs data_engineering_zookeeper
```

---

## 📈 **Key Metrics to Monitor**

### 🎯 **Production-Critical Metrics**

1. **Consumer Lag** 📊
   - How far behind consumers are
   - Available in: Kafka UI, Java Monitor
   - Critical for real-time applications

2. **Message Throughput** 🚀
   - Messages per second produced/consumed
   - Available in: Kafka UI

3. **Broker Health** ❤️
   - CPU, memory, disk usage
   - Available in: Docker logs, Kafka UI

4. **Topic Growth** 📈
   - Message count growth over time
   - Available in: Java Monitor, Kafka UI

5. **Partition Distribution** ⚖️
   - Even distribution across brokers
   - Available in: All monitoring tools

---

## 🚨 **Monitoring During Development**

### When Testing Producer/Consumer:

1. **Before Running Tests:**
```bash
# Start monitoring in one terminal
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.KafkaMonitor" -Dexec.args="live 5"
```

2. **Run Your Tests:**
```bash
# In another terminal
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.producer.ProducerTest"
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.consumer.ConsumerTest"
```

3. **Watch Real-time Updates:**
   - See topics being created
   - Monitor message counts
   - Track consumer group formation
   - Observe offset changes

---

## 🔧 **Advanced Monitoring Commands**

### Manual Kafka CLI Commands (in Docker)

```bash
# List topics
docker exec -it data_engineering_kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describe specific topic
docker exec -it data_engineering_kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic sales-events

# List consumer groups
docker exec -it data_engineering_kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Describe consumer group
docker exec -it data_engineering_kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group sales-processor-group

# Get topic offsets
docker exec -it data_engineering_kafka kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic sales-events --time -1
```

---

## 🎨 **Monitoring Workflow Recommendations**

### 🏁 **Getting Started**
1. Start with **Kafka UI** (http://localhost:9090) - easiest to understand
2. Use **Java Monitor** for programmatic checks
3. Use **Shell script** for quick CLI checks

### 🔄 **During Development**
1. Keep Kafka UI open in browser
2. Run Java live monitoring: `mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.KafkaMonitor" -Dexec.args="live 10"`
3. Watch for:
   - Topics being created
   - Messages being produced
   - Consumers joining groups
   - Offset commits

### 🚀 **Production Monitoring** (Future)
1. Set up dedicated monitoring stack (Prometheus + Grafana)
2. Configure alerts for consumer lag
3. Monitor disk space and broker performance
4. Set up log aggregation

---

## 🐛 **Troubleshooting**

### No Topics Found?
```bash
# Create the sales-events topic first
docker exec -it data_engineering_kafka kafka-topics --bootstrap-server localhost:9092 --create --topic sales-events --partitions 3 --replication-factor 1
```

### No Consumer Groups?
- Run a consumer first:
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.consumer.ConsumerTest"
```

### Connection Issues?
```bash
# Check if Kafka is running
docker ps | grep kafka

# Restart if needed
docker-compose -f docker-compose-kafka.yml restart
```

### Java Monitoring Not Working?
```bash
# Compile first
mvn compile

# Check connectivity
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.MonitoringTest"
```

---

## 🎯 **Next Steps**

1. **Explore Kafka UI**: http://localhost:9090
2. **Run End-to-End Test** while monitoring:
   ```bash
   # Terminal 1: Start monitoring
   mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.KafkaMonitor" -Dexec.args="live 5"
   
   # Terminal 2: Run tests
   mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.EndToEndTest"
   ```
3. **Experiment with monitoring** while running your producer/consumer tests

---

## 📚 **Additional Resources**

- **Kafka UI Documentation**: https://docs.kafka-ui.provectus.io/
- **Kafka Monitoring Best Practices**: https://kafka.apache.org/documentation/#monitoring
- **AdminClient API**: https://kafka.apache.org/documentation/#adminapi

---

**💡 Pro Tip**: Keep the Kafka UI open in a browser tab while developing - it's the fastest way to see what's happening in your Kafka cluster! 