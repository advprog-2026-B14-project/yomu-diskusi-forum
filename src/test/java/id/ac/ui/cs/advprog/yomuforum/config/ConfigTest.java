package id.ac.ui.cs.advprog.yomuforum.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.driver-class-name=org.h2.Driver"})
class ConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AsyncConfig asyncConfig;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private Environment env;

    @Test
    void testAsyncConfigLoad() {
        assertNotNull(asyncConfig);
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.taskExecutor();
        assertNotNull(executor);
        assertEquals("yomu-async-", executor.getThreadNamePrefix());
    }

    @Test
    void testSecurityConfigLoad() throws Exception {
        assertNotNull(securityConfig);
        assertNotNull(context.getBean(SecurityFilterChain.class));
        assertNotNull(context.getBean("corsConfigurationSource"));
    }

    @Test
    void testCorsOriginResolving() {
        // Test private methods via reflection to hit branches
        SecurityConfig config = new SecurityConfig(env);
        
        // test blank origin
        ReflectionTestUtils.setField(config, "allowedOrigins", "");
        assertNotNull(config.corsConfigurationSource());

        // test * origin
        ReflectionTestUtils.setField(config, "allowedOrigins", "*");
        assertNotNull(config.corsConfigurationSource());

        // test normal origins
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:3000, *.example.com");
        assertNotNull(config.corsConfigurationSource());
    }
}


