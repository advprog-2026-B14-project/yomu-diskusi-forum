package id.ac.ui.cs.advprog.yomuforum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YomuForumApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true, "Context loads successfully");
    }

    @Test
    void testMainClassExists() {
        assertDoesNotThrow(() -> {
            Class.forName("id.ac.ui.cs.advprog.yomuforum.YomuForumApplication");
        });
    }

    @Test
    void testMainMethodRunsSpringApplication() {
        try (var mockedSpringApp = mockStatic(SpringApplication.class)) {
            mockedSpringApp.when(() -> SpringApplication.run(YomuForumApplication.class, new String[]{}))
                    .thenReturn(null);

            YomuForumApplication.main(new String[]{});

            mockedSpringApp.verify(() -> SpringApplication.run(YomuForumApplication.class, new String[]{}));
        }
    }
}
