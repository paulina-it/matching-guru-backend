package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.organisation.OrganisationDto;
import uk.bovykina.matching_guru.dto.user.LoginResponse;
import uk.bovykina.matching_guru.dto.user.UserCreateDto;
import uk.bovykina.matching_guru.dto.user.UserLoginDto;
import uk.bovykina.matching_guru.dto.user.UserResponseDto;
import uk.bovykina.matching_guru.entity.enums.UserRole;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.service.OrganisationService;
import uk.bovykina.matching_guru.service.UserService;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final OrganisationService organisationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDto request) {
        try {
            logger.info("Login attempt for email: {}", request.getEmail());
            LoginResponse response = userService.login(request.getEmail(), request.getPassword());
            logger.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            logger.warn("Login failed: User not found for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        } catch (IllegalArgumentException e) {
            logger.warn("Login failed: Invalid credentials for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        } catch (RuntimeException e) {
            logger.error("An unexpected error occurred during login for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during login");
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateDto userCreateDto) {
        try {
            logger.info("Signup attempt for email: {}", userCreateDto.getEmail());
            userService.createUser(userCreateDto);
            LoginResponse response = userService.login(userCreateDto.getEmail(), userCreateDto.getPassword());
            logger.info("Signup and login successful for email: {}", userCreateDto.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Signup failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserNotFoundException e) {
            logger.error("Signup error: User not found after creation for email: {}", userCreateDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        } catch (RuntimeException e) {
            logger.error("An unexpected error occurred during registration for email: {}", userCreateDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration");
        }
    }

    @GetMapping("/admin/organisation-status")
    public ResponseEntity<?> checkAdminOrganisationStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthenticated access attempt to /auth/admin/organisation-status");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        try {
            UserResponseDto userDto = userService.getUserByEmail(email);

            // Check if the user has ADMIN role
            if (!UserRole.ADMIN.equals(userDto.getRole())) {
                logger.warn("Access denied: Non-admin user attempted to access /admin/organisation-status, email: {}", email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: User is not an admin");
            }

            // Check if the admin has an associated organisation
            if (userDto.getOrganisationId() == null) {
                return ResponseEntity.ok("Admin has no organisation");
            }

            Optional<OrganisationDto> organisation = organisationService.getOrganisationById(userDto.getOrganisationId());
            return ResponseEntity.ok(organisation);
        } catch (UserNotFoundException e) {
            logger.error("User not found during /auth/admin/organisation-status check for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (Exception e) {
            logger.error("An error occurred during organisation status check for admin, email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while checking organisation status");
        }
    }
}
