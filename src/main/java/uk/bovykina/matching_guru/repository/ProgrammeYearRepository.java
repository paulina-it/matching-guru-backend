package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeYear;

import java.util.List;
import java.util.Optional;

public interface ProgrammeYearRepository extends JpaRepository<ProgrammeYear, Long> {
    Optional<ProgrammeYear> findProgrammeYearByAcademicYear(String academicYear);
    Optional<ProgrammeYear> findProgrammeYearById(Long id);
    List<ProgrammeYear> findProgrammeYearByProgrammeId(Long programmeId);
    List<ProgrammeYear> findByProgramme_Organisation_Id(Long oeganisationId);

    @Query("SELECT py FROM ProgrammeYear py WHERE py.programme.organisation.id = :orgId AND py.isActive = true")
    List<ProgrammeYear> findByProgrammeOrganisationIdAndIsActiveTrue(@Param("orgId") Long organisationId);
}
