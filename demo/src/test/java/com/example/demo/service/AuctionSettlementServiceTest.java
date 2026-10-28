package com.example.demo.service;

import com.example.demo.model.Artwork;
import com.example.demo.model.Auction;
import com.example.demo.model.AuctionRegistration;
import com.example.demo.model.Order;
import com.example.demo.model.User;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.AuctionRegistrationRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionSettlementServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private ArtworksRepository artworksRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private AuctionRegistrationRepository auctionRegistrationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private AuctionSettlementService auctionSettlementService;
    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Test
    void doesNotSettleAnAuctionBeforeItsEndTime() {
        Auction auction = auctionWithEndTime(LocalDateTime.now().plusMinutes(5));
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));

        AuctionSettlementService.SettlementResult result = auctionSettlementService.settleAuction(1);

        assertEquals(AuctionSettlementService.SettlementResult.NOT_ENDED, result);
        verify(auctionRepository, never()).save(any());
        verify(orderService, never()).createOrder(any());
    }

    @Test
    void marksACompletedAuctionWithoutBidsAsUnsold() {
        Auction auction = auctionWithEndTime(LocalDateTime.now().minusMinutes(5));
        auction.setCurrentHighestBid(BigDecimal.ZERO);
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(auctionRegistrationRepository.findByAuction_AuctionId(1)).thenReturn(List.of());

        AuctionSettlementService.SettlementResult result = auctionSettlementService.settleAuction(1);

        assertEquals(AuctionSettlementService.SettlementResult.UNSOLD, result);
        assertEquals(Auction.AuctionStatus.流拍, auction.getStatus());
        verify(auctionRepository).save(auction);
        verify(orderService, never()).createOrder(any());
    }

    @Test
    void createsAnOrderForTheHighestBidderAfterAuctionEnds() {
        Auction auction = auctionWithEndTime(LocalDateTime.now().minusMinutes(5));
        auction.setCurrentHighestBid(new BigDecimal("120.00"));
        User bidder = new User();
        bidder.setUserId(10);
        auction.setHighestBidder(bidder);
        Artwork artwork = new Artwork();
        artwork.setArtworkId(20);
        artwork.setUserId(30);
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(artworksRepository.findById(20)).thenReturn(Optional.of(artwork));
        when(auctionRegistrationRepository.findByAuction_AuctionId(1)).thenReturn(List.of());

        AuctionSettlementService.SettlementResult result = auctionSettlementService.settleAuction(1);

        assertEquals(AuctionSettlementService.SettlementResult.SETTLED, result);
        assertEquals(Auction.AuctionStatus.已结束, auction.getStatus());
        verify(orderService).createOrder(orderCaptor.capture());
        Order order = orderCaptor.getValue();
        assertEquals(20, order.getArtworkId());
        assertEquals(10, order.getBuyerId());
        assertEquals(30, order.getSellerId());
        assertEquals(new BigDecimal("120.00"), order.getFinalPrice());
        verify(auctionRepository).save(auction);
    }

    @Test
    void settlementRefundsAllDepositsAndClearsRegistrations() {
        Auction auction = auctionWithEndTime(LocalDateTime.now().minusMinutes(5));
        auction.setCurrentHighestBid(new BigDecimal("120.00"));
        User winner = userWithIdAndBalance(10, "50.00");
        User otherBidder = userWithIdAndBalance(11, "20.00");
        auction.setHighestBidder(winner);
        Artwork artwork = new Artwork();
        artwork.setArtworkId(20);
        artwork.setUserId(30);
        AuctionRegistration winnerRegistration = registrationFor(winner, "10.00");
        AuctionRegistration otherRegistration = registrationFor(otherBidder, "15.00");

        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(artworksRepository.findById(20)).thenReturn(Optional.of(artwork));
        when(auctionRegistrationRepository.findByAuction_AuctionId(1))
                .thenReturn(List.of(winnerRegistration, otherRegistration));
        when(userRepository.findByUserIdForUpdate(10)).thenReturn(Optional.of(winner));
        when(userRepository.findByUserIdForUpdate(11)).thenReturn(Optional.of(otherBidder));

        AuctionSettlementService.SettlementResult result = auctionSettlementService.settleAuction(1);

        assertEquals(AuctionSettlementService.SettlementResult.SETTLED, result);
        assertEquals(new BigDecimal("60.00"), winner.getWalletBalance());
        assertEquals(new BigDecimal("35.00"), otherBidder.getWalletBalance());
        verify(userRepository).save(winner);
        verify(userRepository).save(otherBidder);
        verify(auctionRegistrationRepository).delete(winnerRegistration);
        verify(auctionRegistrationRepository).delete(otherRegistration);
    }

    @Test
    void orderInitializesRequiredTimestamps() {
        Order order = new Order();

        order.initializeTimestamps();

        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    private Auction auctionWithEndTime(LocalDateTime endTime) {
        Auction auction = new Auction();
        auction.setAuctionId(1);
        auction.setArtworkId(20);
        auction.setStatus(Auction.AuctionStatus.进行中);
        auction.setEndTime(endTime);
        return auction;
    }

    private User userWithIdAndBalance(Integer userId, String balance) {
        User user = new User();
        user.setUserId(userId);
        user.setWalletBalance(new BigDecimal(balance));
        return user;
    }

    private AuctionRegistration registrationFor(User user, String deposit) {
        AuctionRegistration registration = new AuctionRegistration();
        registration.setUser(user);
        registration.setDepositPaid(true);
        registration.setDepositAmount(new BigDecimal(deposit));
        return registration;
    }
}
