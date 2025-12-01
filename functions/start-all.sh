#!/bin/bash

# ===========================================
# PetShop Microservices - Start All Services
# ===========================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════╗"
echo "║  🐾 PetShop Microservices - Azure Functions  ║"
echo "╚══════════════════════════════════════════════╝"
echo -e "${NC}"

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Function to check if a port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0 # Port is in use
    else
        return 1 # Port is free
    fi
}

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    echo -e "${YELLOW}Starting $service_name on port $port...${NC}"
    
    if check_port $port; then
        echo -e "${RED}Port $port is already in use. Skipping $service_name.${NC}"
        return 1
    fi
    
    cd "$SCRIPT_DIR/$service_dir"
    
    # Start the function in background
    func start --port $port > "$SCRIPT_DIR/logs/$service_name.log" 2>&1 &
    local pid=$!
    echo $pid > "$SCRIPT_DIR/pids/$service_name.pid"
    
    # Wait a bit and check if process is still running
    sleep 2
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "${GREEN}✓ $service_name started (PID: $pid)${NC}"
        return 0
    else
        echo -e "${RED}✗ $service_name failed to start${NC}"
        return 1
    fi
}

# Create directories for logs and PIDs
mkdir -p "$SCRIPT_DIR/logs"
mkdir -p "$SCRIPT_DIR/pids"

# Check prerequisites
echo -e "${BLUE}Checking prerequisites...${NC}"

if ! command -v func &> /dev/null; then
    echo -e "${RED}Azure Functions Core Tools not found!${NC}"
    echo "Install with: npm install -g azure-functions-core-tools@4"
    exit 1
fi

if ! command -v dotnet &> /dev/null; then
    echo -e "${RED}.NET SDK not found!${NC}"
    exit 1
fi

echo -e "${GREEN}Prerequisites OK${NC}"
echo ""

# Build the solution first
echo -e "${BLUE}Building solution...${NC}"
cd "$SCRIPT_DIR"
dotnet build Petshop.Functions.sln --configuration Debug
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
fi
echo -e "${GREEN}Build successful${NC}"
echo ""

# Start all services
echo -e "${BLUE}Starting services...${NC}"
echo ""

start_service "auth"       "func-petshop-auth"       7071
start_service "customers"  "func-petshop-customers"  7072
start_service "pets"       "func-petshop-pets"       7073
start_service "catalog"    "func-petshop-catalog"    7074
start_service "scheduling" "func-petshop-scheduling" 7075
start_service "orders"     "func-petshop-orders"     7076

echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  All services started!                       ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}Service URLs:${NC}"
echo "  Auth:       http://localhost:7071"
echo "  Customers:  http://localhost:7072"
echo "  Pets:       http://localhost:7073"
echo "  Catalog:    http://localhost:7074"
echo "  Scheduling: http://localhost:7075"
echo "  Orders:     http://localhost:7076"
echo ""
echo -e "${YELLOW}Logs are available in: $SCRIPT_DIR/logs/${NC}"
echo -e "${YELLOW}To stop all services, run: ./stop-all.sh${NC}"
