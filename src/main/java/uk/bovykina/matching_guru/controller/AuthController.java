package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.user.*;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.service.OrganisationService;
import uk.bovykina.matching_guru.service.UserService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final OrganisationService organisationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Checks whether the current user is authenticated and retrieves their profile.
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthenticated access attempt to /status");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();

        try {
            UserResponseDto userDto = userService.getUserByEmail(email);

            if (userDto.getOrganisationId() != null) {
                organisationService.getOrganisationById(userDto.getOrganisationId())
                        .ifPresent(org -> userDto.setOrganisationName(org.getName()));
            }

            return ResponseEntity.ok(userDto);

        } catch (UserNotFoundException e) {
            logger.error("User not found during /status check for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (Exception e) {
            logger.error("Unexpected error during /status check for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while checking authentication status");
        }
    }

    /**
     * Processes a login request using email and password credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDto request) {
        logger.info("🔑 Login attempt for email: {}", request.getEmail());
        try {
            LoginResponse response = userService.login(request.getEmail(), request.getPassword());
            logger.info("✅ Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Login error for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Registers a new user and logs them in upon successful creation.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateDto userCreateDto) {
        logger.info("📝 Signup attempt for email: {}", userCreateDto.getEmail());
        try {
            userService.createUser(userCreateDto);
            LoginResponse response = userService.login(userCreateDto.getEmail(), userCreateDto.getPassword());
            logger.info("✅ Signup and login successful for email: {}", userCreateDto.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Signup error for email: {} - {}", userCreateDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Allows the authenticated user to update their password by providing the current one.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            String email = auth.getName();
            Long userId = userService.getUserByEmail(email).getId();
            userService.changeOwnPassword(userId, dto.getOldPassword(), dto.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Password change failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}
