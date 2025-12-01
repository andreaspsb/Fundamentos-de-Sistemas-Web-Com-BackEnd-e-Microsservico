#!/bin/bash

# Stop all Java Azure Functions for Petshop

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "==================================================="
echo "  Stopping Petshop Java Azure Functions"
echo "==================================================="

# Function to stop a process by PID file
stop_process() {
    local name=$1
    local pidfile="$SCRIPT_DIR/.pids/$name.pid"
    
    if [ -f "$pidfile" ]; then
        local pid=$(cat "$pidfile")
        if kill -0 "$pid" 2>/dev/null; then
            echo "Stopping $name (PID: $pid)..."
            kill "$pid" 2>/dev/null || true
            sleep 1
            # Force kill if still running
            if kill -0 "$pid" 2>/dev/null; then
                kill -9 "$pid" 2>/dev/null || true
            fi
        fi
        rm -f "$pidfile"
    fi
}

# Stop all functions
stop_process "func-petshop-orders-java"
stop_process "func-petshop-scheduling-java"
stop_process "func-petshop-catalog-java"
stop_process "func-petshop-pets-java"
stop_process "func-petshop-customers-java"
stop_process "func-petshop-auth-java"

# Stop H2 server
stop_process "h2"

# Also try to kill any remaining func processes on our ports
for port in 7081 7082 7083 7084 7085 7086; do
    pid=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$pid" ]; then
        echo "Killing process on port $port (PID: $pid)..."
        kill "$pid" 2>/dev/null || true
    fi
done

# Kill any remaining H2 server processes
pkill -f "org.h2.tools.Server" 2>/dev/null || true

echo ""
echo "All functions stopped."
echo ""
