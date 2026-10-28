package com.example.demo.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessControlServiceTest {

    private final AccessControlService accessControlService = new AccessControlService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userCanAccessOnlyTheirOwnResource() {
        authenticateAs(10, "ROLE_User");

        assertDoesNotThrow(() -> accessControlService.requireSelfOrAdmin(10));
        assertThrows(ResponseStatusException.class, () -> accessControlService.requireSelfOrAdmin(11));
    }

    @Test
    void administratorCanAccessAnyUserResourceAndAdminOperations() {
        authenticateAs(10, "ROLE_Admin");

        assertDoesNotThrow(() -> accessControlService.requireSelfOrAdmin(11));
        assertDoesNotThrow(accessControlService::requireAdmin);
    }

    @Test
    void participantCanAccessOnlyAnOrderTheyTakePartIn() {
        authenticateAs(10, "ROLE_User");

        assertDoesNotThrow(() -> accessControlService.requireParticipantOrAdmin(11, 10));
        assertThrows(ResponseStatusException.class,
                () -> accessControlService.requireParticipantOrAdmin(11, 12));
    }

    private void authenticateAs(Integer userId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                )
        );
    }
}
