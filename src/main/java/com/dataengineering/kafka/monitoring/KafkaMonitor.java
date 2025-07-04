package com.dataengineering.kafka.monitoring;

import com.dataengineering.kafka.config.KafkaConfig;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * KafkaMonitor provides programmatic monitoring of Kafka cluster
 * 
 * Features:
 * - Topic information and statistics
 * - Consumer group monitoring
 * - Partition and offset tracking
 * - Cluster health checks
 * - Real-time metrics
 */
public class KafkaMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaMonitor.class);
    private AdminClient adminClient;
    
    public KafkaMonitor() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        
        this.adminClient = AdminClient.create(props);
        
        System.out.println("✅ KafkaMonitor initialized");
    }
    
    /**
     * Get comprehensive cluster information
     */
    public void showClusterInfo() {
        try {
            System.out.println("\n🏢 KAFKA CLUSTER INFORMATION");
            System.out.println("================================");
            
            // Cluster details
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            String clusterId = clusterResult.clusterId().get();
            Collection<Node> nodes = clusterResult.nodes().get();
            
            System.out.println("🆔 Cluster ID: " + clusterId);
            System.out.println("🖥️  Brokers: " + nodes.size());
            
            for (Node node : nodes) {
                System.out.println("   ├─ Broker " + node.id() + ": " + node.host() + ":" + node.port());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to get cluster info: " + e.getMessage());
        }
    }
    
    /**
     * Get detailed topic information
     */
    public void showTopicInfo() {
        try {
            System.out.println("\n📋 TOPIC INFORMATION");
            System.out.println("===================");
            
            // List all topics
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topicNames = topicsResult.names().get();
            
            if (topicNames.isEmpty()) {
                System.out.println("❌ No topics found");
                return;
            }
            
            // Describe topics
            DescribeTopicsResult describeResult = adminClient.describeTopics(topicNames);
            Map<String, TopicDescription> topicDescriptions = describeResult.all().get();
            
            for (TopicDescription topic : topicDescriptions.values()) {
                System.out.println("\n📁 Topic: " + topic.name());
                System.out.println("   ├─ Partitions: " + topic.partitions().size());
                System.out.println("   ├─ Internal: " + topic.isInternal());
                
                // Partition details
                for (TopicPartitionInfo partition : topic.partitions()) {
                    System.out.println("   ├─ Partition " + partition.partition() + 
                                     " | Leader: " + partition.leader().id() + 
                                     " | Replicas: " + partition.replicas().size());
                }
                
                // Get partition sizes
                showTopicSize(topic.name());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to get topic info: " + e.getMessage());
        }
    }
    
    /**
     * Show topic message count and size
     */
    private void showTopicSize(String topicName) {
        try {
            Properties consumerProps = KafkaConfig.getConsumerProperties("monitor-group");
            consumerProps.put("enable.auto.commit", "false");
            
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
            
            // Get all partitions for topic
            List<TopicPartition> partitions = new ArrayList<>();
            consumer.partitionsFor(topicName).forEach(partitionInfo -> 
                partitions.add(new TopicPartition(topicName, partitionInfo.partition()))
            );
            
            if (!partitions.isEmpty()) {
                // Get end offsets (total messages)
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
                long totalMessages = endOffsets.values().stream().mapToLong(Long::longValue).sum();
                
                System.out.println("   └─ Total Messages: " + totalMessages);
            }
            
            consumer.close();
            
        } catch (Exception e) {
            System.out.println("   └─ Could not get message count: " + e.getMessage());
        }
    }
    
    /**
     * Monitor consumer groups
     */
    public void showConsumerGroups() {
        try {
            System.out.println("\n👥 CONSUMER GROUPS");
            System.out.println("=================");
            
            // List consumer groups
            ListConsumerGroupsResult groupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> groups = groupsResult.all().get();
            
            if (groups.isEmpty()) {
                System.out.println("❌ No consumer groups found");
                return;
            }
            
            for (ConsumerGroupListing group : groups) {
                System.out.println("\n👥 Group: " + group.groupId());
                System.out.println("   ├─ State: " + (group.state().isPresent() ? group.state().get() : "UNKNOWN"));
                
                // Get detailed group info
                showConsumerGroupDetails(group.groupId());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to get consumer groups: " + e.getMessage());
        }
    }
    
    /**
     * Show detailed consumer group information
     */
    private void showConsumerGroupDetails(String groupId) {
        try {
            // Describe consumer group
            DescribeConsumerGroupsResult describeResult = adminClient.describeConsumerGroups(
                Collections.singletonList(groupId)
            );
            ConsumerGroupDescription groupDesc = describeResult.all().get().get(groupId);
            
            System.out.println("   ├─ Members: " + groupDesc.members().size());
            System.out.println("   ├─ Coordinator: " + groupDesc.coordinator().id());
            
            // Show member details
            for (MemberDescription member : groupDesc.members()) {
                System.out.println("   │  ├─ Member: " + member.consumerId());
                System.out.println("   │  │  ├─ Host: " + member.host());
                System.out.println("   │  │  └─ Assignments: " + member.assignment().topicPartitions().size());
            }
            
            // Get consumer group offsets
            showConsumerGroupOffsets(groupId);
            
        } catch (Exception e) {
            System.out.println("   └─ Could not get group details: " + e.getMessage());
        }
    }
    
    /**
     * Show consumer group lag information
     */
    private void showConsumerGroupOffsets(String groupId) {
        try {
            // List consumer group offsets
            ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();
            
            if (!offsets.isEmpty()) {
                System.out.println("   └─ Topic Offsets:");
                
                // Calculate lag for each partition
                Properties consumerProps = KafkaConfig.getConsumerProperties("monitor-group-temp");
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
                
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(offsets.keySet());
                
                for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                    TopicPartition tp = entry.getKey();
                    long currentOffset = entry.getValue().offset();
                    long endOffset = endOffsets.getOrDefault(tp, 0L);
                    long lag = Math.max(0, endOffset - currentOffset);
                    
                    System.out.println("      ├─ " + tp.topic() + "[" + tp.partition() + 
                                     "] Offset: " + currentOffset + 
                                     " | End: " + endOffset + 
                                     " | Lag: " + lag);
                }
                
                consumer.close();
            }
            
        } catch (Exception e) {
            System.out.println("   └─ Could not get offsets: " + e.getMessage());
        }
    }
    
    /**
     * Live monitoring with refresh capability
     */
    public void startLiveMonitoring(int refreshSeconds) {
        System.out.println("\n🔄 STARTING LIVE MONITORING");
        System.out.println("Refresh interval: " + refreshSeconds + " seconds");
        System.out.println("Press Ctrl+C to stop\n");
        
        try {
            while (true) {
                System.out.println("\n" + "=".repeat(60));
                System.out.println("📊 LIVE KAFKA MONITORING - " + new Date());
                System.out.println("=".repeat(60));
                
                showClusterInfo();
                showTopicInfo();
                showConsumerGroups();
                
                System.out.println("\n⏳ Next refresh in " + refreshSeconds + " seconds...");
                Thread.sleep(refreshSeconds * 1000);
            }
        } catch (InterruptedException e) {
            System.out.println("\n🛑 Monitoring stopped");
        } catch (Exception e) {
            System.err.println("❌ Monitoring error: " + e.getMessage());
        }
    }
    
    /**
     * Test Kafka connectivity
     */
    public boolean testConnection() {
        try {
            adminClient.describeCluster().clusterId().get(5, TimeUnit.SECONDS);
            System.out.println("✅ Kafka connection successful");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Kafka connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Close resources
     */
    public void close() {
        if (adminClient != null) {
            adminClient.close();
            System.out.println("✅ KafkaMonitor closed");
        }
    }
    
    /**
     * Main method for standalone monitoring
     */
    public static void main(String[] args) {
        KafkaMonitor monitor = new KafkaMonitor();
        
        try {
            if (!monitor.testConnection()) {
                System.err.println("❌ Cannot connect to Kafka. Make sure it's running!");
                return;
            }
            
            if (args.length > 0 && "live".equals(args[0])) {
                int refreshSeconds = args.length > 1 ? Integer.parseInt(args[1]) : 10;
                monitor.startLiveMonitoring(refreshSeconds);
            } else {
                monitor.showClusterInfo();
                monitor.showTopicInfo();
                monitor.showConsumerGroups();
            }
            
        } finally {
            monitor.close();
        }
    }
} 