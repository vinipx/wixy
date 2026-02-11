#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "🚀 Starting Wixy locally..."
cd "$PROJECT_DIR"

# Build (skip tests for fast startup)
./gradlew bootJar -x test -x integrationTest --quiet

echo "✅ Wixy starting on http://localhost:8080 (admin) and http://localhost:9090 (wiremock)"
echo "   Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   Health:     http://localhost:8080/actuator/health"
echo ""

# Run with local profile; pass any extra args from the command line
exec java -jar build/libs/wixy-*.jar --spring.profiles.active=local "$@"
