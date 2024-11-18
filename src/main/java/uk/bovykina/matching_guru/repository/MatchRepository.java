package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.Match;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByMentorProgrammeYearId(Long programmeYearId);
}
