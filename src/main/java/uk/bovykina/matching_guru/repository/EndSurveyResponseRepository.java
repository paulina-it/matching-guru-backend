package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.EndSurveyResponse;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;

import java.util.List;

public interface EndSurveyResponseRepository extends JpaRepository<EndSurveyResponse, Long> {
    List<EndSurveyResponse> findByParticipantInProgrammeId(Long participantId);
    boolean existsByParticipantInProgramme(ParticipantInProgrammeYear participant);
    List<EndSurveyResponse> findByProgrammeYearId(Long programmeYearId);

}
