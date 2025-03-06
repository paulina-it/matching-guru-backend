package uk.bovykina.matching_guru.dto.survey;

import lombok.Data;

@Data
public class EndSurveyResponseCreateDto {
    private Long participantId;
    private Long matchId;
    private String responseData;
}