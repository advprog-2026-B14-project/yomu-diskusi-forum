# 🚀 Quick Start Guide - Yomu Diskusi Forum

Panduan cepat untuk developer yang ingin segera mulai coding.

---

## ⚡ 5 Menit Setup (Local Development)

### 1. Prasyarat

```bash
# Verify Java installed
java -version
# Expected: openjdk version "21" or higher

# Verify Git installed
git --version
```

### 2. Clone & Navigate

```bash
git clone https://github.com/<org>/yomu-diskusi-forum.git
cd yomu-diskusi-forum
```

### 3. Setup Database (Choose One)

#### A. Local PostgreSQL (fastest)

```bash
# Ensure PostgreSQL running
psql -U postgres

# Create database
CREATE DATABASE yomu_forum;

# Exit psql
\q

# Create .env file in project root
cat > .env << EOF
DB_URL=jdbc:postgresql://localhost:5432/yomu_forum
DB_USERNAME=postgres
DB_PASSWORD=your_password
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000
EOF
```

**Then run schema setup:**

```bash
psql -U postgres -d yomu_forum << EOF
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
```

#### B. Supabase (easiest)

1. Go to https://supabase.com/ → Create account
2. Create new project
3. Copy "Connection string" (JDBC) from Settings → Database
4. Create `.env`:

```bash
cat > .env << EOF
DB_URL=<paste_supabase_jdbc_url>
DB_USERNAME=postgres
DB_PASSWORD=<paste_supabase_password>
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000
EOF
```

5. Create tables in Supabase SQL Editor (copy from section A above)

### 4. Build & Run

```bash
# Build
./gradlew clean build -x test

# Run
./gradlew bootRun
```

**Expected Output:**

```
... YomuForumApplication : Started YomuForumApplication in 5.234s
```

### 5. Test API

```bash
# Open new terminal
curl http://localhost:8085/api/comments
# Expected: []
```

✅ **Done!** API ready at `http://localhost:8085/api`

---

## 🧪 Quick Test Flow

### Test Comments

```bash
# 1. Create comment
COMMENT=$(curl -s -X POST http://localhost:8085/api/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Great learning material!",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "readingId": "770e8400-e29b-41d4-a716-446655440001"
  }')

COMMENT_ID=$(echo $COMMENT | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
echo "Created comment: $COMMENT_ID"

# 2. Get comment
curl http://localhost:8085/api/comments/$COMMENT_ID | json_pp

# 3. Update comment
curl -X PUT http://localhost:8085/api/comments/$COMMENT_ID \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Updated!",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "readingId": "770e8400-e29b-41d4-a716-446655440001"
  }'

# 4. Delete comment
curl -X DELETE "http://localhost:8085/api/comments/$COMMENT_ID?userId=550e8400-e29b-41d4-a716-446655440000"
```

### Test Reactions

```bash
# 1. Create comment first (see above)
# 2. Add reaction
curl -X POST http://localhost:8085/api/reactions \
  -H "Content-Type: application/json" \
  -d '{
    "commentId": "'$COMMENT_ID'",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "reactionType": "UPVOTE"
  }'

# 3. Get reactions
curl http://localhost:8085/api/reactions/comment/$COMMENT_ID
```

---

## 📚 Project Structure (Important Files)

```
yomu-diskusi-forum/
├── src/main/java/id/ac/ui/cs/advprog/yomuforum/
│   ├── controller/
│   │   ├── CommentController.java      # REST API endpoints
│   │   └── ReactionController.java
│   ├── service/
│   │   ├── CommentServiceImpl.java      # Business logic
│   │   └── ReactionServiceImpl.java
│   ├── model/
│   │   ├── Comment.java                # JPA entity
│   │   ├── Reaction.java
│   │   └── ReactionType.java           # Enum
│   ├── repository/
│   │   ├── CommentRepository.java      # Data access
│   │   └── ReactionRepository.java
│   ├── dto/
│   │   ├── CommentRequest.java         # Input validation
│   │   └── ReactionRequest.java
│   ├── exception/
│   │   ├── ForbiddenException.java     # Custom errors
│   │   └── InvalidInputException.java
│   ├── config/
│   │   ├── WebConfig.java              # CORS config
│   │   └── GlobalExceptionHandler.java # Error handling
│   └── YomuForumApplication.java       # Main class
├── src/test/java/...                   # Unit tests
├── .env                                # Environment variables
├── build.gradle.kts                    # Build config
├── README.md                           # Full documentation
├── INSTALLATION_GUIDE.md               # Detailed setup
├── TESTING_GUIDE.md                    # Testing strategies
├── ARCHITECTURE.md                     # Design patterns
└── Yomu_Diskusi_Forum_API.postman_collection.json
```

