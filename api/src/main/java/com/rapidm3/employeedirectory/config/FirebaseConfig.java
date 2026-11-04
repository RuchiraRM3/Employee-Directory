package com.rapidm3.employeedirectory.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.rapidm3.employeedirectory.security.FirebaseTokenVerifier;
import com.rapidm3.employeedirectory.security.TokenVerifier;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initialises the Firebase Admin SDK from Application Default Credentials, i.e. the
 * service-account JSON pointed to by GOOGLE_APPLICATION_CREDENTIALS (kept outside the repo).
 */
@Configuration
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfig {

    @Bean
    TokenVerifier tokenVerifier() throws IOException {
        FirebaseApp app = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build())
                : FirebaseApp.getInstance();
        return new FirebaseTokenVerifier(FirebaseAuth.getInstance(app));
    }
}
