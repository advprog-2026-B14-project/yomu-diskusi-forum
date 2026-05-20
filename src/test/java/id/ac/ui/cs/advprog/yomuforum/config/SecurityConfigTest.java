package id.ac.ui.cs.advprog.yomuforum.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void securityConfigCanBeInstantiated() {
        SecurityConfig config = new SecurityConfig(new MockEnvironment());
        assertNotNull(config);
    }
}
