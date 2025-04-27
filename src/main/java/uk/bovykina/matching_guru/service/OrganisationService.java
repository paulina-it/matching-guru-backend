package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.organisation.*;
import uk.bovykina.matching_guru.entity.InviteToken;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.repository.InviteTokenRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.UserRepository;
import uk.bovykina.matching_guru.util.CloudinaryService;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationRepository organisationRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_TOKEN_EXPIRY_HOURS = 24;

    public OrganisationDto createOrganisation(OrganisationCreateDto organisationCreateDto, Long userId) {
        log.info("Creating a new organisation with name: {}", organisationCreateDto.getName());

        Organisation organisation = OrganisationMapper.toOrganisation(organisationCreateDto);
        organisation.setJoinCode(generateUniqueJoinCode());
        Organisation savedOrganisation = organisationRepository.save(organisation);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setOrganisation(savedOrganisation);
        userRepository.save(user);

        log.info("Organisation '{}' created successfully with ID: {}", organisation.getName(), organisation.getId());
        return OrganisationMapper.toOrganisationDto(savedOrganisation);
    }

    public OrganisationDto regenerateJoinCode(Long organisationId) {
        log.info("Regenerating join code for organisation ID: {}", organisationId);
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with ID: " + organisationId));

        organisation.setJoinCode(generateUniqueJoinCode());
        Organisation updated = organisationRepository.save(organisation);

        return OrganisationMapper.toOrganisationDto(updated);
    }

    public String uploadOrganisationLogo(Long organisationId, MultipartFile file) throws IOException {
        log.info("Uploading logo for organisation ID: {}", organisationId);
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new NoSuchElementException("Organisation not found with ID: " + organisationId));

        String logoUrl = cloudinaryService.uploadImage(file, "organisation_logos");
        organisation.setLogoUrl(logoUrl);
        organisationRepository.save(organisation);
        log.info("Logo uploaded successfully for organisation ID: {}", organisationId);
        return logoUrl;
    }

    public boolean validateInviteToken(String token) {
        log.info("Validating invite token: {}", token);
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (inviteToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            inviteTokenRepository.delete(inviteToken);
            log.warn("Expired invite token deleted: {}", token);
            return false;
        }

        log.info("Invite token is valid: {}", token);
        return true;
    }

    public void deleteInviteToken(String token) {
        log.info("Deleting invite token: {}", token);
        inviteTokenRepository.findByToken(token)
                .ifPresentOrElse(inviteTokenRepository::delete,
                        () -> log.warn("Attempted to delete non-existing token: {}", token));
    }

    public Optional<OrganisationDto> getOrganisationById(Long id) {
        log.info("Fetching organisation with ID: {}", id);
        return organisationRepository.findById(id)
                .map(OrganisationMapper::toOrganisationDto);
    }

    public List<OrganisationDto> getAllOrganisations() {
        log.info("Fetching all organisations");
        return organisationRepository.findAll().stream()
                .map(OrganisationMapper::toOrganisationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrganisationDto updateOrganisation(Long id, OrganisationUpdateDto organisationUpdateDto) {
        log.info("Updating organisation with ID: {}", id);
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with ID: " + id));

        OrganisationMapper.updateOrganisationFromDto(organisation, organisationUpdateDto);
        Organisation updatedOrganisation = organisationRepository.save(organisation);
        log.info("Organisation with ID {} updated successfully", id);
        return OrganisationMapper.toOrganisationDto(updatedOrganisation);
    }

    public void deleteOrganisation(Long id) {
        log.info("Deleting organisation with ID: {}", id);
        organisationRepository.deleteById(id);
        log.info("Organisation with ID {} deleted successfully", id);
    }

    @Transactional
    public OrganisationDto joinOrganisation(Long userId, String joinCode) {
        log.info("User with ID {} attempting to join organisation with join code: {}", userId, joinCode);
        Organisation organisation = organisationRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid join code"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setOrganisation(organisation);
        userRepository.save(user);

        log.info("User ID {} successfully joined organisation '{}'", userId, organisation.getName());
        return OrganisationMapper.toOrganisationDto(organisation);
    }

    private String generateUniqueJoinCode() {
        String joinCode;
        do {
            joinCode = generateRandomCode(6);
        } while (organisationRepository.findByJoinCode(joinCode).isPresent());
        return joinCode;
    }

    private String generateRandomCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    private static class OrganisationMapper {
        public static Organisation toOrganisation(OrganisationCreateDto dto) {
            Organisation organisation = new Organisation();
            organisation.setName(dto.getName());
            organisation.setDescription(dto.getDescription());
            organisation.setLogoUrl(dto.getLogoUrl());
            return organisation;
        }

        public static OrganisationDto toOrganisationDto(Organisation organisation) {
            OrganisationDto dto = new OrganisationDto();
            dto.setId(organisation.getId());
            dto.setName(organisation.getName());
            dto.setJoinCode(organisation.getJoinCode());
            dto.setDescription(organisation.getDescription());
            dto.setLogoUrl(organisation.getLogoUrl());
            return dto;
        }

        public static void updateOrganisationFromDto(Organisation organisation, OrganisationUpdateDto dto) {
            if (dto.getName() != null) organisation.setName(dto.getName());
            if (dto.getDescription() != null) organisation.setDescription(dto.getDescription());
        }
    }
}
