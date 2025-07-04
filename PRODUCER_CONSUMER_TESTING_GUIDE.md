# 🧪 Producer-Consumer Testing Complete Guide

This guide shows you **5 different ways** to test your Kafka producer and consumer together.

## 🎯 Quick Start - All Methods

### ✅ **Method 1: Automated End-to-End Test (RECOMMENDED)**
**Perfect for**: Quick verification that everything works
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="auto"
```

### 🎮 **Method 2: Interactive Testing**
**Perfect for**: Manual testing and experimenting
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="interactive"
```

### 🖥️ **Method 3: Separate Terminals**
**Perfect for**: Independent control of producer and consumer
```bash
# Terminal 1: Start Consumer
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="consumer-only"

# Terminal 2: Start Producer
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="producer-only"
```

### 🔥 **Method 4: Load Testing**
**Perfect for**: Performance testing
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="load"
```

### 📊 **Method 5: Monitoring + Testing**
**Perfect for**: Watching real-time data flow
```bash
# Terminal 1: Start Live Monitoring
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.KafkaMonitor" -Dexec.args="live 5"

# Terminal 2: Run Any Test
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="auto"
```

---

## 📖 Detailed Method Explanations

### 🤖 Method 1: Automated End-to-End Test

**What it does:**
- Starts consumer in background
- Creates producer 
- Sends 3 test messages
- Sends 1 high-value message (triggers analytics)
- Waits for processing
- Cleans up everything

**Usage:**
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="auto"
```

**Example Output:**
```
🤖 AUTOMATED END-TO-END TEST
===========================
📥 Starting consumer...
✅ SalesEventConsumer initialized for group: sales-processor-group
🔄 Consumer started in background thread
📤 Starting producer...
📦 Sending test messages...
   📤 Sending: AUTO_TEST_001 | Product: Laptop
   ✅ Sent
📥 Received 1 records
🔄 Processing record: key=AUTO_TEST_001, partition=0, offset=0
💼 Processing: SalesRecord{id='AUTO_TEST_001', customer='CUST002', product='Laptop', quantity=1, amount=542.23, date=2025-07-02T16:07:45.123}
✅ Successfully processed sales record: AUTO_TEST_001
```

---

### 🎮 Method 2: Interactive Testing

**What it does:**
- Starts consumer in background
- Gives you interactive producer commands
- Type commands to send different types of messages
- Watch real-time processing

**Usage:**
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="interactive"
```

**Interactive Commands:**
- `send` - Send a random sales record
- `high` - Send high-value transaction (triggers analytics)
- `batch` - Send 5 messages at once
- `quit` - Exit

**Example Session:**
```
🎯 INTERACTIVE MODE STARTED
==========================
Commands:
  'send' - Send a random message
  'high' - Send high-value message
  'batch' - Send 5 messages
  'quit' - Exit

🎮 Command: send
📤 Sending: INTERACTIVE_001
✅ Sent!
📥 Received 1 records
💼 Processing: SalesRecord{id='INTERACTIVE_001', ...}

🎮 Command: high
💰 Sending high-value: HIGH_VALUE_002 ($10000.0)
✅ Sent!
🚨 HIGH VALUE SALE: $10000.0

🎮 Command: quit
```

---

### 🖥️ Method 3: Separate Terminals

**What it does:**
- Gives you full control over consumer and producer separately
- Perfect for debugging individual components
- Can restart one without affecting the other

**Setup:**

**Terminal 1 - Consumer:**
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="consumer-only"
```

**Terminal 2 - Producer:**
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="producer-only"
```

**Producer Commands:**
- `send` - Send one message
- `batch 10` - Send 10 messages
- `quit` - Exit

**Benefits:**
- ✅ Independent control
- ✅ Easy debugging
- ✅ Can restart consumer/producer separately
- ✅ Perfect for troubleshooting

---

### 🔥 Method 4: Load Testing

**What it does:**
- Sends 50 messages in batches of 10
- Measures throughput (messages/second)
- Tests system under load
- Shows performance metrics

**Usage:**
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="load"
```

