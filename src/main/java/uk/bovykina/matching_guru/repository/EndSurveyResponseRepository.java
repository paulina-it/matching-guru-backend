package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.EndSurveyResponse;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;

import java.util.List;

public interface EndSurveyResponseRepository extends JpaRepository<EndSurveyResponse, Long> {
    List<EndSurveyResponse> findByParticipantInProgrammeId(Long participantId);
    boolean existsByParticipantInProgramme(ParticipantInProgrammeYear participant);
    List<EndSurveyResponse> findByProgrammeYearId(Long programmeYearId);
    @Query("""
    SELECT e.programmeYear.id, COUNT(e.id)
    FROM EndSurveyResponse e
    WHERE e.programmeYear.id IN :yearIds
    GROUP BY e.programmeYear.id
""")
    List<Object[]> countSurveysByProgrammeYear(@Param("yearIds") List<Long> yearIds);

}
