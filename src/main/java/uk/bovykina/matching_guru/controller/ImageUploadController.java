package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.service.OrganisationService;
import uk.bovykina.matching_guru.service.UserService;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:3001", allowCredentials = "true")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public class ImageUploadController {

    private final UserService userService;
    private final OrganisationService organisationService;

    /**
     * Uploads a profile image and returns the image URL.
     */
    @PostMapping("/profile-image")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        log.info("📸 Uploading profile image...");

        if (file == null || file.isEmpty()) {
            log.error("❌ No file uploaded.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
        }

        try {
            String imageUrl = userService.uploadProfileImage(file);
            log.info("✅ Image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            log.error("❌ Error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        }
    }

    /**
     * Uploads a logo for an organisation.
     */
    @PostMapping("/organisation-logo/{organisationId}")
    public ResponseEntity<String> uploadLogo(
            @PathVariable Long organisationId,
            @RequestParam("file") MultipartFile file) {
        log.info("📤 Uploading organisation logo for ID: {}", organisationId);

        if (file == null || file.isEmpty()) {
            log.warn("⚠️ No file received for organisation ID: {}", organisationId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("⚠️ Unsupported file type for organisation ID: {}", organisationId);
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only image files are allowed (JPG, PNG, etc).");
        }

        try {
            String logoUrl = organisationService.uploadOrganisationLogo(organisationId, file);
            log.info("✅ Logo uploaded successfully for organisation ID: {}", organisationId);
            return ResponseEntity.ok("Logo uploaded successfully: " + logoUrl);
        } catch (IOException e) {
            log.error("❌ Error uploading logo for organisation ID: {}", organisationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload logo: " + e.getMessage());
        }
    }

    /**
     * Handles max upload size exceeded.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File is too large! Maximum allowed size is 5MB.");
    }
}
