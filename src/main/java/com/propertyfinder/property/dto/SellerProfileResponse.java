package com.propertyfinder.property.dto;

public record SellerProfileResponse(
        String email,
        String phone,
        String instagramUrl,
        String facebookUrl
) {
}
