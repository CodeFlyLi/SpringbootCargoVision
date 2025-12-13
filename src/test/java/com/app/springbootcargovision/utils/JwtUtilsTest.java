package com.app.springbootcargovision.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils();

    @Test
    public void testGenerateAndValidateToken() {
        String username = "testUser";
        String role = "admin";
        String token = jwtUtils.generateToken(username, role);

        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.length() > 0);

        String extractedUsername = jwtUtils.extractUsername(token);
        Assertions.assertEquals(username, extractedUsername);

        String extractedRole = jwtUtils.extractRole(token);
        Assertions.assertEquals(role, extractedRole);

        Assertions.assertTrue(jwtUtils.validateToken(token, username));
        Assertions.assertFalse(jwtUtils.validateToken(token, "otherUser"));
    }
}
