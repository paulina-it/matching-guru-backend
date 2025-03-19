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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
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
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found with id: " + userId);
                });

        user.setOrganisation(savedOrganisation);
        userRepository.save(user);

        log.info("Organisation '{}' created successfully with ID: {}", organisation.getName(), organisation.getId());
        return OrganisationMapper.toOrganisationDto(savedOrganisation);
    }

    public String uploadOrganisationLogo(Long organisationId, MultipartFile file) throws IOException {
        log.info("📸 Uploading logo for organisation ID: {}", organisationId);

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> {
                    log.error("❌ Organisation not found with ID: {}", organisationId);
                    return new NoSuchElementException("Organisation not found.");
                });

        String logoUrl = cloudinaryService.uploadImage(file);

        organisation.setLogoUrl(logoUrl);

        organisationRepository.save(organisation);

        log.info("✅ Logo uploaded successfully for organisation ID: {}", organisationId);
        return logoUrl;
    }


    public String generateInviteToken(Long organisationId, String email) {
        log.info("Generating invite token for organisation ID: {} and email: {}", organisationId, email);
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(INVITE_TOKEN_EXPIRY_HOURS);

        InviteToken inviteToken = new InviteToken(token, organisationId, email, expiryDate);
        inviteTokenRepository.save(inviteToken);

        log.info("Invite token generated successfully for email: {}", email);
        return token;
    }

    public boolean validateInviteToken(String token) {
        log.info("Validating invite token: {}", token);

        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid invite token: {}", token);
                    return new IllegalArgumentException("Invalid invitation token");
                });

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
                .ifPresentOrElse(
                        inviteTokenRepository::delete,
                        () -> log.warn("Attempted to delete non-existing token: {}", token)
                );
    }

    public Optional<OrganisationDto> getOrganisationById(Long id) {
        log.info("Fetching organisation with ID: {}", id);
        return organisationRepository.findById(id)
                .map(organisation -> {
                    log.info("Organisation found: {}", organisation.getName());
                    return OrganisationMapper.toOrganisationDto(organisation);
                });
    }

    public List<OrganisationDto> getAllOrganisations() {
        log.info("Fetching all organisations");
        List<OrganisationDto> organisations = organisationRepository.findAll().stream()
                .map(OrganisationMapper::toOrganisationDto)
                .collect(Collectors.toList());
        log.info("Total organisations found: {}", organisations.size());
        return organisations;
    }

    @Transactional
    public OrganisationDto updateOrganisation(Long id, OrganisationUpdateDto organisationUpdateDto) {
        log.info("Updating organisation with ID: {}", id);

        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Organisation not found with ID: {}", id);
                    return new IllegalArgumentException("Organisation not found");
                });

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
        log.info("User with ID {} is attempting to join organisation with join code: {}", userId, joinCode);

        Organisation organisation = organisationRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> {
                    log.warn("Invalid join code: {}", joinCode);
                    return new IllegalArgumentException("Invalid join code");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        user.setOrganisation(organisation);
        userRepository.save(user);

        log.info("User ID {} successfully joined organisation '{}'", userId, organisation.getName());
        return OrganisationMapper.toOrganisationDto(organisation);
    }

    private String generateUniqueJoinCode() {
        log.info("Generating unique join code for an organisation");
        String joinCode;
        do {
            joinCode = generateRandomCode(6);
        } while (organisationRepository.findByJoinCode(joinCode).isPresent());
        log.info("Generated join code: {}", joinCode);
        return joinCode;
    }

    private String generateRandomCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder codeBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            codeBuilder.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return codeBuilder.toString();
    }

    private static class OrganisationMapper {
        public static Organisation toOrganisation(OrganisationCreateDto dto) {
            Organisation organisation = new Organisation();
            organisation.setName(dto.getName());
            organisation.setDescription(dto.getDescription());
            return organisation;
        }

        public static OrganisationDto toOrganisationDto(Organisation organisation) {
            OrganisationDto dto = new OrganisationDto();
            dto.setId(organisation.getId());
            dto.setName(organisation.getName());
            dto.setJoinCode(organisation.getJoinCode());
            dto.setDescription(organisation.getDescription());
            return dto;
        }

        public static void updateOrganisationFromDto(Organisation organisation, OrganisationUpdateDto dto) {
            if (dto.getName() != null) {
                log.info("Updating organisation name to '{}'", dto.getName());
                organisation.setName(dto.getName());
            }
            if (dto.getDescription() != null) {
                log.info("Updating organisation description");
                organisation.setDescription(dto.getDescription());
            }
        }
    }
}
