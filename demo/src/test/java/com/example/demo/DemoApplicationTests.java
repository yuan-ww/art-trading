package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.OrderRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.example.demo.model.Artwork;
import java.util.List;

@SpringBootTest
class DemoApplicationTests {
    @Autowired private ArtworksRepository artworksRepository;
    @Autowired private OrderRepository orderRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void adminOrderProjectionRunsAgainstConfiguredDatabase() {
        assertNotNull(orderRepository.findAdminPage(null, null, null, PageRequest.of(0, 5)));
        assertNotNull(orderRepository.findParticipantPage(1, null, null, null, null, PageRequest.of(0, 5)));
    }

    @Test
    void publicArtworkProjectionRunsAgainstConfiguredDatabase() {
        assertNotNull(artworksRepository.findPublicList(null, PageRequest.of(0, 5)));
        assertNotNull(artworksRepository.findPublicFiltered(null, false, List.of(""), false, List.of(""),
                false, List.of(Artwork.Status.在售), false, List.of(""), PageRequest.of(0, 5)));
    }

}
