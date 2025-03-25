package uk.bovykina.matching_guru.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantInProgrammeYear, Long> {
    Optional<ParticipantInProgrammeYear> findByUserId(Long userId);
    List<ParticipantInProgrammeYear> findByProgrammeYearId(Long programmeYearId);
    List<ParticipantInProgrammeYear> findByRole(ParticipantRole role);

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId")
    int countByProgrammeYearId(@Param("programmeYearId") Long programmeYearId);

    @Query("SELECT p FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId AND p.role = :role")
    List<ParticipantInProgrammeYear> findByProgrammeYearIdAndRole(@Param("programmeYearId") Long programmeYearId, @Param("role") ParticipantRole role);

    @Query(value = "SELECT COUNT(DISTINCT p.id) " +
            "FROM participants_in_programme_year p " +
            "JOIN programme_year py ON p.programme_year_id = py.id " +
            "WHERE py.programme_id = :programmeId", nativeQuery = true)
    Integer countDistinctParticipantsByProgrammeId(@Param("programmeId") Long programmeId);

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId AND p.isMatched = false")
    int countByProgrammeYearIdAndIsMatchedFalse(@Param("programmeYearId") Long programmeYearId);

    Page<ParticipantInProgrammeYear> findAllByProgrammeYear(ProgrammeYear programmeYear, Pageable pageable);

    @Query("""
        SELECT p FROM ParticipantInProgrammeYear p
        WHERE p.programmeYear = :programmeYear
          AND (
            LOWER(CONCAT(p.user.firstName, ' ', p.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%'))
          )
    """)
    Page<ParticipantInProgrammeYear> searchByProgrammeYearAndUserNameOrEmail(
            @Param("programmeYear") ProgrammeYear programmeYear,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM ParticipantInProgrammeYear p
        WHERE p.programmeYear = :programmeYear
          AND p.role = :role
          AND (
            LOWER(CONCAT(p.user.firstName, ' ', p.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%'))
          )
    """)
    Page<ParticipantInProgrammeYear> searchByProgrammeYearAndRoleAndUserNameOrEmail(
            @Param("programmeYear") ProgrammeYear programmeYear,
            @Param("role") ParticipantRole role,
            @Param("search") String search,
            Pageable pageable
    );

    Page<ParticipantInProgrammeYear> findByProgrammeYearAndRole(
            ProgrammeYear programmeYear,
            ParticipantRole role,
            Pageable pageable
    );

    ParticipantInProgrammeYear findByRoleAndIsMatched(ParticipantRole role, boolean isMatched);
}
