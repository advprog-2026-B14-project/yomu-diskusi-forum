package id.ac.ui.cs.advprog.yomuforum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

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

        // If no explicit origins configured, default to localhost for development only.
        // Prefer explicit configuration via system properties or application properties
        // to avoid accidentally enabling wide open CORS with credentials.
        String allowedOriginsProperty = System.getProperty("cors.allowed-origins", "");
        // Default to false for safety. Explicitly set to true only for trusted origins.
        boolean allowCredentialsProperty = Boolean.parseBoolean(System.getProperty("cors.allow-credentials", "false"));

        if (allowedOriginsProperty == null || allowedOriginsProperty.isBlank()) {
            // Development-friendly default: only allow localhost and permit credentials for local testing.
            configuration.setAllowedOrigins(List.of("http://localhost:3000"));
            configuration.setAllowCredentials(true);
        } else if (allowedOriginsProperty.trim().equals("*")) {
            // Wildcard origins are dangerous when credentials are allowed — explicitly disallow credentials.
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowCredentials(false);
        } else {
            List<String> origins = Arrays.stream(allowedOriginsProperty.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            configuration.setAllowedOrigins(origins);
            configuration.setAllowCredentials(allowCredentialsProperty);
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Be explicit about allowed headers instead of allowing all. Add more if needed.
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}