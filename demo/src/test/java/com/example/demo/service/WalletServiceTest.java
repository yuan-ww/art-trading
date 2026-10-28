package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.WalletTransaction;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
    @Mock private UserRepository users;
    @Mock private WalletTransactionRepository transactions;
    @InjectMocks private WalletService walletService;

    @Test
    void repeatedTopUpKeyReturnsExistingResultWithoutCreditingAgain() {
        User user = new User(); user.setUserId(7); user.setWalletBalance(new BigDecimal("30.00"));
        WalletTransaction recorded = new WalletTransaction();
        recorded.setUserId(7); recorded.setAmount(new BigDecimal("10.00")); recorded.setType(WalletTransaction.Type.TOP_UP);
        when(transactions.findByRequestKey("top-up-1")).thenReturn(Optional.of(recorded));
        when(users.findById(7)).thenReturn(Optional.of(user));

        User returned = walletService.topUp(7, new BigDecimal("10.00"), "测试充值", "top-up-1");

        assertEquals(user, returned);
        assertEquals(new BigDecimal("30.00"), user.getWalletBalance());
        verify(users, never()).findByUserIdForUpdate(any());
        verify(users, never()).save(any());
        verify(transactions, never()).save(any());
    }
}
