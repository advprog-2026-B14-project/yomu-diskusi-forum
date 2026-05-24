package id.ac.ui.cs.advprog.yomuforum.config;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncConfigTest {

    @Test
    void testTaskExecutor() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.taskExecutor();
        
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        
        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) executor;
        
        // Assert some configurations
        assertEquals(2, threadPool.getCorePoolSize());
        assertEquals(4, threadPool.getMaxPoolSize());
        assertEquals("yomu-async-", threadPool.getThreadNamePrefix());
    }
}
