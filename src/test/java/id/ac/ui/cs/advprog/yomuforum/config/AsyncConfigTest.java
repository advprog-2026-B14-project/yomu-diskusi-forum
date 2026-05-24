package id.ac.ui.cs.advprog.yomuforum.config;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncConfigTest {

    @Test
    void testTaskExecutor() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.taskExecutor();
        
        assertNotNull(executor);
        assertTrue(executor instanceof org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor);
        
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor threadPool = (org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) executor;
        
        // Assert some configurations
        org.junit.jupiter.api.Assertions.assertEquals(2, threadPool.getCorePoolSize());
        org.junit.jupiter.api.Assertions.assertEquals(4, threadPool.getMaxPoolSize());
        org.junit.jupiter.api.Assertions.assertEquals("yomu-async-", threadPool.getThreadNamePrefix());
    }
}
