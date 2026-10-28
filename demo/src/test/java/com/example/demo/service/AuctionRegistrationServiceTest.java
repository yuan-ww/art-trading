package com.example.demo.service;

import com.example.demo.model.Auction;
import com.example.demo.model.AuctionRegistration;
import com.example.demo.model.User;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.BidRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionRegistrationServiceTest {

    @Mock
    private AuctionRegistrationRepository auctionRegistrationRepository;
    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private WalletService walletService;
    @InjectMocks
    private AuctionRegistrationService auctionRegistrationService;
    @Captor
    private ArgumentCaptor<AuctionRegistration> registrationCaptor;

    @Test
    void registrationUsesConfiguredDepositRatherThanClientAmount() {
        Auction auction = activeAuction();
        auction.setDepositAmount(new BigDecimal("50.00"));
        User user = userWithBalance("80.00");
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(userRepository.findByUserIdForUpdate(2)).thenReturn(Optional.of(user));
        when(auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(2, 1)).thenReturn(false);
        when(auctionRegistrationRepository.save(any(AuctionRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        auctionRegistrationService.registerToAuction(1, 2, new BigDecimal("0.01"));

        assertEquals(new BigDecimal("30.00"), user.getWalletBalance());
        verify(auctionRegistrationRepository).save(registrationCaptor.capture());
        assertEquals(new BigDecimal("50.00"), registrationCaptor.getValue().getDepositAmount());
    }

    @Test
    void duplicateRegistrationDoesNotDeductBalanceAgain() {
        Auction auction = activeAuction();
        auction.setDepositAmount(new BigDecimal("50.00"));
        User user = userWithBalance("80.00");
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(userRepository.findByUserIdForUpdate(2)).thenReturn(Optional.of(user));
        when(auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(2, 1)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> auctionRegistrationService.registerToAuction(1, 2, new BigDecimal("50.00")));

        assertEquals(new BigDecimal("80.00"), user.getWalletBalance());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void userCannotCancelRegistrationAfterBidding() {
        Auction auction = activeAuction();
        auction.setAuctionId(1);
        User user = userWithBalance("30.00");
        user.setUserId(2);
        AuctionRegistration registration = new AuctionRegistration();
        registration.setAuction(auction);
        registration.setUser(user);
        registration.setDepositPaid(true);
        registration.setDepositAmount(new BigDecimal("50.00"));
        when(auctionRegistrationRepository.findById(3)).thenReturn(Optional.of(registration));
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(userRepository.findByUserIdForUpdate(2)).thenReturn(Optional.of(user));
        when(bidRepository.existsByAuction_AuctionIdAndUser_UserId(1, 2)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> auctionRegistrationService.cancelRegistration(3));

        assertEquals(new BigDecimal("30.00"), user.getWalletBalance());
        verify(auctionRegistrationRepository, never()).delete(any(AuctionRegistration.class));
        verify(userRepository, never()).save(any(User.class));
    }

    private Auction activeAuction() {
        Auction auction = new Auction();
        auction.setStatus(Auction.AuctionStatus.进行中);
        auction.setStartTime(LocalDateTime.now().minusMinutes(1));
        auction.setEndTime(LocalDateTime.now().plusMinutes(1));
        return auction;
    }

    private User userWithBalance(String balance) {
        User user = new User();
        user.setWalletBalance(new BigDecimal(balance));
        return user;
    }
}
