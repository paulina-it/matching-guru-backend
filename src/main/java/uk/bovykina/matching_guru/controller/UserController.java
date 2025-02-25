package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uk.bovykina.matching_guru.service.UserService;
import uk.bovykina.matching_guru.dto.user.UserResponseDto;
import uk.bovykina.matching_guru.dto.user.UserUpdateDto;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3001", allowCredentials = "true")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Uploads a profile image.
     */
    @PostMapping("/upload-profile-image")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("email") String email,
                                                     @RequestParam("file") MultipartFile file) {
        log.info("📸 Uploading profile image for email: {}", email);

        if (file == null || file.isEmpty()) {
            log.error("❌ No file received for email: {}", email);
            return ResponseEntity.status(400).body("No file uploaded.");
        }

        try {
            log.info("✅ Received file: {}, Size: {}", file.getOriginalFilename(), file.getSize());

            String imageUrl = userService.uploadProfileImage(email, file);
            log.info("✅ Profile image updated successfully for email: {}", email);

            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            log.error("❌ Error uploading image for email {}: {}", email, e.getMessage());
            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File is too large! Maximum allowed size is 5MB.");
    }


    /**
     * Updates user details.
     */
    @PutMapping("/update")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody UserUpdateDto userUpdateDto) {
        log.info("🔄 Updating user: {}", userUpdateDto.getEmail());
        try {
            UserResponseDto updatedUser = userService.updateUser(userUpdateDto);
            log.info("✅ User updated successfully: {}", updatedUser.getEmail());
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            log.error("❌ Error updating user: {}", e.getMessage());
            return ResponseEntity.status(404).body(null);
        }
    }

    /**
     * Fetch all users.
     */
    @GetMapping("/all")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("📌 Fetching users - Page: {}, Size: {}", page, size);
        Page<UserResponseDto> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Fetch user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("🔍 Fetching user with ID: {}", id);
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (UserNotFoundException e) {
            log.warn("⚠️ User not found with ID: {}", id);
            return ResponseEntity.status(404).body(null);
        }
    }

    /**
     * Delete a user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("🗑️ Deleting user with ID: {}", id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("✅ User deleted successfully.");
        } catch (UserNotFoundException e) {
            log.warn("⚠️ User not found with ID: {}", id);
            return ResponseEntity.status(404).body("User not found.");
        }
    }
}
