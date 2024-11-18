package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.Organisation;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Optional<Organisation> findByJoinCode(String joinCode);
    Optional<Organisation> findById(Long id);
    Optional<Organisation> findByName(String name);

    boolean existsByJoinCode(String joinCode);
}
