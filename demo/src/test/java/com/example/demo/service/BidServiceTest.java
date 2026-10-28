package com.example.demo.service;

import com.example.demo.model.Auction;
import com.example.demo.model.Bid;
import com.example.demo.model.Artwork;
import com.example.demo.model.User;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.BidRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuctionRegistrationRepository auctionRegistrationRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private BidService bidService;

    @Test
    void registeredUserCanBidOnlyAboveStartingPrice() {
        Auction auction = activeAuction();
        User user = new User();
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2)).thenReturn(Optional.of(user));
        when(auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(2, 1)).thenReturn(true);
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bid bid = bidService.placeBid(1, 2, new BigDecimal("120.00"));

        assertEquals(new BigDecimal("120.00"), bid.getBidAmount());
        assertEquals(new BigDecimal("120.00"), auction.getCurrentHighestBid());
    }

    @Test
    void unregisteredUserCannotBid() {
        Auction auction = activeAuction();
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2)).thenReturn(Optional.of(new User()));
        when(auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(2, 1)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> bidService.placeBid(1, 2, new BigDecimal("120.00")));
    }

    @Test
    void ownerCannotBidOnOwnArtwork() {
        Auction auction = activeAuction();
        Artwork artwork = new Artwork();
        artwork.setUserId(2);
        auction.setArtwork(artwork);
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2)).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class,
                () -> bidService.placeBid(1, 2, new BigDecimal("120.00")));
    }

    @Test
    void deletingHighestBidRestoresThePreviousHighestBidder() {
        Auction auction = activeAuction();
        auction.setAuctionId(1);
        User highestBidder = new User();
        highestBidder.setUserId(2);
        User previousBidder = new User();
        previousBidder.setUserId(3);
        Bid highestBid = bid(10, auction, highestBidder, "150.00");
        Bid previousBid = bid(9, auction, previousBidder, "120.00");
        auction.setCurrentHighestBid(new BigDecimal("150.00"));
        auction.setHighestBidder(highestBidder);

        when(bidRepository.findById(10)).thenReturn(Optional.of(highestBid));
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));
        when(bidRepository.findByAuction_auctionId(1)).thenReturn(List.of(highestBid, previousBid));

        bidService.deleteBid(10);

        assertEquals(new BigDecimal("120.00"), auction.getCurrentHighestBid());
        assertEquals(previousBidder, auction.getHighestBidder());
        verify(bidRepository).delete(highestBid);
        verify(auctionRepository).save(auction);
    }

    @Test
    void cannotDeleteBidAfterAuctionHasEnded() {
        Auction auction = activeAuction();
        auction.setAuctionId(1);
        auction.setStatus(Auction.AuctionStatus.已结束);
        Bid bid = bid(10, auction, new User(), "150.00");
        when(bidRepository.findById(10)).thenReturn(Optional.of(bid));
        when(auctionRepository.findByAuctionIdForUpdate(1)).thenReturn(Optional.of(auction));

        assertThrows(IllegalStateException.class, () -> bidService.deleteBid(10));
    }

    private Bid bid(Integer id, Auction auction, User user, String amount) {
        Bid bid = new Bid();
        bid.setBidId(id);
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(new BigDecimal(amount));
        return bid;
    }

    private Auction activeAuction() {
        Auction auction = new Auction();
        auction.setStatus(Auction.AuctionStatus.进行中);
        auction.setStartingPrice(new BigDecimal("100.00"));
        auction.setCurrentHighestBid(BigDecimal.ZERO);
        auction.setStartTime(LocalDateTime.now().minusMinutes(1));
        auction.setEndTime(LocalDateTime.now().plusMinutes(1));
        return auction;
    }
}
