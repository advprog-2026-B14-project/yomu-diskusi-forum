package id.ac.ui.cs.advprog.yomuforum.functional;

import id.ac.ui.cs.advprog.yomuforum.dto.CommentRequest;
import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
class CommentApiFunctionalTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String userId;
    private String readingId;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        readingId = UUID.randomUUID().toString();
        headers = new HttpHeaders();
        headers.set("X-User-Id", userId);
        headers.set("X-User-Role", "USER");
    }

    @Test
    void testCreateAndRetrieveCommentFlow() {
        // 1. Create a new comment
        CommentRequest request = new CommentRequest();
        request.setUserId(userId);
        request.setReadingId(readingId);
        request.setContent("This is a functional test comment");

        HttpEntity<CommentRequest> createEntity = new HttpEntity<>(request, headers);
        ResponseEntity<Comment> createResponse = restTemplate.exchange(
                "/api/comments",
                HttpMethod.POST,
                createEntity,
                Comment.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getId());
        assertEquals("This is a functional test comment", createResponse.getBody().getContent());
        
        UUID commentId = createResponse.getBody().getId();

        // 2. Retrieve the comment by ID
        ResponseEntity<Comment> getResponse = restTemplate.getForEntity(
                "/api/comments/" + commentId,
                Comment.class
        );
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(commentId, getResponse.getBody().getId());

        // 3. Update the comment
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setUserId(userId);
        updateRequest.setContent("Updated content in functional test");
        
        HttpEntity<CommentRequest> updateEntity = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<Comment> updateResponse = restTemplate.exchange(
                "/api/comments/" + commentId,
                HttpMethod.PUT,
                updateEntity,
                Comment.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Updated content in functional test", updateResponse.getBody().getContent());

        // 4. Get by Reading ID
        ResponseEntity<List<Comment>> getByReadingResponse = restTemplate.exchange(
                "/api/comments/reading/" + readingId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Comment>>() { }
        );
        
        assertEquals(HttpStatus.OK, getByReadingResponse.getStatusCode());
        assertNotNull(getByReadingResponse.getBody());
        assertTrue(getByReadingResponse.getBody().stream().anyMatch(c -> c.getId().equals(commentId)));

        // 5. Delete the comment
        HttpEntity<Void> deleteEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/comments/" + commentId + "?userId=" + userId,
                HttpMethod.DELETE,
                deleteEntity,
                Void.class
        );
        
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // 6. Verify comment is deleted (should return 404)
        ResponseEntity<Comment> verifyDeleteResponse = restTemplate.getForEntity(
                "/api/comments/" + commentId,
                Comment.class
        );
        assertEquals(HttpStatus.NOT_FOUND, verifyDeleteResponse.getStatusCode());
    }
}
