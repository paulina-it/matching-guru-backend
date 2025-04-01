package uk.bovykina.matching_guru.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT COUNT(m) FROM Match m WHERE m.programmeYear.id = :programmeYearId AND m.status = 'PENDING'")
    int countPendingMatches(@Param("programmeYearId") Long programmeYearId);

    @EntityGraph(attributePaths = {"mentor", "mentee", "programmeYear"})
    Page<Match> findByProgrammeYearId(Long programmeYearId, Pageable pageable);

    @Query("""
                SELECT m FROM Match m 
                JOIN FETCH m.mentor me 
                JOIN FETCH m.mentee mt 
                WHERE m.programmeYear.id = :programmeYearId 
                AND (
                    LOWER(me.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) 
                    OR LOWER(me.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) 
                    OR LOWER(mt.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) 
                    OR LOWER(mt.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) 
                    OR LOWER(me.user.email) LIKE LOWER(CONCAT('%', :query, '%')) 
                    OR LOWER(mt.user.email) LIKE LOWER(CONCAT('%', :query, '%'))
                )
                AND (:status IS NULL OR m.status = :status)
            """)
    Page<Match> searchMatches(@Param("programmeYearId") Long programmeYearId,
                              @Param("query") String query,
                              @Param("status") MatchStatus status,
                              Pageable pageable);


    @Query("SELECT m FROM Match m JOIN FETCH m.mentor JOIN FETCH m.mentee WHERE m.programmeYear.id = :programmeYearId")
    Page<Match> fetchMatchesWithParticipants(Long programmeYearId, Pageable pageable);

    @Query("SELECT m FROM Match m WHERE m.mentor.id = :participantId OR m.mentee.id = :participantId")
    List<Match> findByMentorIdOrMenteeId(@Param("participantId") Long participantId);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.programmeYear.id = :programmeYearId")
    boolean existsByProgrammeYearId(@Param("programmeYearId") Long programmeYearId);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.mentor.id = :mentorId AND m.mentee.id = :menteeId")
    boolean existsByMentorIdAndMenteeId(@Param("mentorId") Long mentorId, @Param("menteeId") Long menteeId);

}
