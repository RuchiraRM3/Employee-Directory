package com.rapidm3.employeedirectory.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class FirebaseTokenVerifier implements TokenVerifier {

    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenVerifier(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public String verify(String idToken) {
        try {
            // Checks signature, expiry, audience (project) and issuer.
            return firebaseAuth.verifyIdToken(idToken).getUid();
        } catch (FirebaseAuthException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid Firebase ID token", e);
        }
    }
}
