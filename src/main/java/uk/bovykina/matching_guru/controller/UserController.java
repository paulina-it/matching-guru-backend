package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.service.UserService;
import uk.bovykina.matching_guru.util.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/{userId}/upload-profile-image")
    public ResponseEntity<String> uploadProfileImage(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok("Profile image uploaded successfully: " + imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }
}
