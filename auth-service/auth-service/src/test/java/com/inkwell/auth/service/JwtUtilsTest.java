package com.inkwell.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {

    @Test
    void generatedTokenCanBeReadAndValidated() {
        JwtUtils jwtUtils = new JwtUtils();

        String token = jwtUtils.generateToken("author@inkwell.com", "AUTHOR");

        assertTrue(jwtUtils.validateToken(token));
        assertEquals("author@inkwell.com", jwtUtils.extractEmail(token));
        assertEquals("AUTHOR", jwtUtils.extractRole(token));
    }

    @Test
    void validateTokenReturnsFalseForMalformedToken() {
        JwtUtils jwtUtils = new JwtUtils();

        assertFalse(jwtUtils.validateToken("not-a-token"));
    }
}
