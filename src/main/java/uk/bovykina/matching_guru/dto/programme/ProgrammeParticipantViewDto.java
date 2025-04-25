package uk.bovykina.matching_guru.dto.programme;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgrammeParticipantViewDto {
    private List<ProgrammeDto> myProgrammes;
    private List<ProgrammeDto> availableProgrammes;
}
