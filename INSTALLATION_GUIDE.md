# 📖 Panduan Instalasi Lengkap - Yomu Diskusi Forum

Panduan step-by-step untuk setup local development environment dan production deployment.

---

## 1️⃣ Prerequisites

### System Requirements

- **OS**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **RAM**: Minimum 4GB (recommended 8GB untuk development)
- **Disk Space**: Minimal 2GB

### Required Software

| Software   | Version                 | Purpose                                 |
| ---------- | ----------------------- | --------------------------------------- |
| Java JDK   | 21+                     | Runtime & compilation                   |
| Gradle     | 8.0+                    | Build automation (included via wrapper) |
| Git        | 2.25+                   | Version control                         |
| PostgreSQL | 12+ OR Supabase account | Database                                |

---

## 2️⃣ Development Setup (Local Machine)

### Step 1: Install Java 21

#### Windows

```powershell
# Using Chocolatey (if installed)
choco install openjdk21

# OR download from https://adoptium.net/
# Then set environment variable
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.0"
```

#### macOS

```bash
# Using Homebrew
brew install openjdk@21

# Link Java (if needed)
sudo ln -sfn /usr/local/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Verify
java -version
```

#### Linux (Ubuntu)

```bash
sudo apt update
sudo apt install openjdk-21-jdk

# Verify
java -version
```

---

### Step 2: Clone Repository

```bash
git clone https://github.com/<org>/yomu-diskusi-forum.git
cd yomu-diskusi-forum
```

---

### Step 3: Setup Database

#### Option A: Local PostgreSQL

```bash
# Install PostgreSQL
# Windows: https://www.postgresql.org/download/windows/
# macOS: brew install postgresql
# Linux: sudo apt install postgresql

# Create database
psql -U postgres
```

```sql
CREATE DATABASE yomu_forum;
\c yomu_forum

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
CREATE INDEX idx_comments_parent_id ON comments(parent_comment_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_reactions_comment_id ON reactions(comment_id);
```

Then create `.env`:

```properties
DB_URL=jdbc:postgresql://localhost:5432/yomu_forum
DB_USERNAME=postgres
DB_PASSWORD=your_password
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

#### Option B: Supabase (Cloud)

1. Create account di https://supabase.com/
2. Create new project
3. Go to **Settings → Database**
4. Copy "Connection string" (use JDBC format)
5. Update `.env`:

```properties
DB_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<password-dari-supabase>
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

6. Create tables in Supabase SQL Editor (copy schema dari Option A)

---

### Step 4: Build & Run Locally

```bash
# Build project
./gradlew clean build

# Or skip tests during development
./gradlew clean build -x test

# Run application
./gradlew bootRun
```

**Expected Output:**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.2)

... YomuForumApplication : Started YomuForumApplication in 5.234s (JVM running for 6.123s)
```

✅ **Success!** App is running at `http://localhost:8085`

---

## 3️⃣ Verify Installation

### Test API Endpoints

```bash
# Test GET /api/comments
curl -X GET http://localhost:8085/api/comments \
  -H "Content-Type: application/json"

# Expected response (empty array initially):
# []
```

### Or use Postman

1. Download Postman: https://www.postman.com/downloads/
2. Import collection (will be provided)
3. Set environment variables:
   - `base_url`: http://localhost:8085/api
   - `userId`: any UUID (e.g., 550e8400-e29b-41d4-a716-446655440000)
   - `readingId`: any UUID (e.g., 770e8400-e29b-41d4-a716-446655440001)

4. Test each endpoint

---

## 4️⃣ Production Deployment

### Option A: EC2 (AWS)

#### Prerequisites

- AWS account with EC2 instance
- Ubuntu 20.04 LTS or later
- Security group allows inbound on port 8085, 22
- SSH key pair configured

#### Step 1: SSH into EC2

```bash
ssh -i "your-key.pem" ubuntu@<ec2-public-ip>
```

#### Step 2: Install Java & Docker

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk docker.io git

# Add user to docker group (optional, for sudo-less docker commands)
sudo usermod -aG docker $USER
```

#### Step 3: Clone & Build

```bash
git clone https://github.com/<org>/yomu-diskusi-forum.git
cd yomu-diskusi-forum

