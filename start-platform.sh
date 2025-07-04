#!/bin/bash

# 🚀 Big Data Learning Platform - Complete Environment Startup Script
# Professional enterprise-grade setup launcher

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_color() {
    printf "${1}${2}${NC}\n"
}

print_header() {
    echo ""
    print_color $CYAN "============================================"
    print_color $CYAN "$1"
    print_color $CYAN "============================================"
    echo ""
}

print_status() {
    print_color $GREEN "✅ $1"
}

print_warning() {
    print_color $YELLOW "⚠️  $1"
}

print_error() {
    print_color $RED "❌ $1"
}

print_info() {
    print_color $BLUE "ℹ️  $1"
}

# Check if Docker and Docker Compose are installed
check_dependencies() {
    print_header "CHECKING DEPENDENCIES"
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    print_status "Docker is installed"
    
    if ! command -v docker-compose &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            print_error "Docker Compose is not installed. Please install Docker Compose first."
            exit 1
        else
            COMPOSE_CMD="docker compose"
        fi
    else
        COMPOSE_CMD="docker-compose"
    fi
    print_status "Docker Compose is available"
}

# Function to clean up existing containers and volumes
cleanup() {
    print_header "CLEANING UP EXISTING ENVIRONMENT"
    
    print_info "Stopping and removing existing containers..."
    $COMPOSE_CMD down --remove-orphans 2>/dev/null || true
    
    print_info "Removing unused networks..."
    docker network prune -f 2>/dev/null || true
    
    print_status "Cleanup completed"
}

# Function to build images
build_images() {
    print_header "BUILDING DOCKER IMAGES"
    
    print_info "Building ML Services image..."
    $COMPOSE_CMD build ml-services
    
    print_info "Building ETL Engine image..."
    $COMPOSE_CMD build etl-engine
    
    print_info "Building Kafka Streams image..."
    $COMPOSE_CMD build kafka-streams
    
    print_status "All images built successfully"
}

# Function to start infrastructure services first
start_infrastructure() {
    print_header "STARTING INFRASTRUCTURE SERVICES"
    
    print_info "Starting Zookeeper..."
    $COMPOSE_CMD up -d zookeeper
    sleep 10
    
    print_info "Starting Kafka..."
    $COMPOSE_CMD up -d kafka
    sleep 15
    
    print_info "Starting Schema Registry..."
    $COMPOSE_CMD up -d schema-registry
    sleep 5
    
    print_info "Starting databases..."
    $COMPOSE_CMD up -d postgres mysql redis
    sleep 10
    
    print_status "Infrastructure services started"
}

# Function to start application services
start_applications() {
    print_header "STARTING APPLICATION SERVICES"
    
    print_info "Starting MLflow..."
    $COMPOSE_CMD up -d mlflow
    sleep 10
    
    print_info "Starting ML Services..."
    $COMPOSE_CMD up -d ml-services
    sleep 15
    
    print_info "Starting ETL Engine..."
    $COMPOSE_CMD up -d etl-engine
    sleep 10
    
    print_info "Starting Kafka Streams..."
    $COMPOSE_CMD up -d kafka-streams
    sleep 5
    
    print_status "Application services started"
}

# Function to start monitoring and management services
start_monitoring() {
    print_header "STARTING MONITORING & MANAGEMENT SERVICES"
    
    print_info "Starting Prometheus..."
    $COMPOSE_CMD up -d prometheus
    sleep 5
    
    print_info "Starting Grafana..."
    $COMPOSE_CMD up -d grafana
    sleep 5
    
    print_info "Starting AlertManager..."
    $COMPOSE_CMD up -d alertmanager
    sleep 5
    
    print_info "Starting management UIs..."
    $COMPOSE_CMD up -d kafka-ui redis-commander jupyter
    sleep 5
    
    print_status "Monitoring and management services started"
}

