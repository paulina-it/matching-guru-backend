package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.organisation.*;
import uk.bovykina.matching_guru.entity.InviteToken;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.repository.InviteTokenRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationRepository organisationRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final UserRepository userRepository;
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_TOKEN_EXPIRY_HOURS = 24;

    public OrganisationDto createOrganisation(OrganisationCreateDto organisationCreateDto, Long userId) {
        Organisation organisation = OrganisationMapper.toOrganisation(organisationCreateDto);
        organisation.setJoinCode(generateUniqueJoinCode());
        Organisation savedOrganisation = organisationRepository.save(organisation);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setOrganisation(savedOrganisation);
        userRepository.save(user);

        return OrganisationMapper.toOrganisationDto(savedOrganisation);
    }

    public String generateInviteToken(Long organisationId, String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(INVITE_TOKEN_EXPIRY_HOURS);

        InviteToken inviteToken = new InviteToken(token, organisationId, email, expiryDate);
        inviteTokenRepository.save(inviteToken);

        return token;
    }

    public boolean validateInviteToken(String token) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (inviteToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            inviteTokenRepository.delete(inviteToken); // Expired token, delete it
            return false;
        }
        return true;
    }

    public void deleteInviteToken(String token) {
        inviteTokenRepository.findByToken(token)
                .ifPresent(inviteTokenRepository::delete);
    }

    public Optional<OrganisationDto> getOrganisationById(Long id) {
        return organisationRepository.findById(id).map(OrganisationMapper::toOrganisationDto);
    }

    public List<OrganisationDto> getAllOrganisations() {
        return organisationRepository.findAll().stream()
                .map(OrganisationMapper::toOrganisationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrganisationDto updateOrganisation(Long id, OrganisationUpdateDto organisationUpdateDto) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        OrganisationMapper.updateOrganisationFromDto(organisation, organisationUpdateDto);
        Organisation updatedOrganisation = organisationRepository.save(organisation);

        return OrganisationMapper.toOrganisationDto(updatedOrganisation);
    }

    public void deleteOrganisation(Long id) {
        organisationRepository.deleteById(id);
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
            if (dto.getName() != null) organisation.setName(dto.getName());
            if (dto.getDescription() != null) organisation.setDescription(dto.getDescription());
        }
    }
}
