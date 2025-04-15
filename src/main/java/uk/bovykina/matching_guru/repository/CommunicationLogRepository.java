package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.CommunicationLog;

import java.util.List;
import java.util.Optional;

public interface CommunicationLogRepository extends JpaRepository<CommunicationLog, Long> {
    List<CommunicationLog> findByMatchId(Long matchId);
    Optional<CommunicationLog> findTopByMatchIdOrderByTimestampDesc(Long matchId);
    List<CommunicationLog> findByMatch_ProgrammeYearId(Long programmeYearId);


}
