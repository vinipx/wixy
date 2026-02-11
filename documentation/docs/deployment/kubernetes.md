---
sidebar_position: 2
title: Kubernetes
---

# Kubernetes Deployment

Deploy WIXY to Kubernetes for shared team environments and CI/CD pipelines.

## Deployment Manifest

```yaml title="k8s/deployment.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wixy
  labels:
    app: wixy
spec:
  replicas: 1
  selector:
    matchLabels:
      app: wixy
  template:
    metadata:
      labels:
        app: wixy
    spec:
      containers:
        - name: wixy
          image: wixy:latest
          ports:
            - name: admin
              containerPort: 8080
            - name: wiremock
              containerPort: 9090
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: cloud
            - name: WIXY_SECURITY_ENABLED
              value: "true"
            - name: WIXY_SECURITY_API_KEY
              valueFrom:
                secretKeyRef:
                  name: wixy-secrets
                  key: api-key
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: admin
            initialDelaySeconds: 20
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: admin
            initialDelaySeconds: 10
            periodSeconds: 5
          resources:
            requests:
              cpu: 250m
              memory: 256Mi
            limits:
              cpu: "1"
              memory: 512Mi
```

## Service

```yaml title="k8s/service.yaml"
apiVersion: v1
kind: Service
metadata:
  name: wixy
spec:
  selector:
    app: wixy
  ports:
    - name: admin
      port: 8080
      targetPort: admin
    - name: wiremock
      port: 9090
      targetPort: wiremock
  type: ClusterIP
```

## Secret

```yaml title="k8s/secret.yaml"
apiVersion: v1
kind: Secret
metadata:
  name: wixy-secrets
type: Opaque
stringData:
  api-key: "your-secret-api-key-here"
```

## Deploy

```bash
# Create the secret
kubectl apply -f k8s/secret.yaml

# Deploy the application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Verify
kubectl get pods -l app=wixy
kubectl logs -l app=wixy -f
```

## Accessing WIXY

### From Within the Cluster

```bash
# Admin API
curl http://wixy:8080/wixy/admin/mappings \
  -H "X-Wixy-Api-Key: your-secret-api-key-here"

# WireMock stubs
curl http://wixy:9090/api/your-stub
```

### From Outside (Port Forward)

```bash
kubectl port-forward svc/wixy 8080:8080 9090:9090

# Then access locally
curl http://localhost:8080/actuator/health
```

## Ingress (Optional)

```yaml title="k8s/ingress.yaml"
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: wixy
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
    - host: wixy.internal.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: wixy
                port:
                  number: 8080
```

## Scaling Considerations

:::warning
WIXY stores stubs **in-memory** within the WireMock server. Running multiple replicas means each replica has its own independent set of stubs. For shared state, use a single replica or implement an external stub store.
:::

| Replicas | Use Case |
|----------|----------|
| 1 | Shared team mock server, consistent stubs |
| N | Load testing, each replica serves the same file-based stubs |
