package uk.bovykina.matching_guru.mapper;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.organisation.*;
import uk.bovykina.matching_guru.entity.Organisation;

@Component
public class OrganisationMapper {

    public Organisation toEntity(OrganisationCreateDto dto) {
        Organisation organisation = new Organisation();
        organisation.setName(dto.getName());
        organisation.setDescription(dto.getDescription());
        organisation.setLogoUrl(dto.getLogoUrl());
        return organisation;
    }

    public OrganisationDto toDto(Organisation organisation) {
        OrganisationDto dto = new OrganisationDto();
        dto.setId(organisation.getId());
        dto.setName(organisation.getName());
        dto.setJoinCode(organisation.getJoinCode());
        dto.setDescription(organisation.getDescription());
        dto.setLogoUrl(organisation.getLogoUrl());
        return dto;
    }

    public void updateFromDto(Organisation organisation, OrganisationUpdateDto dto) {
        if (dto.getName() != null) organisation.setName(dto.getName());
        if (dto.getDescription() != null) organisation.setDescription(dto.getDescription());
    }
}
