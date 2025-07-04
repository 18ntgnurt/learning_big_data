# 📊 Big Data Platform Monitoring Stack

This directory contains the unified monitoring infrastructure for the entire Big Data platform, providing comprehensive observability across all services.

## 🏗️ Architecture Overview

The monitoring stack is designed as a standalone, modular system that can monitor the entire Big Data platform:

```
┌─────────────────────────────────────────────────────────────────┐
│                    MONITORING STACK                            │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   METRICS       │      LOGS       │        ALERTS              │
│                 │                 │                            │
│ Prometheus ──┐  │ Loki ────────┐  │ AlertManager ──┐           │
│              │  │              │  │                │           │
│ Exporters:   │  │ Promtail ────┘  │ Notifications: │           │
│ • Node       │  │                 │ • Email        │           │
│ • cAdvisor   │  │ Log Sources:    │ • Slack        │           │
│ • Redis      │  │ • Containers    │ • PagerDuty    │           │
│ • Kafka      │  │ • Applications  │                │           │
│ • Postgres   │  │ • System        │ Alert Rules:   │           │
│ • MySQL      │  │                 │ • Thresholds   │           │
│ • Custom     │  │                 │ • Predictions  │           │
│              │  │                 │ • Anomalies    │           │
└──────────────┼──┴─────────────────┼─────────────────┼───────────┘
               │                    │                 │
               └────────────────────┼─────────────────┘
                                    │
              ┌─────────────────────▼─────────────────────┐
              │              GRAFANA                      │
              │                                           │
              │ Dashboards:                              │
              │ • Platform Overview                      │
              │ • ETL & Data Processing                  │
              │ • Fraud Detection & ML                   │
              │ • Kafka & Streaming                      │
              │ • Infrastructure                         │
              │ • Business Metrics                       │
              └───────────────────────────────────────────┘
```

## 🚀 Quick Start

### 1. Start the Monitoring Stack

```bash
# Navigate to monitoring directory
cd infrastructure/monitoring

# Start all monitoring services
docker-compose -f docker-compose.monitoring.yml up -d

# Check status
docker-compose -f docker-compose.monitoring.yml ps
```

### 2. Access Monitoring Services

| Service | URL | Credentials | Purpose |
|---------|-----|-------------|---------|
| **Grafana** | http://localhost:3000 | admin/admin123 | Dashboards & Visualization |
| **Prometheus** | http://localhost:9090 | - | Metrics & Queries |
| **AlertManager** | http://localhost:9093 | - | Alert Management |
| **Loki** | http://localhost:3100 | - | Log Aggregation API |

### 3. Network Integration

The monitoring stack uses its own network (`bigdata-monitoring`) but can connect to the main platform:

```bash
# Connect monitoring to main platform network
docker network connect bigdata-network bigdata-prometheus
docker network connect bigdata-network bigdata-grafana
```

## 📋 Services Overview

### Core Monitoring Services

#### 🔍 **Prometheus** (Port: 9090)
- **Purpose**: Time-series metrics collection and storage
- **Retention**: 30 days
- **Scrape Interval**: 5-30s depending on service
- **Config**: `prometheus/prometheus.yml`

#### 📊 **Grafana** (Port: 3000)
- **Purpose**: Metrics visualization and dashboards
- **Features**: 
  - Pre-configured dashboards
  - Multiple data sources
  - Alert management
  - Image rendering
- **Config**: `grafana/` directory

#### 🚨 **AlertManager** (Port: 9093)
- **Purpose**: Alert routing and notifications
- **Features**:
  - Email notifications
  - Slack integration
  - Alert grouping and inhibition
  - Escalation policies
- **Config**: `alertmanager/alertmanager.yml`

### System Monitoring

#### 🖥️ **Node Exporter** (Port: 9100)
- **Purpose**: System metrics (CPU, memory, disk, network)
- **Metrics**: 
  - System load and uptime
  - Memory usage and swap
  - Disk I/O and filesystem usage
  - Network interface statistics

#### 🐳 **cAdvisor** (Port: 8080)
- **Purpose**: Container resource usage metrics
- **Metrics**:
  - CPU usage per container
  - Memory consumption
  - Network I/O
  - Filesystem usage

