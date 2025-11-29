#!/bin/bash

# ===========================================
# PetShop Microservices - Stop All Services
# ===========================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}"
echo "╔══════════════════════════════════════════════╗"
echo "║  🐾 PetShop Microservices - Stop Services    ║"
echo "╚══════════════════════════════════════════════╝"
echo -e "${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="$SCRIPT_DIR/pids/$service_name.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo -e "${YELLOW}Stopping $service_name (PID: $pid)...${NC}"
            kill $pid 2>/dev/null
            sleep 1
            
            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                kill -9 $pid 2>/dev/null
            fi
            
            echo -e "${GREEN}✓ $service_name stopped${NC}"
        else
            echo -e "${YELLOW}$service_name is not running${NC}"
        fi
        rm -f "$pid_file"
    else
        echo -e "${YELLOW}No PID file found for $service_name${NC}"
    fi
}

# Stop all services
stop_service "auth"
stop_service "customers"
stop_service "pets"
stop_service "catalog"
stop_service "scheduling"
stop_service "orders"

# Also kill any remaining func processes on our ports
echo ""
echo -e "${BLUE}Cleaning up any remaining processes...${NC}"
for port in 7071 7072 7073 7074 7075 7076; do
    pid=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}Killing process on port $port (PID: $pid)${NC}"
        kill -9 $pid 2>/dev/null
    fi
done

echo ""
echo -e "${GREEN}All services stopped!${NC}"