---

## 🔧 Common Commands

```bash
# Build (skip tests)
./gradlew build -x test

# Run app
./gradlew bootRun

# Run tests
./gradlew test

# Run specific test
./gradlew test --tests CommentControllerTest

# Build JAR
./gradlew bootJar

# Generate test coverage
./gradlew test jacocoTestReport
# View: build/reports/jacoco/test/html/index.html

# Clean project
./gradlew clean

# Check Java version in gradle
./gradlew -v

# Run with specific port
./gradlew bootRun --args='--server.port=8086'
```

---

## 💡 Development Tips

### IDE Setup (VS Code)

1. Install "Extension Pack for Java"
2. Open project folder
3. Wait for language server to start
4. F5 to run, or use terminal

### IDE Setup (IntelliJ IDEA)

1. File → Open → select project folder
2. Wait for Gradle sync
3. Right-click `YomuForumApplication.java` → Run
4. Or use Run → Run 'YomuForumApplication'

### Hot Reload (Spring Boot DevTools)

Already included! Just run `./gradlew bootRun` and:

- Save Java file → auto-recompile
- Browser refresh → see changes (for controllers)

### Debug Mode

```bash
# Terminal 1: Start app with debugging
./gradlew bootRun --debug

# Terminal 2: Attach debugger in IDE (if using IDE)
# Breakpoints will work automatically
```

---

## 🚨 Troubleshooting Quick Fixes

| Problem                   | Solution                                                     |
| ------------------------- | ------------------------------------------------------------ |
| Database connection error | Check `.env` file exists and `DB_PASSWORD` is correct        |
| Port 8085 already in use  | Change port in `application.properties`: `server.port=8086`  |
| Java version error        | Run `java -version`; must be 21+                             |
| Gradle build fails        | Run `./gradlew clean --refresh-dependencies`                 |
| Tests fail                | Run `./gradlew test --stacktrace` for details                |
| Can't connect to DB       | Verify PostgreSQL/Supabase is running, credentials in `.env` |

---

## 📖 Next Steps

1. **Learn API**: Read [README.md](./README.md) - full API docs
2. **Setup Complete**: See [INSTALLATION_GUIDE.md](./INSTALLATION_GUIDE.md)
3. **Test API**: Use Postman collection: `Yomu_Diskusi_Forum_API.postman_collection.json`
4. **Write Tests**: Check [TESTING_GUIDE.md](./TESTING_GUIDE.md)
5. **Deploy**: Follow [INSTALLATION_GUIDE.md](./INSTALLATION_GUIDE.md#-production-deployment)
6. **Understand Code**: Read [ARCHITECTURE.md](./ARCHITECTURE.md) for patterns

---

## 🤔 Quick Questions

**Q: How do I add a new API endpoint?**

```java
// 1. Add method to service interface
public interface CommentService {
    List<Comment> searchByContent(String keyword);
}

// 2. Implement in service class
@Override
public List<Comment> searchByContent(String keyword) {
    return commentRepository.findByContentContaining(keyword);
}

// 3. Add to repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByContentContaining(String keyword);
}

// 4. Add controller endpoint
@GetMapping("/comments/search")
public List<Comment> search(@RequestParam String keyword) {
    return commentService.searchByContent(keyword);
}
```

**Q: How do I handle a new error?**

```java
// 1. Create custom exception
public class CommentTooLongException extends RuntimeException {
    public CommentTooLongException(String msg) { super(msg); }
}

// 2. Add handler to GlobalExceptionHandler
@ExceptionHandler(CommentTooLongException.class)
public ResponseEntity<ErrorResponse> handle(CommentTooLongException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("Bad Request", ex.getMessage()));
}

// 3. Throw in service
if (content.length() > 5000) {
    throw new CommentTooLongException("Comment too long");
}
```

**Q: How do I run migrations/schema updates?**

- Set `spring.jpa.hibernate.ddl-auto=update` temporarily in `application.properties`
- Start app once (will update schema)
- Set back to `validate` for production safety

---

## 📞 Help

- Check [README.md](./README.md#troubleshooting) for more troubleshooting
- Check test files for usage examples: `src/test/java/...`
- Read code comments in controller/service classes
- Slack/Email team for questions
