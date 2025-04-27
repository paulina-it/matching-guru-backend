package uk.bovykina.matching_guru.mapper;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.programme.EligibilityCreateDto;
import uk.bovykina.matching_guru.dto.programme.EligibilityDto;
import uk.bovykina.matching_guru.entity.Eligibility;
import uk.bovykina.matching_guru.entity.Programme;

@Component
public class EligibilityMapper {

    public EligibilityDto toDto(Eligibility eligibility) {
        EligibilityDto dto = new EligibilityDto();
        dto.setId(eligibility.getId());
        dto.setProgrammeId(eligibility.getProgramme().getId());
        dto.setAcademicStage(eligibility.getAcademicStage());
        dto.setRole(eligibility.getRole());
        return dto;
    }

    public Eligibility toEntity(EligibilityCreateDto dto, Programme programme) {
        Eligibility eligibility = new Eligibility();
        eligibility.setProgramme(programme);
        eligibility.setAcademicStage(dto.getAcademicStage());
        eligibility.setRole(dto.getRole());
        return eligibility;
    }
}
