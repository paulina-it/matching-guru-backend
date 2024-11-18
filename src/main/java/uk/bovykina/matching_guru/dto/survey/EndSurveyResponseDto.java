package uk.bovykina.matching_guru.dto.survey;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndSurveyResponseDto {
    private Long id;
    private Long participantInProgrammeId;
    private String responseData;
    private LocalDateTime completedAt;
}