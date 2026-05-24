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

    @Test
    void testCorsConfigurationSource_ProdProfile() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        SecurityConfig config = new SecurityConfig(env);
        
        org.springframework.web.cors.CorsConfigurationSource source = config.corsConfigurationSource();
        assertNotNull(source);
    }

    @Test
    void testCorsConfigurationSource_DevProfile() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        SecurityConfig config = new SecurityConfig(env);
        
        org.springframework.web.cors.CorsConfigurationSource source = config.corsConfigurationSource();
        assertNotNull(source);
    }
}
