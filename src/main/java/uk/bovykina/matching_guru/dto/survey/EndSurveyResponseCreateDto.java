package uk.bovykina.matching_guru.dto.survey;

import lombok.Data;

@Data
public class EndSurveyResponseCreateDto {
    private Long participantId;
    private String responseData;
}