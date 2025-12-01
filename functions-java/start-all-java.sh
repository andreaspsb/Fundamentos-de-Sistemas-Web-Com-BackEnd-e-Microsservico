#!/bin/bash

# Start all Java Azure Functions for Petshop
# This script starts the H2 database server and all function apps

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "==================================================="
echo "  Starting Petshop Java Azure Functions"
echo "==================================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Check if Azure Functions Core Tools is installed
if ! command -v func &> /dev/null; then
    echo "Error: Azure Functions Core Tools is not installed"
    echo "Install with: npm install -g azure-functions-core-tools@4"
    exit 1
fi

# Build the project first
echo ""
echo "Building all modules..."
mvn clean package -DskipTests

# Start H2 TCP Server in background
echo ""
echo "Starting H2 Database Server..."
java -cp ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar org.h2.tools.Server -tcp -tcpAllowOthers -ifNotExists &
H2_PID=$!
echo "H2 Server started with PID: $H2_PID"
sleep 2

# Create pid file directory
mkdir -p .pids

# Save H2 PID
echo $H2_PID > .pids/h2.pid

# Function to start an Azure Function
start_function() {
    local name=$1
    local port=$2
    
    echo ""
    echo "Starting $name on port $port..."
    
    cd "$SCRIPT_DIR/$name/target/azure-functions/$name"
    func start --port $port &
    local PID=$!
    echo $PID > "$SCRIPT_DIR/.pids/$name.pid"
    cd "$SCRIPT_DIR"
    
    echo "$name started with PID: $PID"
    sleep 3
}

# Start all functions
start_function "func-petshop-auth-java" 7081
start_function "func-petshop-customers-java" 7082
start_function "func-petshop-pets-java" 7083
start_function "func-petshop-catalog-java" 7084
start_function "func-petshop-scheduling-java" 7085
start_function "func-petshop-orders-java" 7086

echo ""
echo "==================================================="
echo "  All Functions Started Successfully!"
echo "==================================================="
echo ""
echo "Endpoints:"
echo "  Auth:       http://localhost:7081/api/auth"
echo "  Customers:  http://localhost:7082/api/clientes"
echo "  Pets:       http://localhost:7083/api/pets"
echo "  Catalog:    http://localhost:7084/api/produtos"
echo "              http://localhost:7084/api/categorias"
echo "              http://localhost:7084/api/servicos"
echo "  Scheduling: http://localhost:7085/api/agendamentos"
echo "  Orders:     http://localhost:7086/api/pedidos"
echo ""
echo "H2 Console:   http://localhost:8082 (JDBC URL: jdbc:h2:tcp://localhost/~/petshop-functions)"
echo ""
echo "Default Admin: admin / admin123"
echo ""
echo "To stop all functions, run: ./stop-all-java.sh"
echo ""

# Keep script running to show logs
wait
