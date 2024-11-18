package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.Programme;
import java.util.List;

public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
    List<Programme> findByOrganisationId(Long organisationId);
}
