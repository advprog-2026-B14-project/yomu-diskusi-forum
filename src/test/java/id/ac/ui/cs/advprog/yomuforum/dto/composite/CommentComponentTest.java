package id.ac.ui.cs.advprog.yomuforum.dto.composite;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommentComponentTest {

    private Comment comment;
    private Comment childComment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setUserId(UUID.randomUUID());
        comment.setReadingId(UUID.randomUUID());
        comment.setContent("Root comment");
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());

        childComment = new Comment();
        childComment.setId(UUID.randomUUID());
        childComment.setUserId(UUID.randomUUID());
        childComment.setReadingId(comment.getReadingId());
        childComment.setParentCommentId(comment.getId());
        childComment.setContent("Child comment");
        childComment.setCreatedAt(new Date());
        childComment.setUpdatedAt(new Date());
    }

    // ─── CommentLeaf tests ────────────────────────────────────────

    @Test
    void testLeafIsLeaf() {
        CommentLeaf leaf = new CommentLeaf(comment);
        assertTrue(leaf.isLeaf());
    }

    @Test
    void testLeafGetters() {
        CommentLeaf leaf = new CommentLeaf(comment);
        assertEquals(comment.getId(), leaf.getId());
        assertEquals(comment.getUserId(), leaf.getUserId());
        assertEquals(comment.getReadingId(), leaf.getReadingId());
        assertEquals(comment.getParentCommentId(), leaf.getParentCommentId());
        assertEquals(comment.getContent(), leaf.getContent());
        assertEquals(comment.getCreatedAt(), leaf.getCreatedAt());
        assertEquals(comment.getUpdatedAt(), leaf.getUpdatedAt());
    }

    @Test
    void testLeafGetChildrenReturnsEmptyList() {
        CommentLeaf leaf = new CommentLeaf(comment);
        assertNotNull(leaf.getChildren());
        assertTrue(leaf.getChildren().isEmpty());
    }

    @Test
    void testLeafAddChildThrowsException() {
        CommentLeaf leaf = new CommentLeaf(comment);
        CommentLeaf child = new CommentLeaf(childComment);

        assertThrows(UnsupportedOperationException.class, () -> leaf.addChild(child));
    }

    @Test
    void testLeafRemoveChildThrowsException() {
        CommentLeaf leaf = new CommentLeaf(comment);
        CommentLeaf child = new CommentLeaf(childComment);

        assertThrows(UnsupportedOperationException.class, () -> leaf.removeChild(child));
    }

    // ─── CommentComposite tests ───────────────────────────────────

    @Test
    void testCompositeIsNotLeaf() {
        CommentComposite composite = new CommentComposite(comment);
        assertFalse(composite.isLeaf());
    }

    @Test
    void testCompositeGetters() {
        CommentComposite composite = new CommentComposite(comment);
        assertEquals(comment.getId(), composite.getId());
        assertEquals(comment.getUserId(), composite.getUserId());
        assertEquals(comment.getReadingId(), composite.getReadingId());
        assertEquals(comment.getParentCommentId(), composite.getParentCommentId());
        assertEquals(comment.getContent(), composite.getContent());
        assertEquals(comment.getCreatedAt(), composite.getCreatedAt());
        assertEquals(comment.getUpdatedAt(), composite.getUpdatedAt());
    }

    @Test
    void testCompositeAddChild() {
        CommentComposite composite = new CommentComposite(comment);
        CommentLeaf child = new CommentLeaf(childComment);

        composite.addChild(child);

        assertEquals(1, composite.getChildren().size());
        assertEquals(child, composite.getChildren().get(0));
    }

    @Test
    void testCompositeRemoveChild() {
        CommentComposite composite = new CommentComposite(comment);
        CommentLeaf child = new CommentLeaf(childComment);

        composite.addChild(child);
        assertEquals(1, composite.getChildren().size());

        composite.removeChild(child);
        assertTrue(composite.getChildren().isEmpty());
    }

    @Test
    void testCompositeGetChildrenInitiallyEmpty() {
        CommentComposite composite = new CommentComposite(comment);
        assertNotNull(composite.getChildren());
        assertTrue(composite.getChildren().isEmpty());
    }

    // ─── Tree structure tests ─────────────────────────────────────

    @Test
    void testNestedTree() {
        CommentComposite root = new CommentComposite(comment);
        CommentComposite mid = new CommentComposite(childComment);

        Comment grandchildComment = new Comment();
        grandchildComment.setId(UUID.randomUUID());
        grandchildComment.setUserId(UUID.randomUUID());
        grandchildComment.setReadingId(comment.getReadingId());
        grandchildComment.setParentCommentId(childComment.getId());
        grandchildComment.setContent("Grandchild");
        grandchildComment.setCreatedAt(new Date());
        grandchildComment.setUpdatedAt(new Date());

        CommentLeaf grandchild = new CommentLeaf(grandchildComment);

        root.addChild(mid);
        mid.addChild(grandchild);

        assertEquals(1, root.getChildren().size());
        assertFalse(root.isLeaf());

        CommentComponent midNode = root.getChildren().get(0);
        assertFalse(midNode.isLeaf());
        assertEquals(1, midNode.getChildren().size());

        CommentComponent grandchildNode = midNode.getChildren().get(0);
        assertTrue(grandchildNode.isLeaf());
        assertEquals("Grandchild", grandchildNode.getContent());
    }

    @Test
    void testUniformTreatment() {
        // Both leaf and composite can be treated as CommentComponent
        CommentComponent leaf = new CommentLeaf(comment);
        CommentComponent composite = new CommentComposite(childComment);

        // Both have same interface methods
        assertNotNull(leaf.getId());
        assertNotNull(composite.getId());
        assertNotNull(leaf.getContent());
        assertNotNull(composite.getContent());
        assertNotNull(leaf.getChildren());
        assertNotNull(composite.getChildren());
    }
}
