---
sidebar_position: 3
title: CI/CD
---

# CI/CD Integration

WIXY integrates seamlessly into CI/CD pipelines via Docker and GitHub Actions.

## GitHub Actions — Build & Test

```yaml title=".github/workflows/ci.yml"
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission
        run: chmod +x ./gradlew

      - name: Build & Test
        run: ./gradlew check

      - name: Upload coverage report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: build/reports/jacoco/
```

## Using WIXY as a CI Service

### GitHub Actions with Docker

```yaml title=".github/workflows/integration-test.yml"
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      wixy:
        image: ghcr.io/vinipx/wixy:latest
        ports:
          - 8080:8080
          - 9090:9090
        env:
          SPRING_PROFILES_ACTIVE: docker
        options: >-
          --health-cmd "curl -f http://localhost:8080/actuator/health"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
          --health-start-period 20s

    steps:
      - uses: actions/checkout@v4

      - name: Wait for WIXY
        run: |
          for i in {1..30}; do
            curl -sf http://localhost:8080/actuator/health && break
            echo "Waiting for WIXY... ($i)"
            sleep 2
          done

      - name: Load stubs
        run: |
          for stub in test/stubs/*.json; do
            echo "Loading stub: $stub"
            curl -X POST http://localhost:8080/wixy/admin/mappings \
              -H "Content-Type: application/json" \
              -d @"$stub"
          done

      - name: Run integration tests
        run: ./gradlew integrationTest
```

## Pipeline Patterns

### Pattern 1: WIXY as Sidecar

```yaml
# Start WIXY alongside your app
services:
  wixy:
    image: wixy:latest
    ports: ["9090:9090"]

steps:
  - name: Start app pointing to WIXY
    env:
      EXTERNAL_API_URL: http://localhost:9090
    run: ./start-app.sh &

  - name: Run tests
    run: ./run-tests.sh
```

### Pattern 2: Record in Staging, Replay in CI

```bash
# Staging pipeline (runs weekly)
curl -X POST http://wixy:8080/wixy/admin/recordings/start \
  -d '{"targetUrl": "https://staging.example.com"}'
# ... exercise flows ...
curl -X POST http://wixy:8080/wixy/admin/recordings/stop
curl http://wixy:8080/wixy/admin/mappings > recorded-stubs.json
# Commit recorded-stubs.json to repo

# CI pipeline (runs on every PR)
# Load the recorded stubs
curl -X POST http://wixy:8080/wixy/admin/mappings/import \
  -d @recorded-stubs.json
# Run tests — no external connectivity needed
```

### Pattern 3: Dynamic Stubs per Test

```bash
# Each test creates its own stubs
curl -X POST http://localhost:8080/wixy/admin/mappings/reset
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -d @test-specific-stub.json
./run-specific-test.sh
```
