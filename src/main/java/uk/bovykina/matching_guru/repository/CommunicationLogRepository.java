package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.CommunicationLog;

import java.util.List;
import java.util.Optional;

public interface CommunicationLogRepository extends JpaRepository<CommunicationLog, Long> {
    List<CommunicationLog> findByMatchId(Long matchId);

    Optional<CommunicationLog> findTopByMatchIdOrderByTimestampDesc(Long matchId);

    List<CommunicationLog> findByMatch_ProgrammeYearId(Long programmeYearId);

    @Query("""
                SELECT m.programmeYear.id, l.type, COUNT(l)
                FROM CommunicationLog l
                JOIN l.match m
                WHERE m.programmeYear.id IN :yearIds
                GROUP BY m.programmeYear.id, l.type
            """)
    List<Object[]> countLogsByYearAndType(@Param("yearIds") List<Long> yearIds);

    @Query("""
                SELECT cl.match.programmeYear.id, cl.timestamp
                FROM CommunicationLog cl
                WHERE cl.match.programmeYear.id IN :yearIds
            """)
    List<Object[]> findLogTimestampsByProgrammeYearIds(@Param("yearIds") List<Long> yearIds);

    @Query(
            value = """
                    SELECT
                      CONCAT(EXTRACT(YEAR FROM cl.timestamp)::TEXT,
                             '-W',
                             LPAD(EXTRACT(WEEK FROM cl.timestamp)::TEXT, 2, '0'))   AS week,
                      COUNT(*)                                                     AS interactions
                    FROM communication_log cl
                    JOIN matches m ON m.id = cl.match_id
                    WHERE m.programme_year_id = :yearId
                    GROUP BY
                      EXTRACT(YEAR FROM cl.timestamp),
                      EXTRACT(WEEK FROM cl.timestamp)
                    ORDER BY
                      EXTRACT(YEAR FROM cl.timestamp),
                      EXTRACT(WEEK FROM cl.timestamp)
                    """,
            nativeQuery = true)
    List<Object[]> findWeeklyEngagementByYear(@Param("yearId") Long yearId);
}
