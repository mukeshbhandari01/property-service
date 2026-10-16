package com.propertyfinder.property.repository;

import com.propertyfinder.property.entity.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findBySellerEmailIgnoreCase(String sellerEmail);
}