**Example Output:**
```
🔥 LOAD TEST
============
📥 Starting consumer...
📤 Starting producer...
🚀 Starting load test: 50 messages in batches of 10

📦 Batch 1/5
📤 📤 📤 📤 📤 📤 📤 📤 📤 📤  ✅ Batch 1 sent!

📊 LOAD TEST RESULTS:
====================
Messages sent: 50
Duration: 2341ms
Rate: 21.37 messages/second
```

---

### 📊 Method 5: Monitoring + Testing

**What it does:**
- Shows real-time monitoring while testing
- See topics being created
- Watch message counts grow
- Monitor consumer lag
- Observe partition assignments

**Setup:**

**Terminal 1 - Start Monitoring:**
```bash
# Live monitoring (refreshes every 5 seconds)
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.KafkaMonitor" -Dexec.args="live 5"
```

**Terminal 2 - Run Tests:**
```bash
# Run any test while monitoring
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.ProducerConsumerTestSuite" -Dexec.args="auto"
```

**What You'll See:**
- 📊 Topic message counts updating in real-time
- 👥 Consumer groups appearing
- 📈 Offset changes
- 🔄 Partition assignments

---

## 🎨 **Testing Workflows & Scenarios**

### 🏁 **Getting Started Workflow**
1. **Quick Test**: `auto` mode to verify everything works
2. **Explore**: `interactive` mode to understand the system
3. **Debug**: `separate terminals` if you find issues

### 🔧 **Development Workflow**
1. **Code Changes**: Make changes to producer/consumer
2. **Quick Verify**: Run `auto` test
3. **Deep Test**: Use `interactive` or `load` testing
4. **Monitor**: Use live monitoring during development

### 🚨 **Troubleshooting Workflow**
1. **Separate Terminals**: Test producer and consumer independently
2. **Simple Producer**: Use the simple producer test for debugging
3. **Monitoring**: Check cluster status and consumer groups
4. **Logs**: Check Docker logs if needed

---

## 🔍 **Additional Testing Commands**

### Simple Producer Test (for debugging):
```bash
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.SimpleProducerTest"
```

### Individual Component Tests:
```bash
# Test producer only
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.producer.ProducerTest"

# Test consumer only
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.consumer.ConsumerTest"
```

### Monitoring Tests:
```bash
# One-time monitoring snapshot
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.monitoring.MonitoringTest"

# Shell-based monitoring
./monitor-kafka.sh
```

---

## 🐛 **Common Issues & Solutions**

### ❌ Producer Fails to Send
**Check:**
1. Is Kafka running? `docker ps | grep kafka`
2. Does topic exist? Use Kafka UI or monitoring
3. Are there validation errors? Check record validation

**Solution:**
```bash
# Restart Kafka if needed
docker-compose -f docker-compose-kafka.yml restart

# Run simple producer test
mvn exec:java -Dexec.mainClass="com.dataengineering.kafka.testing.SimpleProducerTest"
```

### ❌ Consumer Not Receiving Messages
**Check:**
1. Is consumer running and subscribed?
2. Are messages being produced?
3. Check consumer group status

**Solution:**
```bash
# Use separate terminals to test independently
# Terminal 1: consumer-only
# Terminal 2: producer-only
```

### ❌ JSON Serialization Issues
**Check:**
1. LocalDateTime support in ObjectMapper
2. Required fields in SalesRecord
3. Validation errors

**Solution:** Already fixed in the producer with `objectMapper.findAndRegisterModules()`

---

## 🎯 **Best Practices**

### ✅ **For Learning:**
1. Start with **automated test** to see it working
2. Use **interactive mode** to understand the flow
3. Try **monitoring + testing** to see real-time data

### ✅ **For Development:**
1. Use **separate terminals** for debugging
2. Keep **monitoring** open in a browser tab
3. Run **load tests** to check performance

### ✅ **For Production:**
1. Implement proper error handling
2. Add metrics and monitoring
3. Use batch sending for better performance

---

## 🚀 **Next Steps**

1. **Try all methods** to understand different use cases
2. **Modify the test data** in `ProducerConsumerTestSuite`
3. **Add your own business logic** to the consumer
4. **Implement error handling** and retry logic
5. **Add more test scenarios** based on your needs

---

**💡 Pro Tip**: Keep the Kafka UI (http://localhost:9090) open while testing to see the visual representation of what's happening! 