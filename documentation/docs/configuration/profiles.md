---
sidebar_position: 1
title: Profiles
---

# Configuration Profiles

WIXY uses Spring Boot profiles to provide environment-specific configuration. Each profile inherits defaults from `application.yml` and overrides specific properties.

## Available Profiles

| Profile | Usage | Key Differences |
|---------|-------|-----------------|
| `default` | Base configuration | All defaults |
| `local` | Local development | Verbose logging, security OFF |
| `docker` | Docker containers | Env-var driven, verbose OFF |
| `cloud` | Cloud deployment | Security ON by default, JSON logging |

## Default Configuration

```yaml title="application.yml"
server:
  port: 8080

wixy:
  wiremock:
    port: 9090
    verbose: true
    root-dir: classpath:/wiremock
  proxy:
    enabled: false
    target-url: ""
    record: false
  security:
    enabled: false
    api-key: ""

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs

logging:
  level:
    io.github.vinipx.wixy: INFO
    org.wiremock: INFO
```

## Local Profile

Optimised for local development with maximum logging visibility:

```yaml title="application-local.yml"
wixy:
  wiremock:
    port: 9090
    verbose: true
  proxy:
    enabled: false
  security:
    enabled: false

logging:
  level:
    io.github.vinipx.wixy: DEBUG
    org.wiremock: DEBUG
```

**Activate:**

```bash
java -jar wixy.jar --spring.profiles.active=local
# or
./scripts/start-local.sh
```

## Docker Profile

Environment-variable driven for container flexibility:

```yaml title="application-docker.yml"
wixy:
  wiremock:
    port: 9090
    verbose: false
  proxy:
    enabled: ${WIXY_PROXY_ENABLED:false}
    target-url: ${WIXY_PROXY_TARGET_URL:}
    record: ${WIXY_PROXY_RECORD:false}
  security:
    enabled: ${WIXY_SECURITY_ENABLED:false}
    api-key: ${WIXY_SECURITY_API_KEY:}

logging:
  level:
    io.github.vinipx.wixy: INFO
```

**Activate:**

```bash
docker run -e SPRING_PROFILES_ACTIVE=docker \
  -e WIXY_PROXY_ENABLED=true \
  -e WIXY_PROXY_TARGET_URL=https://api.example.com \
  wixy:latest
```

## Cloud Profile

Production-grade with security enabled and structured JSON logging:

```yaml title="application-cloud.yml"
wixy:
  wiremock:
    port: 9090
    verbose: false
  proxy:
    enabled: ${WIXY_PROXY_ENABLED:false}
    target-url: ${WIXY_PROXY_TARGET_URL:}
    record: ${WIXY_PROXY_RECORD:false}
  security:
    enabled: ${WIXY_SECURITY_ENABLED:true}
    api-key: ${WIXY_SECURITY_API_KEY:}

logging:
  level:
    io.github.vinipx.wixy: INFO
  pattern:
    console: >-
      {"timestamp":"%d{ISO8601}","level":"%p","logger":"%logger","message":"%m"}%n
```

**Activate:**

```bash
java -jar wixy.jar --spring.profiles.active=cloud
```

## Profile Selection Priority

Spring Boot resolves properties in this order (last wins):

1. `application.yml` (defaults)
2. `application-{profile}.yml` (profile overrides)
3. Environment variables (highest priority)
4. Command-line arguments (absolute highest)

:::tip
Environment variables always override YAML files. This makes WIXY fully 12-factor compliant — configure entirely from the environment without touching files.
:::
