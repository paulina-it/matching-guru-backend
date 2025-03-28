package uk.bovykina.matching_guru.dto.programme;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProgrammeYearSummaryDto {
    private Long id;
    private Long programmeId;
    private String name;
    private boolean isActive;
    private int participantsCount;
    private int matchesCount;
}
