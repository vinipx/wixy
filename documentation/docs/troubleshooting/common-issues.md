---
sidebar_position: 1
title: Common Issues
---

# Troubleshooting

Common issues and their solutions when using WIXY.

## Startup Issues

### Port Already in Use

**Symptom:** `Address already in use: bind` error on startup

**Solution:**

```bash
# Find what's using the port
lsof -i :8080
lsof -i :9090

# Kill the process
kill -9 <PID>

# Or change WIXY's ports
export SERVER_PORT=3000
export WIXY_WIREMOCK_PORT=3001
java -jar wixy.jar
```

### Java Version Mismatch

**Symptom:** `UnsupportedClassVersionError` or compilation errors

**Solution:**

```bash
# Check your Java version
java -version

# WIXY requires Java 21+
# Install Java 21
brew install openjdk@21  # macOS
```

### Gradle Build Failure

**Symptom:** Build fails with dependency resolution errors

**Solution:**

```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# If wrapper is outdated
./gradlew wrapper --gradle-version=9.3.1
```

## Runtime Issues

### Stub Not Matching

**Symptom:** Requests to `localhost:9090` return 404 when you expect a match.

**Checklist:**
1. ✅ Are you hitting port `9090` (WireMock), not `8080` (Admin)?
2. ✅ Is the HTTP method correct (GET vs POST)?
3. ✅ Is the URL path exact? (`/api/users` ≠ `/api/users/`)
4. ✅ Are required headers present in the request?

```bash
# List all active stubs to verify
curl http://localhost:8080/wixy/admin/mappings

# Check WireMock's near-miss log
curl http://localhost:9090/__admin/requests/unmatched
```

### Proxy Not Forwarding

**Symptom:** Requests return 404 instead of being forwarded to upstream.

**Solution:**

```bash
# Verify proxy is enabled
curl http://localhost:8080/wixy/admin/proxy

# If not enabled, enable it
curl -X POST http://localhost:8080/wixy/admin/proxy/enable \
  -d '{"targetUrl": "https://api.example.com"}'
```

### Recording Not Capturing Stubs

**Symptom:** `capturedStubs: 0` after stopping recording.

**Checklist:**
1. ✅ Was recording actually started? Check status: `GET /wixy/admin/recordings/status`
2. ✅ Did traffic flow through WIXY? Send requests to port `9090`
3. ✅ Is the target URL reachable from WIXY's host?
4. ✅ `ignoreRepeatRequests` means duplicate patterns are skipped

### Health Check Shows DOWN

**Symptom:** `/actuator/health` returns `{"status":"DOWN","components":{"wiremock":{"status":"DOWN"}}}`

**Solution:**

```bash
# Check if WireMock port is actually listening
curl http://localhost:9090/__admin/mappings

# Check application logs for startup errors
docker logs wixy
```

## Docker Issues

### Container Exits Immediately

**Symptom:** Container starts and immediately stops.

```bash
# Check logs
docker logs wixy

# Common cause: port conflict — use different external ports
docker run -p 8081:8080 -p 9091:9090 wixy:latest
```

### Cannot Connect from Host

**Symptom:** `Connection refused` when accessing WIXY from host.

```bash
# Verify ports are published
docker port wixy

# Ensure container is running
docker ps

# Check container health
docker inspect --format='{{.State.Health.Status}}' wixy
```

### Build Fails in Docker

**Symptom:** `./gradlew: Permission denied` during Docker build.

The Dockerfile includes `chmod +x ./gradlew`. If you're building manually:

```bash
chmod +x gradlew
docker build -t wixy:latest .
```

## Security Issues

### 401 Unauthorized

**Symptom:** All requests return 401.

```bash
# Check if security is enabled
# Look for "🔐 API-key security filter enabled" in logs

# Include the API key header
curl -H "X-Wixy-Api-Key: your-key" http://localhost:8080/wixy/admin/mappings

# Health endpoint should still work without auth
curl http://localhost:8080/actuator/health
```

## Performance Issues

### High Memory Usage

- Check total stub count: `GET /wixy/admin/mappings` → `meta.total`
- Large response bodies consume memory — use `bodyFileName` for large payloads
- Reset unused stubs: `POST /wixy/admin/mappings/reset`

### Slow Responses

- Check for `fixedDelayMilliseconds` in your stubs
- Verify no network issues between WIXY and proxy targets
- Check JVM garbage collection with `jstat`

## Getting Help

If you can't resolve an issue:

1. Check the [GitHub Issues](https://github.com/vinipx/wixy/issues)
2. Include: WIXY version, Java version, OS, and full error logs
3. Provide the minimal reproduction steps
