package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeYear;

import java.util.Optional;

public interface ProgrammeYearRepository extends JpaRepository<ProgrammeYear, Long> {
    Optional<ProgrammeYear> findProgrammeYearByAcademicYear(String academicYear);
    Optional<ProgrammeYear> findProgrammeYearById(Long id);
    Optional<ProgrammeYear> findProgrammeYearByProgrammeId(Long programmeId);

}
