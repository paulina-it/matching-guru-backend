package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.organisation.OrganisationCreateDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationUpdateDto;
import uk.bovykina.matching_guru.dto.user.UserResponseDto;
import uk.bovykina.matching_guru.entity.enums.UserRole;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.service.OrganisationService;
import uk.bovykina.matching_guru.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/organisations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;
    private final UserService userService;

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


    @PostMapping("/{organisationId}/invite")
    public ResponseEntity<String> generateInviteToken(
            @PathVariable Long organisationId,
            @RequestParam String email) {
        String inviteToken = organisationService.generateInviteToken(organisationId, email);
        return ResponseEntity.ok(inviteToken);
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
}
