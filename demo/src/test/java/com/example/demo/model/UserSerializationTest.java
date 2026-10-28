package com.example.demo.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void passwordIsAcceptedOnInputButNeverReturnedInJson() throws Exception {
        User user = objectMapper.readValue("{\"username\":\"tester\",\"password\":\"secret\"}", User.class);

        String responseJson = objectMapper.writeValueAsString(user);

        assertEquals("secret", user.getPassword());
        assertFalse(responseJson.contains("password"));
        assertFalse(responseJson.contains("secret"));
    }

    @Test
    void publicArtworkUserViewExcludesSensitiveFields() throws Exception {
        User user = new User();
        user.setUserId(7);
        user.setUsername("artist");
        user.setAvatar("avatar.png");
        user.setEmail("artist@example.test");
        user.setPhone("13800000000");
        user.setRealName("Sensitive Name");
        user.setIdCardNumber("Sensitive ID");
        user.setAddress("Sensitive Address");
        user.setWalletBalance(java.math.BigDecimal.TEN);
        user.setRole("Admin");
        Artwork artwork = new Artwork();
        artwork.setUser(user);

        String responseJson = objectMapper.writeValueAsString(artwork);

        assertEquals(true, responseJson.contains("artist"));
        assertFalse(responseJson.contains("artist@example.test"));
        assertFalse(responseJson.contains("13800000000"));
        assertFalse(responseJson.contains("Sensitive Name"));
        assertFalse(responseJson.contains("Sensitive ID"));
        assertFalse(responseJson.contains("Sensitive Address"));
        assertFalse(responseJson.contains("walletBalance"));
        assertFalse(responseJson.contains("\"role\""));
    }
}
