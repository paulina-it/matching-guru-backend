package uk.bovykina.matching_guru.dto.stats.demographics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProgrammeDemoDto {
    private String programmeName;
    private List<ProgrammeYearDemoDto> years;
}