package com.propertyfinder.property.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreatePropertyRequest(
        @NotBlank String ownerEmail,
        @NotBlank @Size(max = 120) String title,
        @NotBlank String purpose,
        @NotBlank String propertyType,
        @NotBlank String city,
        @NotBlank String address,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Size(max = 3000) String description,
        String imageUrls,
        Boolean showEmail,
        Boolean showPhone,
        Boolean showInstagram,
        Boolean showFacebook
) {
}
