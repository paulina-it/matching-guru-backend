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

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId")
    int countByProgrammeYearId(Long programmeYearId);

    @Query("SELECT p FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId AND p.role = :role")
    List<ParticipantInProgrammeYear> findByProgrammeYearIdAndRole(@Param("programmeYearId") Long programmeYearId, @Param("role") ParticipantRole role);

    default List<ParticipantInProgrammeYear> debugFindByProgrammeYearIdAndRole(Long programmeYearId, ParticipantRole role) {
        List<ParticipantInProgrammeYear> participants = findByProgrammeYearIdAndRole(programmeYearId, role);
        System.out.println("🔍 Найдено " + participants.size() + " участников с ролью " + role + " в ProgrammeYear ID: " + programmeYearId);
        participants.forEach(p -> System.out.println("  - ID: " + p.getId() + ", UserID: " + (p.getUser() != null ? p.getUser().getId() : "NULL")));
        return participants;
    }

    @Query(value = "SELECT COUNT(DISTINCT p.id) " +
            "FROM participants_in_programme_year p " +
            "JOIN programme_year py ON p.programme_year_id = py.id " +
            "WHERE py.programme_id = :programmeId", nativeQuery = true)
    Integer countDistinctParticipantsByProgrammeId(@Param("programmeId") Long programmeId);

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId AND p.isMatched = false")
    int countByProgrammeYearIdAndIsMatchedFalse(@Param("programmeYearId") Long programmeYearId);

    ParticipantInProgrammeYear findByRoleAndIsMatched(ParticipantRole participantRole, boolean b);
}
