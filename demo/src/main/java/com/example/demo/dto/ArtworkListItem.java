package com.example.demo.dto;
import java.math.BigDecimal;
public record ArtworkListItem(Integer artworkId, String name, String author, String description, String saleType,
                              BigDecimal fixedPrice, String status, String categoryName,
                              String sellerName, String imageUrl) { }
