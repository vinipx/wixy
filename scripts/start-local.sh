#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "🚀 Starting Wixy locally..."
cd "$PROJECT_DIR"

# Build (skip tests for fast startup)
./gradlew bootJar -x test -x integrationTest --quiet

# Support starting a 3-node fleet locally (-3)
if [[ "${1:-}" == "-3" ]]; then
    echo "🏗️  Starting 3-node local fleet (Hub + 2 Remote Nodes)..."
    
    # 1. Start Hub (Port 8080/9090)
    echo "   ◆ Starting Hub [8080/9090]..."
    java -jar build/libs/wixy-*.jar --spring.profiles.active=local \
        --server.port=8080 --wixy.wiremock.port=9090 > build/hub.log 2>&1 &
    
    # 2. Start Remote 1 (Port 8081/9091)
    echo "   ◆ Starting Remote-1 [8081/9091]..."
    java -jar build/libs/wixy-*.jar --spring.profiles.active=local \
        --server.port=8081 --wixy.wiremock.port=9091 --wixy.ui.enabled=false \
        --wixy.proxy.enabled=true --wixy.proxy.target-url=https://jsonplaceholder.typicode.com > build/remote-engine-1.log 2>&1 &
    
    # 3. Start Remote 2 (Port 8082/9092)
    echo "   ◆ Starting Remote-2 [8082/9092]..."
    java -jar build/libs/wixy-*.jar --spring.profiles.active=local \
        --server.port=8082 --wixy.wiremock.port=9092 --wixy.ui.enabled=false \
        --wixy.proxy.enabled=true --wixy.proxy.target-url=https://jsonplaceholder.typicode.com > build/remote-engine-2.log 2>&1 &
    
    echo ""
    echo "✅ Fleet started. Logs available at build/*.log"
    echo "👉 Dashboard: http://localhost:8080"
    echo ""
    echo "Press Ctrl+C to stop all nodes..."
    
    # Keep script alive and trap Ctrl+C to kill background processes
    trap "kill 0" EXIT
    wait
else
    echo "✅ Wixy starting on http://localhost:8080 (admin) and http://localhost:9090 (wiremock)"
    echo "   Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "   Health:     http://localhost:8080/actuator/health"
    echo ""

    # Run with local profile; pass any extra args from the command line
    exec java -jar build/libs/wixy-*.jar --spring.profiles.active=local "$@"
fi

