package com.example.orderflow.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Real Firebase Authentication integration, activated only when
 * {@code firebase.enabled=true} with a service-account credentials file.
 * When disabled, {@link NoopFirebaseTokenVerifier} is used instead.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseTokenVerifierImpl implements FirebaseTokenVerifier {

    @Value("${firebase.credentials:}")
    private String credentialsPath;

    @PostConstruct
    void init() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccount = new FileInputStream(credentialsPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("[firebase] initialized from {}", credentialsPath);
            }
        }
    }

    @Override
    public FirebaseUser verify(String idToken) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return new FirebaseUser(token.getUid(), token.getEmail());
        } catch (FirebaseAuthException e) {
            throw new IllegalArgumentException("Invalid Firebase ID token: " + e.getMessage(), e);
        }
    }
}
