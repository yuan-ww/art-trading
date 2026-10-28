package com.example.demo.config;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
class DisabledUserFilterTest {
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }
    @Test void disabledAccountCannotReuseAnExistingSession() throws Exception {
        UserRepository users = mock(UserRepository.class);
        User user = new User(); user.setId(5); user.setDisabled(true);
        when(users.findById(5)).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(5, null, List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        new DisabledUserFilter(users).doFilter(request, response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }
}
