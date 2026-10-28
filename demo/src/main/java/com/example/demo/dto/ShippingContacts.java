package com.example.demo.dto;

public record ShippingContacts(Contact sender, Contact receiver) {
    public record Contact(String username, String address, String phone) { }
}
