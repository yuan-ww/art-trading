package com.example.demo.service;

import com.example.demo.model.Artwork;
import com.example.demo.model.Auction;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.BidRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private ArtworksRepository artworkRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuctionRegistrationRepository auctionRegistrationRepository;
    @Mock
    private BidRepository bidRepository;
    @InjectMocks
    private AuctionService auctionService;

    @Test
    void createAuctionAlwaysStartsInAudit() {
        Artwork artwork = new Artwork();
        artwork.setArtworkId(8);
        Auction request = validAuction();
        request.setStatus(Auction.AuctionStatus.进行中);

        when(artworkRepository.findById(8)).thenReturn(Optional.of(artwork));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Auction created = auctionService.createAuction(request);

        assertEquals(Auction.AuctionStatus.审核中, created.getStatus());
        assertEquals(Artwork.Status.审核, artwork.getStatus());
        verify(artworkRepository).save(artwork);
    }

    @Test
    void updateAuctionRejectsForgedFinalState() {
        Auction existing = validAuction();
        existing.setAuctionId(3);
        existing.setStatus(Auction.AuctionStatus.审核中);
        Auction request = validAuction();
        request.setStatus(Auction.AuctionStatus.已结束);
        when(auctionRepository.findById(3)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> auctionService.updateAuction(3, request));
    }

    @Test
    void cannotResubmitAnAuctionWithPriorBids() {
        Auction existing = validAuction();
        existing.setStatus(Auction.AuctionStatus.已取消);
        existing.setCurrentHighestBid(new BigDecimal("120.00"));
        when(auctionRepository.findByAuctionIdForUpdate(3)).thenReturn(Optional.of(existing));
        assertThrows(IllegalStateException.class, () -> auctionService.resubmitAuction(3, validAuction()));
    }

    @Test
    void cannotResubmitAuctionWithExpiredEndTime() {
        Auction existing = validAuction();
        existing.setStatus(Auction.AuctionStatus.已取消);
        Auction request = validAuction();
        request.setStartTime(LocalDateTime.now().minusDays(2));
        request.setEndTime(LocalDateTime.now().minusDays(1));
        when(auctionRepository.findByAuctionIdForUpdate(3)).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class, () -> auctionService.resubmitAuction(3, request));
    }

    @Test
    void cannotResubmitAuctionWithRegistrationsEvenWithoutHighestBid() {
        Auction existing = validAuction();
        existing.setStatus(Auction.AuctionStatus.已取消);
        when(auctionRepository.findByAuctionIdForUpdate(3)).thenReturn(Optional.of(existing));
        when(auctionRegistrationRepository.countByAuction_AuctionId(3)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> auctionService.resubmitAuction(3, validAuction()));
    }

    @Test
    void auditApprovalViaUpdateMakesArtworkAvailable() {
        Auction existing = validAuction();
        existing.setAuctionId(3);
        existing.setStatus(Auction.AuctionStatus.审核中);
        Auction request = validAuction();
        request.setStatus(Auction.AuctionStatus.进行中);
        Artwork artwork = new Artwork();
        artwork.setArtworkId(8);
        when(auctionRepository.findById(3)).thenReturn(Optional.of(existing));
        when(artworkRepository.findById(8)).thenReturn(Optional.of(artwork));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Auction updated = auctionService.updateAuction(3, request);

        assertEquals(Auction.AuctionStatus.进行中, updated.getStatus());
        assertEquals(Artwork.Status.在售, artwork.getStatus());
        verify(artworkRepository).save(artwork);
    }

    private Auction validAuction() {
        Auction auction = new Auction();
        auction.setArtworkId(8);
        auction.setStartingPrice(new BigDecimal("100.00"));
        auction.setDepositAmount(new BigDecimal("10.00"));
        auction.setDepositPercentage(new BigDecimal("10.00"));
        auction.setStartTime(LocalDateTime.of(2026, 10, 28, 10, 0));
        auction.setEndTime(LocalDateTime.of(2026, 10, 29, 10, 0));
        return auction;
    }
}