./gradlew clean bootJar -x test
```

#### Step 4: Build Docker Image

```bash
docker build -t yomu-diskusi-forum:latest .
```

#### Step 5: Run Container

```bash
docker run -d \
  -p 8085:8085 \
  -e DB_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=<your_password> \
  -e APP_CORS_ALLOWED_ORIGINS=https://yourdomain.com \
  --name yomu-forum \
  yomu-diskusi-forum:latest
```

**Verify:**

```bash
docker logs -f yomu-forum
curl http://localhost:8085/api/comments
```

---

### Option B: Docker Compose (Local Multi-container)

Create `docker-compose.yml`:

```yaml
version: "3.8"
services:
  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: yomu_forum
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    environment:
      DB_URL: jdbc:postgresql://db:5432/yomu_forum
      DB_USERNAME: postgres
      DB_PASSWORD: postgres123
      APP_CORS_ALLOWED_ORIGINS: http://localhost:3000
    ports:
      - "8085:8085"
    depends_on:
      db:
        condition: service_healthy

volumes:
  postgres_data:
```

Run:

```bash
docker-compose up -d
```

---

### Option C: Kubernetes (Advanced)

Create `k8s-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: yomu-forum
  labels:
    app: yomu-forum
spec:
  replicas: 3
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
          image: <docker-registry>/yomu-diskusi-forum:latest
          ports:
            - containerPort: 8085
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
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8085
            initialDelaySeconds: 30
            periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: yomu-forum-service
spec:
  selector:
    app: yomu-forum
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8085
  type: LoadBalancer
```

Deploy:

```bash
kubectl apply -f k8s-deployment.yaml
kubectl get services yomu-forum-service
```

---

## 5️⃣ Configuration Checklist

- [ ] Java 21+ installed (`java -version`)
- [ ] Gradle wrapper executable (`./gradlew -v`)
- [ ] `.env` file created with DB credentials
- [ ] Database schema created (tables & indexes)
- [ ] Application builds successfully (`./gradlew build`)
- [ ] Application starts without errors (`./gradlew bootRun`)
- [ ] At least one API endpoint responds (`GET /api/comments`)
- [ ] Unit tests pass (`./gradlew test`)
- [ ] Docker image builds (if using Docker)

---

## 6️⃣ Troubleshooting Installation

### Java Not Found

```bash
# Check java installation
java -version

# If not found, add to PATH (Windows)
setx PATH "%PATH%;C:\Program Files\Eclipse Adoptium\jdk-21.0.0\bin"

# Restart terminal and try again
```

### Gradle Build Fails

```bash
# Clean gradle cache
./gradlew clean --refresh-dependencies

# Try building again with more output
./gradlew build --stacktrace --info
```

### Database Connection Error

```
Error: "FATAL: Tenant or user not found"
```

**Fixes:**

1. Verify credentials in `.env` match database
2. Check if database service is running
3. Test connection with `psql`:
   ```bash
   psql -h localhost -U postgres -d yomu_forum
   ```
4. For Supabase: ensure username is `postgres` (not with suffix)

### Port Already in Use

```bash
# Windows: Find & kill process using port 8085
netstat -ano | findstr :8085
taskkill /PID <PID> /F

# Linux/macOS
lsof -i :8085
kill -9 <PID>
```

### Build Success but App Won't Start

1. Check all environment variables are set: `echo $DB_URL`
2. Verify database tables exist: `\dt` in psql
3. Check logs: `./gradlew bootRun` will show errors
4. Verify Spring Boot version: check `build.gradle.kts` for compatibility

---

## 7️⃣ IDE Setup (Optional)

### IntelliJ IDEA

1. Open project: `File → Open → select yomu-diskusi-forum folder`
2. IDE auto-detects Gradle: `Enable` when prompted
3. Wait for indexing to complete
4. Run app: `Run → Run 'YomuForumApplication'`

### VS Code

1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - REST Client

2. Open project folder
3. Wait for language server to start
4. Run via terminal: `./gradlew bootRun`

### Eclipse

1. File → Import → Gradle → Existing Gradle Project
2. Select project folder
3. Right-click project → Run As → Spring Boot App

---

## ✅ Next Steps

1. Read [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for endpoint details
2. Check [TESTING_GUIDE.md](./TESTING_GUIDE.md) for testing strategies
3. Review [ARCHITECTURE.md](./ARCHITECTURE.md) for design patterns
4. Start developing! 🚀
