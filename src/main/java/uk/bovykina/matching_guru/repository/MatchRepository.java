package uk.bovykina.matching_guru.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.dto.match.CoordinatorMatchDto;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.dto.match.MatchSummaryDto;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT COUNT(m) FROM Match m WHERE m.programmeYear.id = :programmeYearId AND m.status = 'PENDING'")
    int countPendingMatches(@Param("programmeYearId") Long programmeYearId);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.programmeYear.id = :programmeYearId")
    boolean existsByProgrammeYearId(@Param("programmeYearId") Long programmeYearId);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.mentor.id = :mentorId AND m.mentee.id = :menteeId")
    boolean existsByMentorIdAndMenteeId(@Param("mentorId") Long mentorId, @Param("menteeId") Long menteeId);

    @EntityGraph(attributePaths = {"mentor", "mentee", "programmeYear"})
    Page<Match> findByProgrammeYearId(Long programmeYearId, Pageable pageable);

    @EntityGraph(attributePaths = {"mentor", "mentee", "programmeYear"})
    List<Match> findAllByProgrammeYearId(Long programmeYearId);

    @EntityGraph(attributePaths = {"mentor", "mentee", "programmeYear"})
    @Query("SELECT m FROM Match m WHERE m.mentor.id = :participantId OR m.mentee.id = :participantId")
    List<Match> findByParticipantId(@Param("participantId") Long participantId);

    @EntityGraph(attributePaths = {"mentor", "mentee", "programmeYear"})
    @Query("SELECT m FROM Match m WHERE (m.mentor.id = :id OR m.mentee.id = :id) AND m.programmeYear.id = :programmeYearId")
    Optional<Match> findByParticipantAndProgrammeYear(@Param("id") Long participantId, @Param("programmeYearId") Long programmeYearId);

    @Query("""
                SELECT new uk.bovykina.matching_guru.dto.match.CoordinatorMatchDto(
                    m.id,
                    me.id,
                    CONCAT(me.user.firstName, ' ', me.user.lastName),
                    me.user.email,
                    mt.id,
                    CONCAT(mt.user.firstName, ' ', mt.user.lastName),
                    mt.user.email,
                    m.compatibilityScore,
                    m.status
                )
                FROM Match m
                JOIN m.mentor me
                JOIN m.mentee mt
                WHERE m.programmeYear.id = :programmeYearId
                  AND (:status IS NULL OR m.status = :status)
                  AND (
                    LOWER(me.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                    LOWER(me.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                    LOWER(mt.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                    LOWER(mt.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                    LOWER(me.user.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
                    LOWER(mt.user.email) LIKE LOWER(CONCAT('%', :query, '%'))
                  )
            """)
    Page<CoordinatorMatchDto> searchMatchSummaries(
            @Param("programmeYearId") Long programmeYearId,
            @Param("query") String query,
            @Param("status") MatchStatus status,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM Match m WHERE m.programmeYear.id = :programmeYearId")
    void deleteByProgrammeYearId(@Param("programmeYearId") Long programmeYearId);

    boolean existsByMentorIdAndMenteeIdAndStatusIn(Long mentorId, Long menteeId, List<MatchStatus> statuses);
}
