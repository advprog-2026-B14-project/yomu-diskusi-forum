# 🚀 Deployment Guide - Yomu Diskusi Forum

Production deployment guide untuk berbagai platform (EC2, Docker, Kubernetes, etc).

---

## 📋 Pre-Deployment Checklist

- [ ] All tests pass locally: `./gradlew test`
- [ ] JAR builds successfully: `./gradlew bootJar`
- [ ] Environment variables defined
- [ ] Database schema created
- [ ] CORS origins configured
- [ ] SSL certificate (if using HTTPS)
- [ ] Backup database before deployment
- [ ] Monitoring/logging configured

---

## 1️⃣ EC2 Deployment (AWS)

### Architecture

```
┌──────────────────────────────────────┐
│        AWS EC2 Instance              │
│  ┌────────────────────────────────┐  │
│  │   Docker Container             │  │
│  │  ┌──────────────────────────┐ │  │
│  │  │ Java 21 + Spring Boot    │ │  │
│  │  │ Yomu Forum Service       │ │  │
│  │  └──────────────────────────┘ │  │
│  │         Port 8085              │  │
│  └────────────────────────────────┘  │
│              ↓                         │
│        Port 80/443 (Nginx)            │
└──────────────────────────────────────┘
         ↓
┌──────────────────────────────────────┐
│     Supabase PostgreSQL              │
│   (AWS RDS Hosted)                   │
└──────────────────────────────────────┘
```

### Step 1: EC2 Setup

#### Launch Instance

```bash
# AWS Console:
# 1. EC2 → Instances → Launch Instance
# 2. OS: Ubuntu 20.04 LTS (Free Tier eligible)
# 3. Instance Type: t2.micro or t2.small
# 4. Security Group: Allow SSH (22), HTTP (80), HTTPS (443)
# 5. Storage: 20GB minimum
# 6. Create/Select key pair
```

#### SSH into Instance

```bash
chmod 600 your-key.pem
ssh -i your-key.pem ubuntu@<ec2-public-ip>
```

#### Install Dependencies

```bash
sudo apt update
sudo apt install -y \
  openjdk-21-jdk \
  docker.io \
  git \
  nginx \
  curl

# Verify installations
java -version
docker --version
nginx -v
```

#### Add User to Docker Group (optional)

```bash
sudo usermod -aG docker ubuntu
newgrp docker
```

### Step 2: Clone & Build

```bash
# Clone repository
git clone https://github.com/<org>/yomu-diskusi-forum.git
cd yomu-diskusi-forum

# Build JAR
./gradlew clean bootJar -x test

# Verify JAR created
ls -lh build/libs/yomu-forum-*.jar
```

### Step 3: Docker Build

```bash
# Build Docker image
docker build -t yomu-diskusi-forum:latest .

# Verify image
docker images | grep yomu-diskusi-forum

# Tag for registry (optional)
docker tag yomu-diskusi-forum:latest <docker-registry>/yomu-diskusi-forum:1.0.0
```

### Step 4: Configure Environment

Create `production.env`:

```bash
cat > production.env << 'EOF'
# Database (Supabase)
DB_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<your-production-password>

# CORS
APP_CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Server
SERVER_PORT=8085

# Java
JAVA_OPTS=-Xmx512m -Xms256m
EOF

chmod 600 production.env
```

### Step 5: Run Container

```bash
# Run container
docker run -d \
  --name yomu-forum \
  --restart always \
  -p 8085:8085 \
  --env-file production.env \
  -v /home/ubuntu/logs:/logs \
  yomu-diskusi-forum:latest

# Verify running
docker ps | grep yomu-forum

# Check logs
docker logs -f yomu-forum
```

### Step 6: Setup Nginx (Reverse Proxy)

Create `/etc/nginx/sites-available/yomu-forum`:

```nginx
upstream yomu_forum_backend {
    server localhost:8085;
}

server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://yomu_forum_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check endpoint (no proxy)
    location /health {
        access_log off;
        return 200 "OK\n";
        add_header Content-Type text/plain;
    }
}
```

Enable nginx:

```bash
sudo ln -s /etc/nginx/sites-available/yomu-forum /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### Step 7: Setup HTTPS (Optional but Recommended)

```bash
# Install certbot
sudo apt install -y certbot python3-certbot-nginx

# Get SSL certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Certbot auto-renews, verify:
sudo systemctl status certbot.timer
```

### Step 8: Monitoring & Logs

```bash
# View app logs
docker logs -f yomu-forum --tail 100

# Setup log rotation
docker run --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  ...

# Health check
curl http://localhost:8085/api/comments
curl https://yourdomain.com/api/comments  # via Nginx
```

### Step 9: Auto-Restart on Boot

```bash
# Container already has --restart always
# But verify with:
docker inspect yomu-forum | grep RestartPolicy

# If running in background, use systemd:
cat > ~/.config/systemd/user/yomu-forum.service << EOF
[Unit]
Description=Yomu Forum Docker Container
After=docker.service
Requires=docker.service

