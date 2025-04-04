package uk.bovykina.matching_guru.dto.participant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackSubmissionDto {
    private Long participantId;
    private Long programmeYearId;
    private String code;
    private String responseData;
}