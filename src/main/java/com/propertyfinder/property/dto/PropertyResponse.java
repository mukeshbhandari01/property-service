package com.propertyfinder.property.dto;

import java.math.BigDecimal;

public record PropertyResponse(
        String id,
        String title,
        String city,
        BigDecimal price,
        String imageUrl
) {
}
