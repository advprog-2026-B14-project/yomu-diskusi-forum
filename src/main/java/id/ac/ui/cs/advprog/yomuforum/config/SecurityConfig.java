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
        List<String> exactOrigins = new ArrayList<>();
        List<String> originPatterns = new ArrayList<>();

        boolean isProd = false;
        try {
            String[] active = env.getActiveProfiles();
            for (String p : active) {
                if (p != null && (p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"))) {
                    isProd = true;
                    break;
                }
            }
        } catch (Exception e) {
            // ignore - treat as non-prod
        }

        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            if (isProd) {
                // In production, require explicit configuration. Default to no CORS allowed.
                logger.warn("No CORS allowed origins configured in production; CORS will be disabled.");
            } else {
                // development-friendly default
                originPatterns.add("http://localhost:*");
            }
        } else if (allowedOrigins.trim().equals("*")) {
            // wildcard allowed origins: explicitly disallow credentials
            configuration.setAllowedOriginPatterns(List.of("*"));
            if (allowCredentials) {
                logger.warn("Wildcard CORS origins with credentials enabled is unsafe; disabling credentials.");
            }
            configuration.setAllowCredentials(false);
        } else {
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            for (String origin : origins) {
                if (origin.contains("*")) {
                    originPatterns.add(origin);
                } else {
                    exactOrigins.add(origin);
                }
            }
        }

        if (!exactOrigins.isEmpty()) {
            configuration.setAllowedOrigins(exactOrigins);
        }

        if (!originPatterns.isEmpty()) {
            configuration.setAllowedOriginPatterns(originPatterns);
        }

        // If any allowed origin pattern is wildcard-ish, do not allow credentials even if configured.
        boolean hasWildcardPattern = originPatterns.stream().anyMatch(p -> p.equals("*") || p.contains("*"));
        if (hasWildcardPattern && allowCredentials) {
            logger.warn("Disabling credentials because allowed origin patterns contain a wildcard.");
            configuration.setAllowCredentials(false);
        } else {
            configuration.setAllowCredentials(allowCredentials);
        }

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
}
