package id.ac.ui.cs.advprog.yomuforum.service;

import id.ac.ui.cs.advprog.yomuforum.model.Comment;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComponent;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentComposite;
import id.ac.ui.cs.advprog.yomuforum.dto.composite.CommentLeaf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import id.ac.ui.cs.advprog.yomuforum.repository.ReactionRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentTreeBuilderTest {

    @Mock
    private ReactionRepository reactionRepository;

    private CommentTreeBuilder treeBuilder;
    private UUID readingId;

    @BeforeEach
    void setUp() {
        treeBuilder = new CommentTreeBuilder(reactionRepository);
        readingId = UUID.randomUUID();
    }

    private Comment createComment(UUID id, UUID parentId, String content) {
        Comment c = new Comment();
        c.setId(id);
        c.setUserId(UUID.randomUUID());
        c.setReadingId(readingId);
        c.setParentCommentId(parentId);
        c.setContent(content);
        c.setCreatedAt(new Date());
        c.setUpdatedAt(new Date());
        return c;
    }

    @Test
    void testBuildTreeWithNullList() {
        List<CommentComponent> result = treeBuilder.buildTree(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testBuildTreeWithEmptyList() {
        List<CommentComponent> result = treeBuilder.buildTree(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testBuildTreeWithSingleRootComment() {
        UUID rootId = UUID.randomUUID();
        Comment root = createComment(rootId, null, "Root comment");

        List<CommentComponent> result = treeBuilder.buildTree(List.of(root));

        assertEquals(1, result.size());
        assertTrue(result.get(0).isLeaf());
        assertEquals("Root comment", result.get(0).getContent());
        assertEquals(rootId, result.get(0).getId());
    }

    @Test
    void testBuildTreeWithParentAndChild() {
        UUID rootId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Comment root = createComment(rootId, null, "Root");
        Comment child = createComment(childId, rootId, "Child");

        List<CommentComponent> result = treeBuilder.buildTree(List.of(root, child));

        assertEquals(1, result.size());
        assertFalse(result.get(0).isLeaf());
        assertEquals("Root", result.get(0).getContent());
        assertEquals(1, result.get(0).getChildren().size());
        assertTrue(result.get(0).getChildren().get(0).isLeaf());
        assertEquals("Child", result.get(0).getChildren().get(0).getContent());
    }

    @Test
    void testBuildTreeWithMultipleRoots() {
        UUID root1Id = UUID.randomUUID();
        UUID root2Id = UUID.randomUUID();

        Comment root1 = createComment(root1Id, null, "Root 1");
        Comment root2 = createComment(root2Id, null, "Root 2");

        List<CommentComponent> result = treeBuilder.buildTree(List.of(root1, root2));

        assertEquals(2, result.size());
        assertTrue(result.get(0).isLeaf());
        assertTrue(result.get(1).isLeaf());
    }

    @Test
    void testBuildTreeWithDeepNesting() {
        UUID rootId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        UUID grandchildId = UUID.randomUUID();

        Comment root = createComment(rootId, null, "Root");
        Comment child = createComment(childId, rootId, "Child");
        Comment grandchild = createComment(grandchildId, childId, "Grandchild");

        List<CommentComponent> result = treeBuilder.buildTree(List.of(root, child, grandchild));

        assertEquals(1, result.size());
        assertFalse(result.get(0).isLeaf()); // Root has children

        CommentComponent childNode = result.get(0).getChildren().get(0);
        assertFalse(childNode.isLeaf()); // Child has grandchild

        CommentComponent grandchildNode = childNode.getChildren().get(0);
        assertTrue(grandchildNode.isLeaf()); // Grandchild is leaf
        assertEquals("Grandchild", grandchildNode.getContent());
    }

    @Test
    void testBuildTreeWithMultipleChildrenOnSameParent() {
        UUID rootId = UUID.randomUUID();
        UUID child1Id = UUID.randomUUID();
        UUID child2Id = UUID.randomUUID();
        UUID child3Id = UUID.randomUUID();

        Comment root = createComment(rootId, null, "Root");
        Comment child1 = createComment(child1Id, rootId, "Child 1");
        Comment child2 = createComment(child2Id, rootId, "Child 2");
        Comment child3 = createComment(child3Id, rootId, "Child 3");

        List<CommentComponent> result = treeBuilder.buildTree(List.of(root, child1, child2, child3));

        assertEquals(1, result.size());
        assertFalse(result.get(0).isLeaf());
        assertEquals(3, result.get(0).getChildren().size());
    }

    @Test
    void testBuildTreeWithOrphanComment() {
        // A comment whose parent isn't in the list → treated as root
        UUID orphanId = UUID.randomUUID();
        UUID missingParentId = UUID.randomUUID();

        Comment orphan = createComment(orphanId, missingParentId, "Orphan");

        List<CommentComponent> result = treeBuilder.buildTree(List.of(orphan));

        assertEquals(1, result.size());
        assertTrue(result.get(0).isLeaf());
        assertEquals("Orphan", result.get(0).getContent());
    }

    @Test
    void testBuildTreePreservesCommentData() {
        UUID rootId = UUID.randomUUID();
        Comment root = createComment(rootId, null, "Test content");

        List<CommentComponent> result = treeBuilder.buildTree(List.of(root));
        CommentComponent node = result.get(0);

        assertEquals(rootId, node.getId());
        assertEquals(root.getUserId(), node.getUserId());
        assertEquals(readingId, node.getReadingId());
        assertNull(node.getParentCommentId());
        assertEquals("Test content", node.getContent());
        assertNotNull(node.getCreatedAt());
        assertNotNull(node.getUpdatedAt());
    }

    @Test
    void testConvertToLeafIfNeededKeepsLeafChildren() {
        UUID rootId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Comment root = createComment(rootId, null, "Root");
        Comment child = createComment(childId, rootId, "Child");

        CommentComposite rootComposite = new CommentComposite(root);
        rootComposite.addChild(new CommentLeaf(child));

        CommentComponent result = (CommentComponent) ReflectionTestUtils.invokeMethod(
                treeBuilder,
                "convertToLeafIfNeeded",
                rootComposite,
                Set.of(rootId)
        );

        assertNotNull(result);
        assertFalse(result.isLeaf());
        assertEquals(1, result.getChildren().size());
        assertTrue(result.getChildren().get(0).isLeaf());
        assertEquals("Child", result.getChildren().get(0).getContent());
    }
}
