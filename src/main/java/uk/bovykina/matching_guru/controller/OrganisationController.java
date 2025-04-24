package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.organisation.OrganisationCreateDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationUpdateDto;
import uk.bovykina.matching_guru.dto.user.UserResponseDto;
import uk.bovykina.matching_guru.entity.enums.UserRole;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.service.OrganisationService;
import uk.bovykina.matching_guru.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/organisations")
@RequiredArgsConstructor
@Slf4j
public class OrganisationController {

    private final OrganisationService organisationService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/create")
    public ResponseEntity<?> createOrganisation(
            @RequestBody OrganisationCreateDto organisationCreateDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        try {
            UserResponseDto userDto = userService.getUserByEmail(email);

            if (!UserRole.ADMIN.equals(userDto.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: User is not an admin");
            }

            OrganisationDto organisation = organisationService.createOrganisation(organisationCreateDto, userDto.getId());
            return ResponseEntity.ok(organisation);

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating organisation");
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateInviteToken(@RequestParam String token) {
        boolean isValid = organisationService.validateInviteToken(token);
        return ResponseEntity.ok(isValid);
    }

    @DeleteMapping("/invite/{token}")
    public ResponseEntity<Void> deleteInviteToken(@PathVariable String token) {
        organisationService.deleteInviteToken(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganisationDto> getOrganisationById(@PathVariable Long id) {
        return organisationService.getOrganisationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrganisationDto>> getAllOrganisations() {
        List<OrganisationDto> organisations = organisationService.getAllOrganisations();
        return ResponseEntity.ok(organisations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganisationDto> updateOrganisation(
            @PathVariable Long id,
            @RequestBody OrganisationUpdateDto organisationUpdateDto) {
        OrganisationDto updatedOrganisation = organisationService.updateOrganisation(id, organisationUpdateDto);
        return ResponseEntity.ok(updatedOrganisation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable Long id) {
        organisationService.deleteOrganisation(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Fetches organisation info available to admins.
     */
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

            // Check ADMIN role
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

    @PostMapping("/join")
    public ResponseEntity<?> joinOrganisation(@RequestParam String joinCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        try {
            UserResponseDto userDto = userService.getUserByEmail(email);

            OrganisationDto organisation = organisationService.joinOrganisation(userDto.getId(), joinCode);
            return ResponseEntity.ok(organisation);

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while joining organisation");
        }
    }

}
