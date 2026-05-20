package id.ac.ui.cs.advprog.yomuforum.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigCorsTest {

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void blankAllowedOriginsAddsLocalhostPattern() throws Exception {
        SecurityConfig cfg = new SecurityConfig(new MockEnvironment());
        setField(cfg, "allowedOrigins", "");
        setField(cfg, "allowCredentials", true);

        CorsConfigurationSource source = cfg.corsConfigurationSource();
        CorsConfiguration c = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(c);
        List<String> patterns = c.getAllowedOriginPatterns();
        assertNotNull(patterns);
        assertTrue(patterns.contains("http://localhost:*"));

        assertEquals(Boolean.TRUE, c.getAllowCredentials());
        assertTrue(c.getAllowedMethods().contains("GET"));
        assertTrue(c.getAllowedHeaders().contains("X-User-Id"));
    }

    @Test
    void asteriskAllowedOriginsSetsPatternAndRespectsAllowCredentialsField() throws Exception {
        SecurityConfig cfg = new SecurityConfig(new MockEnvironment());
        setField(cfg, "allowedOrigins", "*");
        setField(cfg, "allowCredentials", false);

        CorsConfigurationSource source = cfg.corsConfigurationSource();
        CorsConfiguration c = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(c);
        List<String> patterns = c.getAllowedOriginPatterns();
        assertNotNull(patterns);
        assertTrue(patterns.contains("*"));

        assertEquals(Boolean.FALSE, c.getAllowCredentials());
    }

    @Test
    void commaSeparatedOriginsAreSplitIntoExactAndPatternLists() throws Exception {
        SecurityConfig cfg = new SecurityConfig(new MockEnvironment());
        setField(cfg, "allowedOrigins", "https://example.com, https://*.example.org, , http://localhost:3000");
        setField(cfg, "allowCredentials", true);

        CorsConfigurationSource source = cfg.corsConfigurationSource();
        CorsConfiguration c = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(c);
        List<String> exact = c.getAllowedOrigins();
        List<String> patterns = c.getAllowedOriginPatterns();

        assertNotNull(exact);
        assertTrue(exact.contains("https://example.com"));
        assertTrue(exact.contains("http://localhost:3000"));

        assertNotNull(patterns);
        assertTrue(patterns.contains("https://*.example.org"));
        assertEquals(Boolean.TRUE, c.getAllowCredentials());
    }
}
