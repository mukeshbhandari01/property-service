
package com.propertyfinder.property.service;

import com.propertyfinder.property.dto.CreatePropertyRequest;
import com.propertyfinder.property.dto.PropertyResponse;
import com.propertyfinder.property.entity.PropertyListing;
import com.propertyfinder.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository repo;
    private final S3Service s3Service;

    public PropertyListing create(
            CreatePropertyRequest r,
            MultipartFile file,
            String authenticatedEmail
    ) throws IOException {

        String key = null;

        // Upload to S3 only when a non-empty image is provided
        if (file != null && !file.isEmpty()) {
            key = s3Service.uploadPhoto(
                    authenticatedEmail.trim().toLowerCase(),
                    file
            );
        }

        PropertyListing p = new PropertyListing();

        p.setOwnerEmail(authenticatedEmail.trim().toLowerCase());
        p.setTitle(r.title());
        p.setPurpose(r.purpose());
        p.setPropertyType(r.propertyType());
        p.setCity(r.city());
        p.setAddress(r.address());
        p.setPrice(r.price());
        p.setDescription(r.description());
        p.setImageUrls(key);
        p.setShowEmail(r.showEmail() == null || r.showEmail());
        p.setShowPhone(Boolean.TRUE.equals(r.showPhone()));
        p.setShowInstagram(Boolean.TRUE.equals(r.showInstagram()));
        p.setShowFacebook(Boolean.TRUE.equals(r.showFacebook()));

        return repo.save(p);
    }

    public void deleteOwned(String id, String authenticatedEmail) {
        PropertyListing p = get(id);

        if (!p.getOwnerEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new SecurityException(
                    "You can only delete your own property listing"
            );
        }

        repo.delete(p);
    }

    public List<PropertyResponse> search(String city) {

        List<PropertyListing> properties =
                repo.findByActiveTrueAndCityContainingIgnoreCase(city);

        return properties.stream()
                .map(property -> {

                    String imageUrl = null;

                    // Generate an S3 URL only if an image key exists
                    if (property.getImageUrls() != null
                            && !property.getImageUrls().isBlank()) {

                        imageUrl = s3Service.generatePhotoUrl(
                                property.getImageUrls()
                        );
                    }

                    return new PropertyResponse(
                            property.getId(),
                            property.getTitle(),
                            property.getCity(),
                            property.getPrice(),
                            imageUrl
                    );
                })
                .toList();
    }

    public PropertyListing get(String id) {
        PropertyListing property=repo.findById(id) .orElseThrow(() ->
                new IllegalArgumentException("Property not found")
        );
        property.setImageUrls(s3Service.generatePhotoUrl(property.getImageUrls()));
        return property;
    }

    public List<PropertyListing> byOwner(String email) {
        return repo.findByOwnerEmailIgnoreCase(email);
    }

    public List<PropertyResponse> getHomeProperties() {

        List<PropertyListing> properties =
                repo.findTop20ByOrderByIdDesc();

        return properties.stream()
                .map(property -> {

                    String imageUrl = null;

                    // Properties without images do not need an S3 URL
                    if (property.getImageUrls() != null
                            && !property.getImageUrls().isBlank()) {

                        imageUrl = s3Service.generatePhotoUrl(
                                property.getImageUrls()
                        );
                    }

                    return new PropertyResponse(
                            property.getId(),
                            property.getTitle(),
                            property.getCity(),
                            property.getPrice(),
                            imageUrl
                    );
                })
                .toList();
    }
}
