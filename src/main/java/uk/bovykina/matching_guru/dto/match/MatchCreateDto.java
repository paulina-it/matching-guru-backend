package uk.bovykina.matching_guru.dto.match;

import lombok.Data;

@Data
public class MatchCreateDto {
    private Long mentorId;
    private Long menteeId;
}