# Function to check service health
check_health() {
    print_header "CHECKING SERVICE HEALTH"
    
    # Wait a bit for services to fully start
    print_info "Waiting for services to initialize..."
    sleep 30
    
    # Check key services
    services=(
        "http://localhost:5000/health|ML Services"
        "http://localhost:5002/health|MLflow"
        "http://localhost:8080/actuator/health|ETL Engine"
        "http://localhost:9091|Kafka UI"
        "http://localhost:3000|Grafana"
        "http://localhost:9090|Prometheus"
        "http://localhost:8082|Redis Commander"
        "http://localhost:8888|Jupyter"
    )
    
    for service in "${services[@]}"; do
        IFS='|' read -r url name <<< "$service"
        if curl -f -s "$url" > /dev/null 2>&1; then
            print_status "$name is healthy"
        else
            print_warning "$name is not responding (may still be starting)"
        fi
    done
}

# Function to display service URLs
show_services() {
    print_header "🎉 BIG DATA PLATFORM IS READY!"
    
    echo ""
    print_color $PURPLE "🔗 ACCESS POINTS:"
    echo ""
    
    print_color $GREEN "📊 Analytics & ML:"
    echo "  • ML Services API:     http://localhost:5000"
    echo "  • MLflow Tracking:     http://localhost:5002"
    echo "  • Jupyter Notebooks:   http://localhost:8888 (token: bigdata123)"
    echo ""
    
    print_color $GREEN "🔧 Data Processing:"
    echo "  • ETL Engine:          http://localhost:8080"
    echo "  • ETL Management:      http://localhost:8084"
    echo ""
    
    print_color $GREEN "📈 Monitoring:"
    echo "  • Grafana Dashboard:   http://localhost:3000 (admin/admin123)"
    echo "  • Prometheus Metrics:  http://localhost:9090"
    echo "  • AlertManager:        http://localhost:9093"
    echo ""
    
    print_color $GREEN "🛠️  Management:"
    echo "  • Kafka UI:            http://localhost:9091"
    echo "  • Redis Commander:     http://localhost:8082"
    echo ""
    
    print_color $GREEN "🗄️  Databases:"
    echo "  • PostgreSQL:          localhost:5432 (bigdata_user/bigdata_pass)"
    echo "  • MySQL:               localhost:3306 (sales_user/sales_pass)"
    echo "  • Redis:               localhost:6379"
    echo ""
    
    print_color $GREEN "📡 Messaging:"
    echo "  • Kafka:               localhost:9092"
    echo "  • Schema Registry:     localhost:8081"
    echo ""
    
    print_color $YELLOW "💡 Quick Start Commands:"
    echo "  • View logs:           docker-compose logs -f [service-name]"
    echo "  • Stop platform:       docker-compose down"
    echo "  • Restart service:     docker-compose restart [service-name]"
    echo "  • View status:         docker-compose ps"
    echo ""
    
    print_color $CYAN "📚 Documentation:"
    echo "  • README.md:           Project overview"
    echo "  • REFACTORED_ARCHITECTURE.md: Architecture details"
    echo "  • PLATFORM_READY_GUIDE.md: Platform guide"
    echo ""
}

# Main execution
main() {
    print_header "🚀 BIG DATA LEARNING PLATFORM STARTUP"
    
    # Check if user wants to skip cleanup
    if [[ "$1" != "--no-cleanup" ]]; then
        cleanup
    fi
    
    # Check dependencies
    check_dependencies
    
    # Build images
    build_images
    
    # Start services in proper order
    start_infrastructure
    start_applications
    start_monitoring
    
    # Check health
    check_health
    
    # Show access information
    show_services
}

# Handle script arguments
case "$1" in
    --help|-h)
        echo "Big Data Platform Startup Script"
        echo ""
        echo "Usage: $0 [OPTIONS]"
        echo ""
        echo "Options:"
        echo "  --no-cleanup    Skip cleanup of existing containers"
        echo "  --help, -h      Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0              # Full startup with cleanup"
        echo "  $0 --no-cleanup # Start without cleanup"
        exit 0
        ;;
    *)
        main "$1"
        ;;
esac 