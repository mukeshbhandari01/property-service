package com.propertyfinder.property.dto;

import jakarta.validation.constraints.Size;

public record SellerProfileRequest(
        @Size(max = 30) String phone,
        @Size(max = 500) String instagramUrl,
        @Size(max = 500) String facebookUrl
) {
}
