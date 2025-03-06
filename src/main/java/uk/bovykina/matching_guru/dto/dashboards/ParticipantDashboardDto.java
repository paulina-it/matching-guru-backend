package uk.bovykina.matching_guru.dto.dashboards;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.dto.survey.EndSurveyResponseDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearResponseDto;
//import uk.bovykina.matching_guru.dto.meetings.MeetingDto;

import java.util.List;

@Getter
@Setter
public class ParticipantDashboardDto {
    private List<EndSurveyResponseDto> feedback;
    private List<ParticipantProgrammeParticipationDto> participations;
//    private List<MeetingDto> meetings; // Added for scheduled mentorship meetings

    @Getter
    @Setter
    public static class ParticipantProgrammeParticipationDto {
        private ProgrammeYearResponseDto programmeYear;
        private Boolean isMentor;
        private Boolean isMentee;
    }
}
