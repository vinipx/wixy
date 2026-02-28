#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

TOTAL_ENGINES=1
ARGS=()

# Parse arguments to look for -N (e.g. -3)
for arg in "$@"; do
  if [[ "$arg" =~ ^-([0-9]+)$ ]]; then
    TOTAL_ENGINES="${BASH_REMATCH[1]}"
  else
    ARGS+=("$arg")
  fi
done

echo "🚀 Starting Wixy locally..."
cd "$PROJECT_DIR"

# Build (skip tests for fast startup)
./gradlew bootJar -x test -x integrationTest --quiet

echo "✅ Wixy Hub starting on http://localhost:8080 (admin) and http://localhost:9090 (embedded wiremock)"
echo "   Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   Health:     http://localhost:8080/actuator/health"

PIDS=()

# Graceful shutdown handler
cleanup() {
    echo ""
    echo "🛑 Stopping all Wixy engines..."
    for pid in "${PIDS[@]}"; do
        kill -TERM "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null || true
    exit 0
}

trap cleanup SIGINT SIGTERM

if [ "$TOTAL_ENGINES" -gt 1 ]; then
    echo "🌐 Starting $((TOTAL_ENGINES - 1)) simulated remote engines..."
    for i in $(seq 1 $((TOTAL_ENGINES - 1))); do
        ADMIN_PORT=$((8080 + i))
        WM_PORT=$((9090 + i))
        echo "   🚀 Remote Engine $i starting on admin port $ADMIN_PORT, wiremock port $WM_PORT"
        
        # Start remote engines with catch-all proxy enabled by default for demo purposes
        java -jar build/libs/wixy-*.jar \
            --spring.profiles.active=local \
            --server.port=$ADMIN_PORT \
            --wixy.wiremock.port=$WM_PORT \
            --wixy.ui.enabled=false \
            --wixy.proxy.enabled=true \
            --wixy.proxy.target-url=https://jsonplaceholder.typicode.com \
            > "build/remote-engine-$i.log" 2>&1 &
        PIDS+=($!)
    done
    echo "   (Remote engine logs available in build/remote-engine-*.log)"

    # Auto-register engines in background
    (
        echo "⏳ Waiting for Hub to start before registering remote engines..."
        # Wait up to ~60 seconds for the hub to become ready
        for attempt in {1..30}; do
            if curl -s http://localhost:8080/actuator/health > /dev/null; then
                echo "✅ Hub is ready. Registering remote engines..."
                for i in $(seq 1 $((TOTAL_ENGINES - 1))); do
                    ADMIN_PORT=$((8080 + i))
                    curl -s -X POST http://localhost:8080/wixy/admin/registry/servers \
                        -H "Content-Type: application/json" \
                        -d "{\"name\":\"Remote-$i\",\"url\":\"http://localhost:$ADMIN_PORT\"}" > /dev/null
                    echo "   📡 Auto-registered Remote-$i (Admin Port: $ADMIN_PORT)"
                done
                break
            fi
            sleep 2
        done
    ) &
    PIDS+=($!)
fi

echo ""
# Run the main Hub in the background to allow trap to handle signals gracefully
java -jar build/libs/wixy-*.jar --spring.profiles.active=local "${ARGS[@]}" &
PIDS+=($!)

wait