### Application Monitoring

#### 🔴 **Redis Exporter** (Port: 9121)
- **Purpose**: Redis (Feature Store) metrics
- **Metrics**: Commands, connections, memory, persistence

#### 🎯 **Kafka Exporter** (Port: 9308)
- **Purpose**: Kafka cluster metrics
- **Metrics**: Topics, partitions, consumer lag, broker performance

#### 🐘 **PostgreSQL Exporter** (Port: 9187)
- **Purpose**: PostgreSQL database metrics
- **Metrics**: Connections, queries, locks, replication

#### 🐬 **MySQL Exporter** (Port: 9104)
- **Purpose**: MySQL database metrics
- **Metrics**: Connections, queries, InnoDB metrics

#### 🕵️ **Blackbox Exporter** (Port: 9115)
- **Purpose**: Endpoint monitoring (HTTP, TCP, ICMP)
- **Probes**: Health checks, connectivity tests
- **Config**: `blackbox/blackbox.yml`

### Custom Exporters

#### 🧠 **MLflow Exporter** (Port: 9401)
- **Purpose**: MLflow tracking server metrics
- **Metrics**: Experiments, runs, models, artifacts

#### 🔍 **Fraud Detection Exporter** (Port: 9402)
- **Purpose**: Fraud detection service metrics
- **Metrics**: Predictions, accuracy, performance

#### 📊 **Data Quality Exporter** (Port: 9403)
- **Purpose**: Data quality monitoring
- **Metrics**: Quality scores, drift detection, validation results

#### ⏱️ **Kafka Lag Exporter** (Port: 8000)
- **Purpose**: Consumer lag monitoring
- **Metrics**: Per-topic consumer lag, processing rates

### Log Management

#### 📝 **Loki** (Port: 3100)
- **Purpose**: Log aggregation and storage
- **Retention**: 7 days
- **Features**: Label-based indexing, compression
- **Config**: `loki/loki-config.yml`

#### 🚚 **Promtail**
- **Purpose**: Log collection agent
- **Sources**: Docker containers, system logs
- **Processing**: Log parsing, labeling, filtering
- **Config**: `promtail/promtail-config.yml`

## 📈 Metrics Categories

### Infrastructure Metrics
- System resources (CPU, memory, disk, network)
- Container performance
- Database performance
- Message queue metrics

### Application Metrics
- Request rates and latency
- Error rates and success rates
- Business KPIs
- Custom application metrics

### ML/AI Metrics
- Model performance
- Prediction accuracy
- Data drift detection
- Feature store utilization

### Business Metrics
- Transaction volumes
- Fraud detection rates
- Processing throughput
- Revenue impact

## 🔔 Alerting Strategy

### Alert Severity Levels

#### 🚨 **Critical** (Immediate Response)
- Service down
- Data loss risk
- Security breaches
- High fraud rates
- **Response Time**: < 5 minutes
- **Notifications**: Email + Slack + PagerDuty

#### ⚠️ **Warning** (Monitor Closely)
- Performance degradation
- Resource constraints
- Model drift
- **Response Time**: < 2 hours
- **Notifications**: Email + Slack

#### ℹ️ **Info** (Awareness)
- Capacity planning
- Trend analysis
- Maintenance windows
- **Response Time**: < 24 hours
- **Notifications**: Slack

### Alert Routing

```yaml
Route: Critical Alerts
├── Fraud Detection → Security Team + Fraud Team
├── ETL Processing → Data Engineering Team
├── Infrastructure → Platform Team
└── ML Models → ML Engineering Team

Route: Warning Alerts
├── Performance → Monitoring Team
├── Resources → Platform Team
└── Data Quality → Data Team

Route: Info Alerts
└── All Teams → #monitoring Channel
```

## 🎯 Dashboard Categories

### 1. **Platform Overview**
- High-level system health
- Key performance indicators
- Service status matrix
- Alert summary

### 2. **ETL & Data Processing**
- Data ingestion rates
- Processing latency
- Error rates
- Queue depths

### 3. **Fraud Detection & ML**
- Model performance
- Prediction accuracy
- Feature store metrics
- Data drift indicators

