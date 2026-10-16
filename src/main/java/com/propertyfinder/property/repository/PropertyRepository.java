package com.propertyfinder.property.repository;

import com.propertyfinder.property.entity.PropertyListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<PropertyListing, String> {
    List<PropertyListing> findByActiveTrueAndCityContainingIgnoreCase(String city);

    List<PropertyListing> findByOwnerEmailIgnoreCase(String email);

    @Override
    Optional<PropertyListing> findById(String s);


   Optional<PropertyListing> findByOwnerEmail(String username);

    List<PropertyListing> findTop20ByOrderByIdDesc();
}