[Service]
Type=simple
ExecStart=/usr/bin/docker run -p 8085:8085 --env-file /home/ubuntu/production.env yomu-diskusi-forum:latest
Restart=always

[Install]
WantedBy=default.target
EOF

sudo systemctl enable --user yomu-forum.service
```

---

## 2️⃣ Docker Compose Deployment

### Single-Server Setup with Database

Create `docker-compose.prod.yml`:

```yaml
version: "3.8"

services:
  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: yomu_postgres
    environment:
      POSTGRES_DB: yomu_forum
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: always

  # Yomu Forum Application
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: yomu_forum
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/yomu_forum
      DB_USERNAME: postgres
      DB_PASSWORD: ${DB_PASSWORD}
      APP_CORS_ALLOWED_ORIGINS: ${CORS_ORIGINS}
      JAVA_OPTS: -Xmx1g -Xms512m
    ports:
      - "8085:8085"
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - ./logs:/logs
    restart: always
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8085/api/comments"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Nginx Reverse Proxy
  nginx:
    image: nginx:alpine
    container_name: yomu_nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - app
    restart: always

volumes:
  postgres_data:
    driver: local
```

Deploy:

```bash
# Create .env file
cat > .env << EOF
DB_PASSWORD=secure_password_here
CORS_ORIGINS=https://yourdomain.com
EOF

# Create init.sql (schema)
cat > init.sql << EOF
CREATE TABLE comments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    reading_id UUID NOT NULL,
    parent_comment_id UUID,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reactions (
    id UUID PRIMARY KEY,
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reactions_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
    UNIQUE(comment_id, user_id)
);

