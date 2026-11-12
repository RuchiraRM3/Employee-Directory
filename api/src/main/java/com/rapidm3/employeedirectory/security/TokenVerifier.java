package com.rapidm3.employeedirectory.security;

/** Verifies a bearer ID token and returns the user's UID. Abstracted so tests can stub Firebase. */
public interface TokenVerifier {

    /** @throws InvalidTokenException if the token is missing, malformed, expired or revoked. */
    String verify(String idToken);
}
