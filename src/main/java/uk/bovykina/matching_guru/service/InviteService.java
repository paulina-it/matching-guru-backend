package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.InviteToken;
import uk.bovykina.matching_guru.repository.InviteTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteTokenRepository inviteTokenRepository;

    public InviteToken createInvite(Long organisationId, String email, Long createdByUserId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        InviteToken invite = InviteToken.builder()
                .token(token)
                .organisationId(organisationId)
                .email(email)
                .expiryDate(expiryDate)
                .createdByUserId(createdByUserId)
                .build();

        return inviteTokenRepository.save(invite);
    }

    public InviteToken validateToken(String token) {
        InviteToken invite = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite token."));

        if (invite.isUsed()) {
            throw new IllegalStateException("Invite already used.");
        }

        if (invite.isExpired()) {
            throw new IllegalStateException("Invite token has expired.");
        }

        return invite;
    }

    public void markTokenAsUsed(String token) {
        InviteToken invite = validateToken(token);
        invite.setUsed(true);
        inviteTokenRepository.save(invite);
    }
}
