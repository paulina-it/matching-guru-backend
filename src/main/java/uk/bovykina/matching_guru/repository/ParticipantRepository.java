package uk.bovykina.matching_guru.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.dto.dashboards.ActivityJoinProjection;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantInProgrammeYear, Long> {
    Optional<ParticipantInProgrammeYear> findByUserId(Long userId);
    Optional<ParticipantInProgrammeYear> findByUserIdAndProgrammeYearId(Long userId, Long programmeYearId);
    Optional<ParticipantInProgrammeYear> findByIdAndProgrammeYearId(Long id, Long programmeYearId);
    List<ParticipantInProgrammeYear> findAllByUserId(Long userId);
    List<ParticipantInProgrammeYear> findByProgrammeYearId(Long programmeYearId);
    List<ParticipantInProgrammeYear> findByRole(ParticipantRole role);
    Page<ParticipantInProgrammeYear> findAllByProgrammeYear(ProgrammeYear programmeYear, Pageable pageable);
    Page<ParticipantInProgrammeYear> findByProgrammeYearAndRole(ProgrammeYear programmeYear, ParticipantRole role, Pageable pageable);

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

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId")
    int countByProgrammeYearId(@Param("programmeYearId") Long programmeYearId);

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId AND p.isMatched = true")
    int countMatchedInProgrammeYear(@Param("programmeYearId") Long programmeYearId);

    @Query("SELECT COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id = :programmeYearId AND p.isMatched = false")
    int countUnmatchedInProgrammeYear(@Param("programmeYearId") Long programmeYearId);

    @Query("SELECT p.programmeYear.id, COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id IN :ids GROUP BY p.programmeYear.id")
    List<Object[]> countParticipantsByProgrammeYears(@Param("ids") List<Long> programmeYearIds);

    @Query("SELECT p.programmeYear.id, COUNT(p) FROM ParticipantInProgrammeYear p WHERE p.programmeYear.id IN :ids AND p.isMatched = true GROUP BY p.programmeYear.id")
    List<Object[]> countMatchedByProgrammeYears(@Param("ids") List<Long> programmeYearIds);

    @Query(value = """
            SELECT COUNT(DISTINCT p.id)
            FROM participants_in_programme_year p
            JOIN programme_year py ON p.programme_year_id = py.id
            WHERE py.programme_id = :programmeId
    """, nativeQuery = true)
    Integer countDistinctParticipantsByProgrammeId(@Param("programmeId") Long programmeId);

    @Query("""
        SELECT py.id AS programmeYearId,
               py.programme.id AS programmeId,
               py.academicYear AS programmeYearName,
               COUNT(p.id) AS count,
               MAX(p.createdAt) AS timestamp
        FROM ParticipantInProgrammeYear p
        JOIN p.programmeYear py
        WHERE py.programme.organisation.id = :organisationId
          AND p.createdAt > :since
        GROUP BY py.id, py.programme.id, py.academicYear
        ORDER BY count DESC
    """)
    List<ActivityJoinProjection> findRecentJoinsByOrganisationId(
            @Param("organisationId") Long organisationId,
            @Param("since") LocalDateTime since
    );

    ParticipantInProgrammeYear findByRoleAndIsMatched(ParticipantRole role, boolean isMatched); // Be careful with this – ambiguous
}
