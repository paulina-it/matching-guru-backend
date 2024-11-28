package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.enums.CriterionType;

import java.util.List;

public interface ProgrammeMatchingCriteriaRepository extends JpaRepository<ProgrammeMatchingCriteria, Long> {

    List<ProgrammeMatchingCriteria> findByProgrammeYearId(Long programmeYearId);
    List<ProgrammeMatchingCriteria> findByProgrammeYearIdAndCriterionType(Long programmeYearId, CriterionType criterionType);

    @Query("SELECT COUNT(c) FROM ProgrammeMatchingCriteria c WHERE c.programmeYear.id = :programmeYearId")
    Integer countCriteriaByProgrammeYearId(@Param("programmeYearId") Long programmeYearId);

    void deleteByProgrammeYearId(Long id);
}