### 4. **Kafka & Streaming**
- Topic throughput
- Consumer lag
- Broker performance
- Producer metrics

### 5. **Infrastructure**
- System resources
- Container performance
- Network metrics
- Storage utilization

### 6. **Business Metrics**
- Transaction volumes
- Revenue tracking
- Customer insights
- Operational KPIs

## 🔧 Configuration Management

### Environment Variables

```bash
# Security
GRAFANA_SECRET_KEY=your-secret-key
SLACK_WEBHOOK_URL=your-slack-webhook

# Redis (if password protected)
REDIS_PASSWORD=your-redis-password

# Email configuration
SMTP_HOST=your-smtp-host
SMTP_USER=your-smtp-user
SMTP_PASS=your-smtp-password
```

### Volume Mounts

All configurations and data are persisted:

- **Prometheus Data**: `./data/prometheus`
- **Grafana Data**: `./data/grafana`
- **AlertManager Data**: `./data/alertmanager`
- **Loki Data**: `./data/loki`

## 🛠️ Maintenance Operations

### Regular Tasks

#### Daily
```bash
# Check service health
docker-compose -f docker-compose.monitoring.yml ps

# Review alerts
curl -s http://localhost:9093/api/v1/alerts | jq
```

#### Weekly
```bash
# Clean up old data
docker exec bigdata-prometheus promtool query instant 'prometheus_tsdb_retention_limit_bytes'

# Review dashboard usage
curl -s http://localhost:3000/api/search | jq
```

#### Monthly
```bash
# Update service images
docker-compose -f docker-compose.monitoring.yml pull
docker-compose -f docker-compose.monitoring.yml up -d

# Review alert rules effectiveness
```

### Troubleshooting

#### Common Issues

**Prometheus not scraping targets:**
```bash
# Check target status
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | select(.health != "up")'

# Verify network connectivity
docker exec bigdata-prometheus nc -zv target-service port
```

**Grafana dashboards not loading:**
```bash
# Check datasource connectivity
docker exec bigdata-grafana grafana-cli admin data-source list

# Verify volume mounts
docker exec bigdata-grafana ls -la /var/lib/grafana/dashboards/
```

**AlertManager not sending notifications:**
```bash
# Check configuration
docker exec bigdata-alertmanager amtool config show

# Test webhook
curl -X POST http://localhost:9093/api/v1/alerts -d @test-alert.json
```

## 📚 Advanced Usage

### Custom Metrics

Add custom metrics to your applications:

```python
# Python example
from prometheus_client import Counter, Histogram, start_http_server

REQUEST_COUNT = Counter('requests_total', 'Total requests', ['method', 'endpoint'])
REQUEST_LATENCY = Histogram('request_duration_seconds', 'Request latency')

# Start metrics server
start_http_server(8000)
```

### Custom Dashboards

Create custom Grafana dashboards:

1. **Via UI**: Grafana → Create → Dashboard
2. **Via Code**: Export JSON and add to `grafana/dashboard-configs/`
3. **Via API**: Use Grafana API for programmatic creation

### Alert Rules

Add custom alert rules in `prometheus/rules/`:

```yaml
groups:
  - name: custom-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
```

## 🔗 Integration with Main Platform

The monitoring stack is designed to work seamlessly with the main platform:

```bash
# Start main platform
cd deployment/docker-compose
docker-compose -f docker-compose.refactored.yml up -d

# Start monitoring (separate terminal)
cd infrastructure/monitoring
docker-compose -f docker-compose.monitoring.yml up -d

# Connect networks if needed
docker network connect bigdata-network bigdata-prometheus
```

## 📞 Support and Maintenance

### Health Checks

All services include health checks for:
- Container orchestration
- Load balancer integration
- Monitoring service monitoring

### Backup Strategy

Critical data backup:
- Prometheus data (metrics)
- Grafana configurations (dashboards)
- AlertManager configurations

### Scaling Considerations

For production deployment:
- Use external storage for Prometheus
- Implement Grafana clustering
- Configure AlertManager clustering
- Use external log storage for Loki

---

For questions or issues, refer to the individual service documentation or create an issue in the project repository. 