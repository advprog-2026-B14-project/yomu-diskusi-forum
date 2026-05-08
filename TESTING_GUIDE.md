# 🧪 Testing Guide - Yomu Diskusi Forum

Comprehensive guide untuk testing aplikasi Yomu Diskusi Forum dari unit tests hingga end-to-end testing.

---

## 📋 Daftar Isi

1. [Unit Testing](#unit-testing)
2. [Integration Testing](#integration-testing)
3. [Manual Testing dengan Postman](#manual-testing-dengan-postman)
4. [Manual Testing dengan cURL](#manual-testing-dengan-curl)
5. [Test Coverage](#test-coverage)
6. [Common Test Scenarios](#common-test-scenarios)

---

## 1️⃣ Unit Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
# Test Comment Controller
./gradlew test --tests CommentControllerTest

# Test Comment Service
./gradlew test --tests CommentServiceImplTest

# Test Reaction Service
./gradlew test --tests ReactionServiceImplTest
```

### Run Tests with Specific Pattern

```bash
# Test all service classes
./gradlew test --tests "*Service*"

# Test all controller classes
./gradlew test --tests "*Controller*"
```

### Expected Output

```
> Task :test
...
CommentControllerTest > testCreateCommentSuccess PASSED
CommentControllerTest > testCreateCommentWithEmptyContent FAILED
...

BUILD FAILED

Total time: 15.234s
```

### Debug Failed Test

```bash
# Run test with full output
./gradlew test --tests CommentControllerTest -i --stacktrace

# Or in IDE:
# Right-click test class → Run with Coverage
```

---

## 2️⃣ Integration Testing

### Database Setup for Testing

Tests menggunakan H2 in-memory database (default) atau PostgreSQL (jika dikonfigurasi).

#### Using H2 (Recommended for Tests)

File: `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

### Run Tests with Specific Profile

```bash
./gradlew test --args='--spring.profiles.active=test'
```

### Test Database State

```bash
# In test code:
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class CommentRepositoryTest {

    @Test
    public void testFindByReadingId() {
        // Test will use in-memory H2 database
    }
}
```

---

## 3️⃣ Manual Testing dengan Postman

### Setup Postman

#### 1. Download & Install

- https://www.postman.com/downloads/

#### 2. Import Collection

```json
{
  "info": {
    "name": "Yomu Diskusi Forum API",
    "version": "1.0.0"
  },
  "item": [
    {
      "name": "Comments",
      "item": [
        {
          "name": "Create Comment",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"content\": \"Great explanation!\",\n  \"userId\": \"{{userId}}\",\n  \"readingId\": \"{{readingId}}\",\n  \"parentCommentId\": null\n}"
            },
            "url": {
              "raw": "{{base_url}}/comments",
              "host": ["{{base_url}}"],
              "path": ["comments"]
            }
          }
        }
      ]
    }
  ]
}
```

#### 3. Setup Environment Variables

1. Click **Environments** (left sidebar)
2. Create new environment: **Yomu Dev**
3. Add variables:
   - `base_url`: http://localhost:8085/api
   - `userId`: 550e8400-e29b-41d4-a716-446655440000
   - `readingId`: 770e8400-e29b-41d4-a716-446655440001
   - `commentId`: (empty, will be filled after creating comment)
   - `reactionId`: (empty, will be filled after creating reaction)

4. Save environment

#### 4. Create Test Requests

**Create Comment**

```
POST {{base_url}}/comments
Content-Type: application/json

{
  "content": "This is a great learning material!",
  "userId": "{{userId}}",
  "readingId": "{{readingId}}",
  "parentCommentId": null
}
```

**Save Response to Variable (Tests tab)**

```javascript
// Extract commentId from response for next requests
if (pm.response.code === 201) {
  var jsonData = pm.response.json();
  pm.environment.set("commentId", jsonData.id);
  console.log("Comment created: " + jsonData.id);
}
```

**Get All Comments**

```
GET {{base_url}}/comments
```

**Get Comments by Reading**

```
GET {{base_url}}/comments/reading/{{readingId}}
```

**Create Reply (Nested Comment)**

```
POST {{base_url}}/comments
Content-Type: application/json

{
  "content": "I agree with this point!",
  "userId": "{{userId}}",
  "readingId": "{{readingId}}",
  "parentCommentId": "{{commentId}}"
}
```

**Update Comment**

```
PUT {{base_url}}/comments/{{commentId}}
Content-Type: application/json

{
  "content": "Updated: This is even better!",
  "userId": "{{userId}}",
  "readingId": "{{readingId}}"
}
```

**Add Reaction**

```
POST {{base_url}}/reactions
Content-Type: application/json

{
  "commentId": "{{commentId}}",
  "userId": "{{userId}}",
  "reactionType": "UPVOTE"
}
```

**Get Reactions by Comment**

```
GET {{base_url}}/reactions/comment/{{commentId}}
```

**Delete Reaction**

```
DELETE {{base_url}}/reactions/{{reactionId}}?userId={{userId}}
```

**Delete Comment (Self)**

```
DELETE {{base_url}}/comments/{{commentId}}?userId={{userId}}&isAdmin=false
```

**Delete Comment (Admin)**

```
DELETE {{base_url}}/comments/{{commentId}}?userId={{adminUserId}}&isAdmin=true
```

---

## 4️⃣ Manual Testing dengan cURL

### Basic cURL Commands

#### Create Comment

```bash
curl -X POST http://localhost:8085/api/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Great explanation!",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "readingId": "770e8400-e29b-41d4-a716-446655440001",
    "parentCommentId": null
  }'
```

#### Get All Comments

```bash
curl -X GET http://localhost:8085/api/comments \
  -H "Content-Type: application/json"
```

#### Get Comment by ID

```bash
curl -X GET http://localhost:8085/api/comments/8cb1b222-00f2-448c-a0e9-eb11cd2058e5 \
  -H "Content-Type: application/json"
```

#### Get Comments by Reading

```bash
curl -X GET http://localhost:8085/api/comments/reading/770e8400-e29b-41d4-a716-446655440001 \
  -H "Content-Type: application/json"
```

#### Get Comments by User

```bash
curl -X GET http://localhost:8085/api/comments/user/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json"
```

#### Update Comment

```bash
curl -X PUT http://localhost:8085/api/comments/8cb1b222-00f2-448c-a0e9-eb11cd2058e5 \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Updated comment",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "readingId": "770e8400-e29b-41d4-a716-446655440001"
  }'
```

#### Delete Comment

```bash
curl -X DELETE "http://localhost:8085/api/comments/8cb1b222-00f2-448c-a0e9-eb11cd2058e5?userId=550e8400-e29b-41d4-a716-446655440000&isAdmin=false" \
  -H "Content-Type: application/json"
```

#### Add Reaction

```bash
curl -X POST http://localhost:8085/api/reactions \
  -H "Content-Type: application/json" \
  -d '{
    "commentId": "8cb1b222-00f2-448c-a0e9-eb11cd2058e5",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "reactionType": "UPVOTE"
  }'
```

#### Get Reactions by Comment

```bash
curl -X GET http://localhost:8085/api/reactions/comment/8cb1b222-00f2-448c-a0e9-eb11cd2058e5 \
  -H "Content-Type: application/json"
```

### Save Response to Variable (Bash)

```bash
# Create comment dan simpan response
RESPONSE=$(curl -s -X POST http://localhost:8085/api/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Test comment",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "readingId": "770e8400-e29b-41d4-a716-446655440001",
    "parentCommentId": null
  }')

# Extract ID (using jq)
COMMENT_ID=$(echo $RESPONSE | jq -r '.id')
echo "Created comment: $COMMENT_ID"

# Use ID untuk next request
curl -X GET http://localhost:8085/api/comments/$COMMENT_ID \
  -H "Content-Type: application/json"
```

---

## 5️⃣ Test Coverage

### Generate Coverage Report

```bash
./gradlew test jacocoTestReport
```

### View Report

- **HTML Report**: `build/reports/jacoco/test/html/index.html`
- Open in browser to see:
  - Line coverage percentage
  - Branch coverage
  - Coverage by class/method

### Check Coverage Threshold

Add to `build.gradle.kts`:

```kotlin
plugins {
    id("jacoco")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map { configureSourceSets(it) }))
    }
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            element = "CLASS"
            includes = listOf("id.ac.ui.cs.advprog.yomuforum.*")

            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

---

## 6️⃣ Common Test Scenarios

### Scenario 1: Basic Comment Workflow

```
1. Create comment on reading
   POST /api/comments → 201 Created

2. Get all comments
   GET /api/comments → 200 OK with array

3. Get comments by reading ID
   GET /api/comments/reading/{id} → 200 OK with filtered array

4. Update comment
   PUT /api/comments/{id} → 200 OK with updated data

5. Delete comment
   DELETE /api/comments/{id} → 204 No Content
```

### Scenario 2: Nested Comments (Replies)

```
1. Create parent comment
   POST /api/comments (parentCommentId: null)

2. Create reply
   POST /api/comments (parentCommentId: parent_id)

3. Get all replies
   GET /api/comments/parent/{parent_id} → array of replies

4. Edit reply
   PUT /api/comments/{reply_id}

5. Delete reply
   DELETE /api/comments/{reply_id}
```

### Scenario 3: Reactions

```
1. Create comment
   POST /api/comments → save comment ID

2. Add upvote
   POST /api/reactions (reactionType: UPVOTE)

3. Get reactions
   GET /api/reactions/comment/{comment_id}

4. Change reaction (replace)
   POST /api/reactions (reactionType: DOWNVOTE)
   → Should update previous reaction

5. Delete reaction
   DELETE /api/reactions/{reaction_id}
```

### Scenario 4: Authorization

```
1. Create comment as User A
   POST /api/comments (userId: user_a_id)

2. Try to edit as User B (should fail)
   PUT /api/comments/{id} (userId: user_b_id)
   → 403 Forbidden

3. Edit as User A (should succeed)
   PUT /api/comments/{id} (userId: user_a_id)
   → 200 OK

4. Delete as User B with admin flag
   DELETE /api/comments/{id} (userId: user_b_id, isAdmin: true)
   → 204 No Content
```

### Scenario 5: Input Validation

```
1. Create comment with empty content
   POST /api/comments (content: "")
   → 400 Bad Request

2. Create comment with non-existent parent
   POST /api/comments (parentCommentId: invalid_uuid)
   → 400 Bad Request

3. Add invalid reaction type
   POST /api/reactions (reactionType: "LIKE")
   → 400 Bad Request

4. Get non-existent comment
   GET /api/comments/{invalid_uuid}
   → 404 Not Found
```

---

## 🔧 Test Debugging

### Enable SQL Logging

Add to `application-test.properties`:

```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### View Test Logs

```bash
./gradlew test --info
```

### Print Debug Info in Tests

```java
@Test
public void testCreateComment() {
    // Print variable values
    System.out.println("Comment ID: " + comment.getId());
    System.out.println("Created At: " + comment.getCreatedAt());

    // Add breakpoints in IDE
    // Or use logger
    logger.debug("Comment created: {}", comment);
}
```

---

## ✅ Testing Checklist

- [ ] All unit tests pass: `./gradlew test`
- [ ] Test coverage > 80%: Check `build/reports/jacoco/test/html/index.html`
- [ ] Create comment: `POST /api/comments` → 201
- [ ] Read comment: `GET /api/comments/{id}` → 200
- [ ] Update comment: `PUT /api/comments/{id}` → 200
- [ ] Delete comment: `DELETE /api/comments/{id}` → 204
- [ ] Create nested comment (reply): `POST /api/comments` with `parentCommentId`
- [ ] Add reaction: `POST /api/reactions` → 201
- [ ] Update reaction (upsert): `POST /api/reactions` with existing user+comment
- [ ] Delete reaction: `DELETE /api/reactions/{id}` → 204
- [ ] Forbidden error on unauthorized delete: → 403
- [ ] Bad request on empty content: → 400
- [ ] Not found error on missing comment: → 404

---

## 📚 Advanced Testing

### Performance Testing (with JMH)

Add to `build.gradle.kts`:

```kotlin
plugins {
    id("me.champeau.jmh") version "0.7.0"
}

jmh {
    jmhVersion = "1.35"
}
```

Run performance tests:

```bash
./gradlew jmh
```

### Mutation Testing (with Pitest)

```kotlin
plugins {
    id("info.solidsoft.pitest") version "1.14.5"
}

pitest {
    targetClasses = listOf("id.ac.ui.cs.advprog.yomuforum.*")
    targetTests = listOf("id.ac.ui.cs.advprog.yomuforum.*Test")
    junit5PluginVersion = "1.1.0"
}
```

Run:

```bash
./gradlew pitest
```

---

## 📞 Support

Jika ada test yang gagal:

1. Baca error message dengan teliti
2. Check test logs: `./gradlew test --stacktrace`
3. Run test individual dengan debug mode
4. Check database state (untuk integration tests)
5. Verify environment variables (untuk IT tests)
