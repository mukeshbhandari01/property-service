package com.propertyfinder.property.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "property_listings")

public class PropertyListing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String ownerEmail;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false, length = 40)
    private String purpose;
    @Column(nullable = false, length = 60)
    private String propertyType;
    @Column(nullable = false, length = 120)
    private String city;
    @Column(nullable = false, length = 500)
    private String address;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;
    @Column(length = 3000)
    private String description;
    @Column(length = 4000)
    private String imageUrls;

    // Contact details are controlled separately for every property listing.
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean showEmail = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean showPhone = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean showInstagram = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean showFacebook = false;

    @Column(nullable = false)
    private boolean active = true;
    private Instant createdAt = Instant.now();
}