CREATE INDEX idx_comments_reading_id ON comments(reading_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_reactions_comment_id ON reactions(comment_id);
EOF

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Verify
docker-compose -f docker-compose.prod.yml ps
```

---

## 3️⃣ Kubernetes Deployment (Advanced)

### Setup (GKE, EKS, AKS)

Create deployment manifests:

`k8s-namespace.yaml`:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: yomu-forum
```

`k8s-configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: yomu-forum
data:
  APP_CORS_ALLOWED_ORIGINS: "https://yourdomain.com"
  JAVA_OPTS: "-Xmx512m -Xms256m"
```

`k8s-secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
  namespace: yomu-forum
type: Opaque
stringData:
  url: "jdbc:postgresql://postgres-service:5432/yomu_forum?sslmode=require"
  username: "postgres"
  password: "secure_password_here"
```

`k8s-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: yomu-forum
  namespace: yomu-forum
  labels:
    app: yomu-forum
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: yomu-forum
  template:
    metadata:
      labels:
        app: yomu-forum
    spec:
      containers:
        - name: yomu-forum
          image: <docker-registry>/yomu-diskusi-forum:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8085
              name: http
          env:
            - name: DB_URL
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: url
            - name: DB_USERNAME
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: username
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: password
            - name: APP_CORS_ALLOWED_ORIGINS
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: APP_CORS_ALLOWED_ORIGINS
            - name: JAVA_OPTS
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: JAVA_OPTS
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /api/comments
              port: 8085
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /api/comments
              port: 8085
            initialDelaySeconds: 10
            periodSeconds: 5
            failureThreshold: 3
          volumeMounts:
            - name: logs
              mountPath: /logs
      volumes:
        - name: logs
          emptyDir: {}
```

`k8s-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: yomu-forum-service
  namespace: yomu-forum
spec:
  selector:
    app: yomu-forum
  ports:
    - protocol: TCP
      port: 8085
      targetPort: 8085
  type: LoadBalancer
```

`k8s-ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: yomu-forum-ingress
  namespace: yomu-forum
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - yourdomain.com
      secretName: tls-secret
  rules:
    - host: yourdomain.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: yomu-forum-service
                port:
                  number: 8085
```

Deploy to Kubernetes:

```bash
# Create namespace & secrets
kubectl apply -f k8s-namespace.yaml
kubectl apply -f k8s-secret.yaml
kubectl apply -f k8s-configmap.yaml

# Deploy application
kubectl apply -f k8s-deployment.yaml
kubectl apply -f k8s-service.yaml
kubectl apply -f k8s-ingress.yaml

# Verify
kubectl get pods -n yomu-forum
kubectl get services -n yomu-forum
kubectl get ingress -n yomu-forum

# View logs
kubectl logs -n yomu-forum -l app=yomu-forum -f
```

---

## 4️⃣ GitHub Actions CI/CD

The workflow is already configured in `.github/workflows/cd.yml`:

```yaml
name: CI/CD

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: "21"

      - name: Build with Gradle
        run: ./gradlew clean bootJar -x test

      - name: Build Docker image
        run: docker build -t yomu-diskusi-forum:${{ github.sha }} .

      - name: Push to registry
        run: |
          docker login -u ${{ secrets.DOCKER_USERNAME }} -p ${{ secrets.DOCKER_PASSWORD }}
          docker push yomu-diskusi-forum:${{ github.sha }}

      - name: Deploy to EC2
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.EC2_KEY }}" > ~/.ssh/key.pem
          chmod 600 ~/.ssh/key.pem
          ssh -i ~/.ssh/key.pem ubuntu@${{ secrets.EC2_IP }} << 'EOF'
            cd yomu-diskusi-forum
            docker pull yomu-diskusi-forum:${{ github.sha }}
            docker stop yomu-forum || true
            docker run -d --name yomu-forum --restart always ...
          EOF
```

**Setup GitHub Secrets:**

```
EC2_IP: <your-ec2-public-ip>
EC2_KEY: <contents-of-private-key.pem>
DOCKER_USERNAME: <your-docker-username>
DOCKER_PASSWORD: <your-docker-password>
```

---

## 5️⃣ Health Checks & Monitoring

### Application Health Endpoint

```bash
# Add to Spring Boot (health is built-in)
curl http://localhost:8085/actuator/health

# Response
{"status":"UP","components":{"db":{"status":"UP"}}}
```

### Database Connection Test

```bash
curl -X GET http://localhost:8085/api/comments

# Should return empty array or existing comments
```

### Monitor Container

```bash
# CPU & Memory usage
docker stats yomu-forum

# View logs
docker logs yomu-forum --follow --tail 50

# Inspect container
docker inspect yomu-forum
```

### Uptime Monitoring

Use external monitoring:

- **Uptime Robot** (free): Monitor HTTP endpoint every 5 minutes
- **DataDog**: Track metrics, logs, traces
- **Prometheus + Grafana**: Detailed metrics
- **CloudWatch** (AWS): Native monitoring

---

## 6️⃣ Backup & Disaster Recovery

### Database Backup

```bash
# Supabase Backup (automatic daily)
# Backup already handled by Supabase

# Manual backup (if self-hosted PostgreSQL)
pg_dump -h localhost -U postgres yomu_forum > backup.sql

# Restore
psql -h localhost -U postgres yomu_forum < backup.sql
```

### Application Backup

```bash
# Backup Docker image
docker save yomu-diskusi-forum:latest | gzip > yomu-forum.tar.gz

# Restore
docker load < yomu-forum.tar.gz
```

---

## 7️⃣ Scaling

### Horizontal Scaling (Multiple Instances)

Using Docker Swarm:

```bash
# Initialize swarm
docker swarm init

# Create service
docker service create \
  --name yomu-forum \
  --replicas 3 \
  --publish 8085:8085 \
  --env-file production.env \
  yomu-diskusi-forum:latest

# Scale up
docker service scale yomu-forum=5
```

Using Kubernetes:

```bash
# Scale replicas
kubectl scale deployment yomu-forum -n yomu-forum --replicas=5
```

### Vertical Scaling (Larger Instance)

```bash
# Update Java heap size
JAVA_OPTS="-Xmx2g -Xms1g"  # Increase from 512m to 2GB

# Restart container
docker restart yomu-forum
```

---

## 📊 Performance Tuning

### Database Optimization

```sql
-- Create indexes (should already exist)
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);
CREATE INDEX idx_reactions_user_id ON reactions(user_id);

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM comments WHERE reading_id = $1;
```

### Java GC Tuning

```bash
JAVA_OPTS="-Xmx512m -Xms256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+ParallelRefProcEnabled"
```

### Connection Pool Optimization

Add to `application.properties`:

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

---

## 🔒 Security Checklist

- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (production)
- [ ] Use environment variables for secrets (not in code)
- [ ] Enable HTTPS (SSL/TLS)
- [ ] Setup firewall rules (allow only necessary ports)
- [ ] Enable database encryption (Supabase does this)
- [ ] Setup regular backups
- [ ] Monitor logs for suspicious activity
- [ ] Keep dependencies updated
- [ ] Use strong database passwords
- [ ] Implement rate limiting (future)

---

## ✅ Deployment Verification

After deployment:

```bash
# 1. Check app is running
curl https://yourdomain.com/api/comments

# 2. Create test comment
curl -X POST https://yourdomain.com/api/comments \
  -H "Content-Type: application/json" \
  -d '{"content":"Test","userId":"...","readingId":"..."}'

# 3. Check logs
docker logs yomu-forum

# 4. Monitor resources
docker stats yomu-forum

# 5. Run health check
curl https://yourdomain.com/actuator/health
```

✅ If all above succeed, deployment is successful!

---

## 📞 Rollback Plan

If deployment has issues:

```bash
# Kubernetes
kubectl rollout undo deployment/yomu-forum -n yomu-forum

# Docker
docker stop yomu-forum
docker run -d ... <previous-image:tag>

# Database (if needed)
psql -h localhost -U postgres yomu_forum < backup.sql
```

---

## 📚 Further Reading

- [Spring Boot Deployment](https://spring.io/guides/gs/spring-boot/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Kubernetes Production](https://kubernetes.io/docs/concepts/production/)
- [PostgreSQL Administration](https://www.postgresql.org/docs/current/admin.html)
