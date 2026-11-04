package com.rapidm3.employeedirectory.config;

import com.rapidm3.employeedirectory.security.FirebaseAuthFilter;
import com.rapidm3.employeedirectory.security.TokenVerifier;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Filter order matters: CORS runs first so that preflights are answered and 401 responses
 * still carry CORS headers (otherwise the browser reports a CORS error instead of 401).
 */
@Configuration
public class WebConfig {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig(@Value("${app.auth.enabled:true}") boolean authEnabled) {
        if (!authEnabled) {
            log.warn("app.auth.enabled=false: the API is NOT protected. Local development only; never demo or share.");
        }
    }

    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin)); // exact Vite dev origin only, never "*"
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true", matchIfMissing = true)
    FilterRegistrationBean<FirebaseAuthFilter> firebaseAuthFilter(TokenVerifier verifier, ObjectMapper objectMapper) {
        FilterRegistrationBean<FirebaseAuthFilter> registration =
                new FilterRegistrationBean<>(new FirebaseAuthFilter(verifier, objectMapper));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
