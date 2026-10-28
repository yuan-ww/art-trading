package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerLoginTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @Test
    void successfulLoginStoresAnAuthenticatedSecurityContextInTheSession() {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "userService", userService);
        User requestUser = new User();
        requestUser.setEmail("user@example.test");
        requestUser.setPassword("password");
        User storedUser = new User();
        storedUser.setUserId(42);
        storedUser.setUsername("tester");
        storedUser.setEmail("user@example.test");
        storedUser.setRole("User");
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(userRepository.existsByEmail("user@example.test")).thenReturn(true);
        when(userService.authenticateUser("user@example.test", "password")).thenReturn(true);
        when(userRepository.findByEmail("user@example.test")).thenReturn(storedUser);

        var response = controller.loginUser(requestUser, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, ((Map<?, ?>) response.getBody()).get("status"));
        HttpSession session = request.getSession(false);
        assertNotNull(session);
        SecurityContext context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        assertNotNull(context);
        assertEquals("42", context.getAuthentication().getName());
    }
}
