# 🏗️ Architecture & Design Documentation - Yomu Diskusi Forum

Comprehensive documentation tentang architecture, design patterns, dan technical decisions untuk Yomu Diskusi Forum.

---

## 📋 Daftar Isi

1. [System Architecture](#system-architecture)
2. [Data Model](#data-model)
3. [Design Patterns](#design-patterns)
4. [Component Interactions](#component-interactions)
5. [Error Handling](#error-handling)
6. [Security Considerations](#security-considerations)
7. [Performance Considerations](#performance-considerations)
8. [Future Improvements](#future-improvements)

---

## 1️⃣ System Architecture

### Microservices Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Web/Mobile)                 │
│              (React.js / Flutter / etc.)                 │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
┌───────▼──────┐  ┌─────▼──────┐  ┌─────▼──────┐
│  Auth Module │  │ Yomu Forum │  │   Books    │
│  (Port 8080) │  │ (Port 8085)│  │  (Port....) │
└──────────────┘  └──────┬─────┘  └────────────┘
                         │
                  ┌──────▼──────┐
                  │ PostgreSQL  │
                  │ (Supabase)  │
                  └─────────────┘
```

### Yomu Forum Service Architecture

```
┌──────────────────────────────────────────────────────┐
│              REST API Controller Layer                │
│  ┌────────────────┐         ┌──────────────────┐    │
│  │ Comment        │         │ Reaction         │    │
│  │ Controller     │         │ Controller       │    │
│  └────────────────┘         └──────────────────┘    │
└─────────────────┬────────────────────┬───────────────┘
                  │                    │
┌─────────────────▼────────────────────▼───────────────┐
│          Service Layer (Business Logic)              │
│  ┌────────────────┐         ┌──────────────────┐    │
│  │ CommentService │         │ ReactionService  │    │
│  │ Impl           │         │ Impl             │    │
│  └────────────────┘         └──────────────────┘    │
└─────────────────┬────────────────────┬───────────────┘
                  │                    │
┌─────────────────▼────────────────────▼───────────────┐
│        Repository Layer (Data Access)                │
│  ┌────────────────┐         ┌──────────────────┐    │
│  │ CommentRepository        │ ReactionRepository    │
│  │ (JpaRepository)│         │ (JpaRepository)  │    │
│  └────────────────┘         └──────────────────┘    │
└─────────────────┬────────────────────┬───────────────┘
                  │                    │
┌─────────────────▼────────────────────▼───────────────┐
│              JPA/Hibernate ORM                       │
└─────────────────┬────────────────────┬───────────────┘
                  │                    │
         ┌────────▼────────┐           │
         │ Comment Entity  │           │
         │ (JPA)           │           │
         └─────────────────┘           │
                                       │
                            ┌──────────▼──────────┐
                            │ Reaction Entity     │
                            │ (JPA)               │
                            └─────────────────────┘
                                       │
         ┌─────────────────────────────▼──────────────┐
         │     PostgreSQL Database (Supabase)        │
         │  ┌─────────────┐    ┌────────────────┐   │
         │  │ comments    │    │ reactions      │   │
         │  │ table       │    │ table          │   │
         │  └─────────────┘    └────────────────┘   │
         └────────────────────────────────────────────┘
```

---

## 2️⃣ Data Model

### Entity Relationship Diagram

```
┌──────────────────────────────┐
│      comments                 │
├──────────────────────────────┤
│ id (UUID) [PK]               │
│ user_id (UUID)               │
│ reading_id (UUID)            │
│ parent_comment_id (UUID) [FK]│◄─────┐
│ content (TEXT)               │      │
│ created_at (TIMESTAMP)       │      │
│ updated_at (TIMESTAMP)       │      │
└───────────────┬──────────────┘      │
                │                     │ (Self-referencing)
                │ (1:N)               │
                │                     │
┌───────────────▼──────────────┐      │
│     reactions                 │      │
├──────────────────────────────┤      │
│ id (UUID) [PK]               │      │
│ comment_id (UUID) [FK] ──────┼──────┘
│ user_id (UUID)               │
│ reaction_type (VARCHAR)      │
│ created_at (TIMESTAMP)       │
└──────────────────────────────┘

Constraints:
- UNIQUE(comment_id, user_id) on reactions
- Foreign key: reactions.comment_id → comments.id
- Self-referencing: comments.parent_comment_id → comments.id
```

### Comment Entity

```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID readingId;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Self-referencing relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", insertable = false, updatable = false)
    private Comment parentComment;
}
```

### Reaction Entity

```java
@Entity
@Table(name = "reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"comment_id", "user_id"})
})
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID commentId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType reactionType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", insertable = false, updatable = false)
    private Comment comment;
}
```

### ReactionType Enum

```java
public enum ReactionType {
    UPVOTE("upvote"),
    DOWNVOTE("downvote"),
    EMOJI_CELEBRATE("celebrate"),
    EMOJI_THUMBS_UP("thumbs-up"),
    EMOJI_LAUGH("laugh"),
    EMOJI_HEART("heart"),
    EMOJI_THINKING("thinking");

    private final String displayName;

    ReactionType(String displayName) {
        this.displayName = displayName;
    }

    public static ReactionType fromString(String value) {
        for (ReactionType type : ReactionType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ReactionType: " + value);
    }
}
```

---

## 3️⃣ Design Patterns

### 3.1 Repository Pattern

**Purpose**: Abstract data access logic dari business logic

**Implementation**:

```java
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByReadingId(UUID readingId);
    List<Comment> findByUserId(UUID userId);
    List<Comment> findByParentCommentId(UUID parentCommentId);
}

// Usage in Service
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment getCommentById(UUID id) {
        return commentRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
    }
}
```

**Benefits**:

- Memisahkan concern: data access vs business logic
- Lebih mudah untuk testing (mock repository)
- Flexible untuk switch database implementation

---

### 3.2 Service Layer Pattern

**Purpose**: Centralized business logic, validation, dan authorization

**Implementation**:

```java
public interface CommentService {
    Comment createComment(CommentRequest request);
    Comment updateComment(UUID id, CommentRequest request);
    void deleteComment(UUID id, UUID userId, boolean isAdmin);
    // ... query methods
}

@Service
public class CommentServiceImpl implements CommentService {

    @Override
    public Comment updateComment(UUID id, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException());

        // Validation
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new InvalidInputException("Content cannot be empty");
        }

        // Authorization
        if (!comment.getUserId().equals(request.getUserId())) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        // Business logic
        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}
```

**Benefits**:

- Centralized business logic
- Authorization & validation in one place
- Reusable across controllers

---

### 3.3 DTO (Data Transfer Object) Pattern

**Purpose**: Decouple API contract dari internal entities

**Implementation**:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    private String content;
    private UUID userId;
    private UUID readingId;
    private UUID parentCommentId;

    // Validation annotations
    @NotBlank(message = "Content is required")
    public String getContent() {
        return content;
    }
}

// Usage in Controller
@PostMapping("/comments")
public ResponseEntity<Comment> createComment(@RequestBody CommentRequest request) {
    Comment comment = commentService.createComment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(comment);
}
```

**Benefits**:

- Kontrolir exactly apa yang diaccept API
- Validation annotations untuk input checking
- Decouple dari entity changes

---

### 3.4 Dependency Injection Pattern

**Purpose**: Loose coupling, easier testing

**Implementation**:

```java
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    // Constructor injection (preferred)
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Or using @RequiredArgsConstructor dari Lombok
    // @Autowired
    // private final CommentService commentService;
}
```

**Benefits**:

- Loosely coupled
- Testable (dapat di-mock)
- Spring container manages lifecycle

---

### 3.5 Enum Pattern for Type Safety

**Purpose**: Ensure valid values, avoid string magic

**Implementation**:

```java
public enum ReactionType {
    UPVOTE, DOWNVOTE, EMOJI_CELEBRATE, EMOJI_THUMBS_UP, EMOJI_LAUGH, EMOJI_HEART, EMOJI_THINKING;

    public static ReactionType fromString(String value) {
        return ReactionType.valueOf(value.toUpperCase());
    }
}

// Usage
@PostMapping("/reactions")
public ResponseEntity<Reaction> addReaction(@RequestBody ReactionRequest request) {
    ReactionType type = ReactionType.fromString(request.getReactionType());
    // ...
}
```

**Benefits**:

- Compile-time safety
- IDE autocomplete
- Prevent invalid values

---

### 3.6 Exception Handling Pattern

**Purpose**: Consistent, meaningful error responses

**Implementation**:

```java
// Custom exceptions
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}

// Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        ErrorResponse error = new ErrorResponse(
            "Forbidden",
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException ex) {
        ErrorResponse error = new ErrorResponse(
            "Bad Request",
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

**Benefits**:

- Consistent error responses
- Centralized error handling
- Meaningful HTTP status codes

---

### 3.7 Idempotent Upsert Pattern (Reactions)

**Purpose**: Multiple identical requests produce same result

**Implementation**:

```java
@Override
public Reaction addReaction(ReactionRequest request) {
    // Delete old reaction for this user+comment if exists
    reactionRepository.deleteByCommentIdAndUserId(
        request.getCommentId(),
        request.getUserId()
    );

    // Create new reaction
    Reaction reaction = new Reaction();
    reaction.setCommentId(request.getCommentId());
    reaction.setUserId(request.getUserId());
    reaction.setReactionType(ReactionType.fromString(request.getReactionType()));
    reaction.setCreatedAt(LocalDateTime.now());

    return reactionRepository.save(reaction);
}
```

**Key**:

- First request: create reaction
- Second request (same user+comment): replace with new reaction
- Third request: same as second

**Benefits**:

- User can change reaction without explicit delete
- No duplicates (UNIQUE constraint prevents it)
- Simpler client logic

---

## 4️⃣ Component Interactions

### Create Comment Flow

```
1. Client sends POST /api/comments
   ↓
2. CommentController.createComment()
   ├─ Parse CommentRequest from body
   ├─ Validate input (@NotBlank on content)
   └─ Call commentService.createComment()
   ↓
3. CommentServiceImpl.createComment()
   ├─ Check content not empty
   ├─ Check parent comment exists (if parentCommentId provided)
   ├─ Create new Comment entity
   └─ Call commentRepository.save()
   ↓
4. CommentRepository.save()
   └─ JPA executes INSERT SQL
   ↓
5. Database (PostgreSQL)
   └─ Insert row, generate UUID
   ↓
6. Service returns Comment
   ↓
7. Controller returns ResponseEntity(201 Created, comment)
   ↓
8. Client receives comment with generated ID
```

### Add Reaction Flow

```
1. Client sends POST /api/reactions
   ↓
2. ReactionController.addReaction()
   ├─ Parse ReactionRequest
   ├─ Validate reactionType
   └─ Call reactionService.addReaction()
   ↓
3. ReactionServiceImpl.addReaction()
   ├─ Check comment exists
   ├─ Delete previous reaction (same user+comment)
   ├─ Create new Reaction
   └─ Call reactionRepository.save()
   ↓
4. Database
   └─ First DELETE, then INSERT
   ↓
5. Service returns new Reaction
   ↓
6. Controller returns ResponseEntity(201 Created, reaction)
```

### Delete Comment with Authorization Flow

```
1. Client sends DELETE /api/comments/{id}?userId=...&isAdmin=false
   ↓
2. CommentController.deleteComment()
   ├─ Extract path variable: id
   ├─ Extract query params: userId, isAdmin
   └─ Call commentService.deleteComment()
   ↓
3. CommentServiceImpl.deleteComment()
   ├─ Get comment from DB
   ├─ Check ownership: userId == comment.userId
   ├─ Check admin flag: isAdmin == true
   ├─ If not owner AND not admin → throw ForbiddenException
   └─ commentRepository.deleteById()
   ↓
4. GlobalExceptionHandler (if exception)
   └─ Return 403 Forbidden
   ↓
5. If success
   └─ Return 204 No Content
```

---

## 5️⃣ Error Handling

### Exception Hierarchy

```
Exception (Java)
    ↓
├─ RuntimeException
│   ├─ ForbiddenException (user not authorized)
│   ├─ InvalidInputException (bad input)
│   └─ CommentNotFoundException (404)
│
└─ Other Spring exceptions
    ├─ DataAccessException
    ├─ HttpMessageNotReadableException
    └─ ...
```

### HTTP Status Codes

| Status             | Meaning          | When                                 |
| ------------------ | ---------------- | ------------------------------------ |
| 200 OK             | Success          | GET successful, PUT successful       |
| 201 Created        | Resource created | POST successful                      |
| 204 No Content     | Success, no body | DELETE successful                    |
| 400 Bad Request    | Invalid input    | Empty content, invalid UUID          |
| 403 Forbidden      | Not authorized   | Delete other's comment as non-admin  |
| 404 Not Found      | Resource missing | Comment/reaction doesn't exist       |
| 500 Internal Error | Server error     | Database error, unexpected exception |

### Example Error Response

```json
{
  "error": "Forbidden",
  "message": "You can only delete your own comments",
  "timestamp": 1715089800000
}
```

---

## 6️⃣ Security Considerations

### 1. Authentication (External)

- Handled by Auth module (separate service)
- JWT token passed in Authorization header
- This service validates userId from token

### 2. Authorization (Internal)

- Ownership check for edit/delete
- Admin flag for override
- Implemented in service layer

```java
if (!isAdmin && !comment.getUserId().equals(currentUserId)) {
    throw new ForbiddenException("Unauthorized");
}
```

### 3. Input Validation

```java
if (content == null || content.isBlank()) {
    throw new InvalidInputException("Content cannot be empty");
}
```

### 4. SQL Injection Prevention

- Using JPA/Hibernate (prepared statements)
- Never concatenate SQL strings
- Parameterized queries via repository methods

### 5. CORS Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = corsOrigins.split(",");
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### 6. No Sensitive Data in Logs

- Don't log passwords, tokens
- Don't log user IDs if sensitive
- Use appropriate log levels (DEBUG vs INFO)

---

## 7️⃣ Performance Considerations

### 1. Database Indexes

```sql
-- Fast lookups
CREATE INDEX idx_comments_reading_id ON comments(reading_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_comment_id);
CREATE INDEX idx_reactions_comment_id ON reactions(comment_id);

-- Unique constraint (also acts as index)
ALTER TABLE reactions ADD UNIQUE(comment_id, user_id);
```

### 2. Lazy Loading

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_comment_id")
private Comment parentComment;
```

- Don't fetch parent comment unless explicitly requested
- Prevents N+1 query problem

### 3. Query Optimization

```java
// ❌ BAD: N+1 query problem
List<Comment> comments = commentRepository.findAll();
for (Comment c : comments) {
    System.out.println(c.getParentComment()); // Extra query!
}

// ✅ GOOD: Single query with join
@Query("SELECT c FROM Comment c LEFT JOIN FETCH c.parentComment")
List<Comment> findAllWithParent();
```

### 4. Pagination for Large Results

```java
@Query("SELECT c FROM Comment c WHERE c.readingId = ?1")
Page<Comment> findByReadingId(UUID readingId, Pageable pageable);

// Usage
Page<Comment> page = commentRepository.findByReadingId(readingId,
    PageRequest.of(0, 20, Sort.by("createdAt").descending()));
```

### 5. Caching (Future)

```java
@Cacheable("comments")
public Comment getCommentById(UUID id) {
    return commentRepository.findById(id).orElse(null);
}

@CacheEvict("comments", key = "#id")
public void deleteComment(UUID id) {
    commentRepository.deleteById(id);
}
```

---

## 8️⃣ Future Improvements

### 1. Pagination

```
GET /api/comments/reading/{id}?page=0&size=20&sort=createdAt,desc
```

### 2. Search & Filtering

```
GET /api/comments/search?content=keyword&readingId=...&fromDate=2026-01-01
```

### 3. Event Sourcing

```
For audit trail of all comment changes
CommentCreated → CommentEdited → CommentDeleted
```

### 4. Comment Moderation

```
Status: PENDING, APPROVED, REJECTED
ReviewedBy, ReviewedAt fields
```

### 5. Notifications

```
Webhook untuk notify users:
- New reply to their comment
- New reaction on their comment
- Comment deleted
```

### 6. Analytics

```
- Top comments by reactions
- Most active users
- Comment frequency over time
```

### 7. Rate Limiting

```
Prevent spam: max 10 comments per minute per user
```

### 8. Full-Text Search

```
Database-level search (PostgreSQL FTS)
Fast content search across all comments
```

---

## Summary

**Key Architectural Decisions:**

1. ✅ Layered architecture (Controller → Service → Repository)
2. ✅ Service layer handles business logic & authorization
3. ✅ DTOs separate API contract from entities
4. ✅ Enums for type safety (ReactionType)
5. ✅ Global exception handling for consistent errors
6. ✅ JPA with lazy loading for performance
7. ✅ Unique constraint for idempotent upsert (reactions)
8. ✅ Separation of concerns (each layer has responsibility)

**Benefits:**

- Maintainable: Easy to find and modify code
- Testable: Each layer can be unit tested
- Secure: Authorization in service layer
- Scalable: Database indexes, lazy loading
- Consistent: Error handling, response formats
