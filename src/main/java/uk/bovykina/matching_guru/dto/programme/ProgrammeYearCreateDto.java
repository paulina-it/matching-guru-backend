package uk.bovykina.matching_guru.dto.programme;


import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;

@Data
public class ProgrammeYearCreateDto {
    private Long programmeId;
    private String academicYear;
    private boolean isActive;
    private String customSettings;
    private AlgorithmType preferredAlgorithm;
}