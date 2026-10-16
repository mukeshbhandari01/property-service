package com.propertyfinder.property.service;

import com.propertyfinder.property.dto.SellerProfileRequest;
import com.propertyfinder.property.entity.SellerProfile;
import com.propertyfinder.property.repository.SellerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerProfileService {
    private final SellerProfileRepository repository;

    @Transactional(readOnly = true)
    public SellerProfile getProfile(String authenticatedEmail) {
        String email = normalizeEmail(authenticatedEmail);
        return repository.findBySellerEmailIgnoreCase(email)
                .orElseGet(() -> {
                    SellerProfile empty = new SellerProfile();
                    empty.setSellerEmail(email);
                    return empty;
                });
    }

    @Transactional
    public SellerProfile saveProfile(String authenticatedEmail, SellerProfileRequest request) {
        String email = normalizeEmail(authenticatedEmail);
        SellerProfile profile = repository.findBySellerEmailIgnoreCase(email)
                .orElseGet(() -> {
                    SellerProfile created = new SellerProfile();
                    created.setSellerEmail(email);
                    return created;
                });

        profile.setPhone(clean(request.phone()));
        profile.setInstagramUrl(clean(request.instagramUrl()));
        profile.setFacebookUrl(clean(request.facebookUrl()));
        return repository.save(profile);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new SecurityException("Authenticated seller email is missing");
        }
        return email.trim().toLowerCase();
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
