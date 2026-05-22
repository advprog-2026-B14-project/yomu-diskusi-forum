package id.ac.ui.cs.advprog.yomuforum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    @Value("${app.cors.allow-credentials:false}")
    private boolean allowCredentials;

    private final Environment env;

    public SecurityConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @SuppressWarnings("java:S4502") // Safe because this is a stateless REST API using tokens, not session cookies
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        boolean isProd = isProductionProfile();
        Origins origins = resolveOrigins(allowedOrigins, isProd);

        applyOrigins(configuration, origins);

        boolean finalAllowCredentials = computeAllowCredentials(allowCredentials, origins);
        configuration.setAllowCredentials(finalAllowCredentials);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "X-User-Id",
                "X-User-Role"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private boolean isProductionProfile() {
        try {
            String[] active = env.getActiveProfiles();
            for (String p : active) {
                if (p != null && (p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    private static final class Origins {
        final List<String> exactOrigins;
        final List<String> originPatterns;

        Origins(List<String> exactOrigins, List<String> originPatterns) {
            this.exactOrigins = exactOrigins;
            this.originPatterns = originPatterns;
        }
    }

    private Origins resolveOrigins(String allowedOrigins, boolean isProd) {
        List<String> exactOrigins = new ArrayList<>();
        List<String> originPatterns = new ArrayList<>();

        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            if (isProd) {
                logger.warn("No CORS allowed origins configured in production; CORS will be disabled.");
            } else {
                originPatterns.add("http://localhost:*");
            }
            return new Origins(exactOrigins, originPatterns);
        }

        if (allowedOrigins.trim().equals("*")) {
            originPatterns.add("*");
            return new Origins(exactOrigins, originPatterns);
        }

        List<String> parsed = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        for (String origin : parsed) {
            if (origin.contains("*")) {
                originPatterns.add(origin);
            } else {
                exactOrigins.add(origin);
            }
        }

        return new Origins(exactOrigins, originPatterns);
    }

    private void applyOrigins(CorsConfiguration configuration, Origins origins) {
        if (!origins.exactOrigins.isEmpty()) {
            configuration.setAllowedOrigins(origins.exactOrigins);
        }
        if (!origins.originPatterns.isEmpty()) {
            configuration.setAllowedOriginPatterns(origins.originPatterns);
        }
    }

    private boolean computeAllowCredentials(boolean configuredAllowCredentials, Origins origins) {
        boolean hasFullWildcard = origins.originPatterns.stream().anyMatch(p -> p.equals("*"));
        if (hasFullWildcard) {
            if (configuredAllowCredentials) {
                logger.warn("Wildcard CORS origins with credentials enabled is unsafe; disabling credentials.");
            }
            return false;
        }

        return configuredAllowCredentials;
    }
}

