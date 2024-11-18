package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.Eligibility;

import java.util.List;

public interface EligibilityRepository extends JpaRepository<Eligibility, Long> {
    List<Eligibility> findByProgrammeId(Long programmeId);
}
