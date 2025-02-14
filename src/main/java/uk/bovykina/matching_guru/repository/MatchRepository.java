package uk.bovykina.matching_guru.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.bovykina.matching_guru.entity.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @EntityGraph(attributePaths = {"mentor", "mentee", "programmeYear"})
    Page<Match> findByProgrammeYearId(Long programmeYearId, Pageable pageable);

    @Query("SELECT m FROM Match m JOIN FETCH m.mentor JOIN FETCH m.mentee WHERE m.programmeYear.id = :programmeYearId")
    Page<Match> fetchMatchesWithParticipants(Long programmeYearId, Pageable pageable);
}
