package uk.bovykina.matching_guru.dto.programme;


import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;

@Data
public class ProgrammeYearResponseDto {
    private Long id;
    private Long programmeId;
    private String programmeName;
    private String academicYear;
    private boolean isActive;
    private String joinCode;
    private String customSettings;
    private AlgorithmType preferredAlgorithm;
}