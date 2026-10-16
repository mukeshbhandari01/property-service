
package com.propertyfinder.property.controller;

import com.propertyfinder.property.dto.CreatePropertyRequest;
import com.propertyfinder.property.dto.PropertyResponse;
import com.propertyfinder.property.dto.SellerProfileRequest;
import com.propertyfinder.property.dto.SellerProfileResponse;
import com.propertyfinder.property.entity.SellerProfile;
import com.propertyfinder.property.service.SellerProfileService;
import com.propertyfinder.property.entity.PropertyListing;
import com.propertyfinder.property.service.PropertyService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService service;
    private final SellerProfileService sellerProfileService;

    public PropertyController(PropertyService service, SellerProfileService sellerProfileService) {
        this.service = service;
        this.sellerProfileService = sellerProfileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @Valid @RequestPart("property") CreatePropertyRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication auth
    ) throws IOException {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
        }

        if (!hasRole(auth, "SELLER")) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Only sellers can create properties"));
        }

        // Image is optional. PropertyService will upload only a non-empty file.
        PropertyListing savedProperty =
                service.create(request, file, auth.getName());

        return ResponseEntity.ok(savedProperty);
    }


    @GetMapping("/seller-profile")
    public ResponseEntity<?> getSellerProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
        }
        if (!hasRole(auth, "SELLER")) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Only sellers can access seller profile"));
        }

        SellerProfile profile = sellerProfileService.getProfile(auth.getName());
        return ResponseEntity.ok(new SellerProfileResponse(
                auth.getName(),
                profile.getPhone(),
                profile.getInstagramUrl(),
                profile.getFacebookUrl()
        ));
    }

    @PutMapping("/seller-profile")
    public ResponseEntity<?> saveSellerProfile(
            @Valid @RequestBody SellerProfileRequest request,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
        }
        if (!hasRole(auth, "SELLER")) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Only sellers can update seller profile"));
        }

        SellerProfile profile = sellerProfileService.saveProfile(auth.getName(), request);
        return ResponseEntity.ok(new SellerProfileResponse(
                auth.getName(),
                profile.getPhone(),
                profile.getInstagramUrl(),
                profile.getFacebookUrl()
        ));
    }

    @GetMapping("/{id}/contact")
    public ResponseEntity<?> getPropertyContact(@PathVariable String id) {
        PropertyListing property = service.get(id);
        Map<String, String> contacts = new LinkedHashMap<>();

        if (property.isShowEmail() && property.getOwnerEmail() != null
                && !property.getOwnerEmail().isBlank()) {
            contacts.put("email", property.getOwnerEmail());
        }

        SellerProfile profile = sellerProfileService.getProfile(property.getOwnerEmail());

        if (property.isShowPhone() && profile.getPhone() != null
                && !profile.getPhone().isBlank()) {
            contacts.put("phone", profile.getPhone());
        }
        if (property.isShowInstagram() && profile.getInstagramUrl() != null
                && !profile.getInstagramUrl().isBlank()) {
            contacts.put("instagramUrl", profile.getInstagramUrl());
        }
        if (property.isShowFacebook() && profile.getFacebookUrl() != null
                && !profile.getFacebookUrl().isBlank()) {
            contacts.put("facebookUrl", profile.getFacebookUrl());
        }

        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/home")
    public List<PropertyResponse> getHomeProperties() {
        return service.getHomeProperties();
    }

    @GetMapping
    public List<PropertyResponse> search(
            @RequestParam(required = false) String city
    ) {
        return service.search(city);
    }

    @GetMapping("/{id}")
    public PropertyListing get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping("/owner/{email}")
    public ResponseEntity<?> byOwner(
            @PathVariable String email,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
        }

        if (!hasRole(auth, "SELLER")) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Seller role required"));
        }

        if (!auth.getName().equalsIgnoreCase(email)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "You can only view your own listings"));
        }

        return ResponseEntity.ok(service.byOwner(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String id,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
        }

        if (!hasRole(auth, "SELLER")) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Only sellers can delete property listings"));
        }

        service.deleteOwned(id, auth.getName());

        return ResponseEntity.noContent().build();
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_" + role));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> forbidden(SecurityException e) {
        return ResponseEntity.status(403)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> notFound(IllegalArgumentException e) {
        return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
    }
}
