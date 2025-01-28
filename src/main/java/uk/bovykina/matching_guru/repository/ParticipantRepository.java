package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantInProgrammeYear, Long> {
    Optional<ParticipantInProgrammeYear> findByUserId(Long userId);

    List<ParticipantInProgrammeYear> findByProgrammeYearId(Long programmeYearId);

    List<ParticipantInProgrammeYear> findByRole(ParticipantRole role);

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p " +
            "WHERE p.programmeYear.programme.id = :programmeId")
    Integer countParticipantsByProgrammeId(@Param("programmeId") Long programmeId);
}
