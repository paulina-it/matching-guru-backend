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
import uk.bovykina.matching_guru.mapper.OrganisationMapper;
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
    private final OrganisationMapper organisationMapper;

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Creates a new organisation and assigns it to the user.
     */
    public OrganisationDto createOrganisation(OrganisationCreateDto dto, Long userId) {
        log.info("Creating organisation for userId={}", userId);

        Organisation organisation = organisationMapper.toEntity(dto);
        organisation.setJoinCode(generateUniqueJoinCode());
        Organisation saved = organisationRepository.save(organisation);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setOrganisation(saved);
        userRepository.save(user);

        log.info("Organisation created with ID={}", saved.getId());
        return organisationMapper.toDto(saved);
    }

    /**
     * Regenerates a new join code for the organisation.
     */
    public OrganisationDto regenerateJoinCode(Long organisationId) {
        log.info("Regenerating join code for organisationId={}", organisationId);
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        organisation.setJoinCode(generateUniqueJoinCode());
        Organisation updated = organisationRepository.save(organisation);

        log.info("New join code set for organisationId={}", updated.getId());
        return organisationMapper.toDto(updated);
    }

    /**
     * Uploads a new logo for the given organisation.
     */
    public String uploadOrganisationLogo(Long organisationId, MultipartFile file) throws IOException {
        log.info("Uploading logo for organisationId={}", organisationId);

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new NoSuchElementException("Organisation not found"));

        String logoUrl = cloudinaryService.uploadImage(file, "organisation_logos");
        organisation.setLogoUrl(logoUrl);
        organisationRepository.save(organisation);

        log.info("Logo uploaded and saved for organisationId={}", organisationId);
        return logoUrl;
    }

    /**
     * Validates an invite token's existence and expiry.
     */
    public boolean validateInviteToken(String token) {
        log.info("Validating invite token: {}", token);
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (inviteToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Invite token expired: {}", token);
            inviteTokenRepository.delete(inviteToken);
            return false;
        }

        return true;
    }

    /**
     * Deletes an invite token by its string value.
     */
    public void deleteInviteToken(String token) {
        log.info("Deleting invite token: {}", token);
        inviteTokenRepository.findByToken(token).ifPresent(inviteTokenRepository::delete);
    }

    /**
     * Retrieves an organisation by its ID.
     */
    public Optional<OrganisationDto> getOrganisationById(Long id) {
        log.info("Fetching organisation by ID: {}", id);
        return organisationRepository.findById(id).map(organisationMapper::toDto);
    }

    /**
     * Returns all organisations.
     */
    public List<OrganisationDto> getAllOrganisations() {
        log.info("Fetching all organisations");
        return organisationRepository.findAll().stream()
                .map(organisationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates the organisation details.
     */
    @Transactional
    public OrganisationDto updateOrganisation(Long id, OrganisationUpdateDto dto) {
        log.info("Updating organisation ID={}", id);
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        organisationMapper.updateFromDto(organisation, dto);
        Organisation updated = organisationRepository.save(organisation);

        log.info("Organisation updated: ID={}", updated.getId());
        return organisationMapper.toDto(updated);
    }

    /**
     * Joins an organisation via join code.
     */
    @Transactional
    public OrganisationDto joinOrganisation(Long userId, String joinCode) {
        log.info("User ID={} attempting to join organisation with joinCode={}", userId, joinCode);

        Organisation organisation = organisationRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid join code"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setOrganisation(organisation);
        userRepository.save(user);

        log.info("User ID={} joined organisation ID={}", userId, organisation.getId());
        return organisationMapper.toDto(organisation);
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
}
