package com.example.orderflow.firebase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Local fallback used when Firebase is disabled. Treats the supplied token as a
 * raw identity so the auth flow can be exercised without Firebase credentials.
 * Token format: "uid:email" (email optional).
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "false", matchIfMissing = true)
public class NoopFirebaseTokenVerifier implements FirebaseTokenVerifier {

    @Override
    public FirebaseUser verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Firebase ID token is required");
        }
        String[] parts = idToken.split(":", 2);
        String uid = parts[0];
        String email = parts.length > 1 ? parts[1] : uid + "@firebase.local";
        log.info("[noop-firebase] accepted local identity uid={}", uid);
        return new FirebaseUser(uid, email);
    }
}
