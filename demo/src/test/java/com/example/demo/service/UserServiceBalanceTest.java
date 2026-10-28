package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceBalanceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void rechargeUpdatesTheLockedUserBalance() {
        User user = new User();
        user.setUserId(5);
        user.setWalletBalance(new BigDecimal("10.00"));
        when(userRepository.findByUserIdForUpdate(5)).thenReturn(Optional.of(user));

        boolean recharged = userService.rechargeUserAccount(5, 5.5);

        assertTrue(recharged);
        assertEquals(new BigDecimal("15.50"), user.getWalletBalance());
        verify(userRepository).save(user);
    }
}
