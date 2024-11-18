package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.InviteToken;

import java.util.Optional;

public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {
    Optional<InviteToken> findByToken(String token);
    Optional<InviteToken> findByEmailAndOrganisationId(String email, Long organisationId);
}
