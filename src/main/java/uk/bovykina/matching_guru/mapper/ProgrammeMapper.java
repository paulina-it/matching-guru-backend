package uk.bovykina.matching_guru.mapper;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.programme.ProgrammeDto;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Programme;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProgrammeMapper {

    public ProgrammeDto toDto(Programme programme, int participants) {
        ProgrammeDto dto = new ProgrammeDto();
        dto.setId(programme.getId());
        dto.setName(programme.getName());
        dto.setDescription(programme.getDescription());
        dto.setOrganisationId(programme.getOrganisation().getId());

        Set<CourseGroup> eligibleGroups = programme.getEligibleCourseGroups();
        dto.setCourseGroupIds(
                eligibleGroups.stream()
                        .map(CourseGroup::getId)
                        .collect(Collectors.toSet())
        );
        dto.setCourseGroups(
                eligibleGroups.stream()
                        .collect(Collectors.toMap(
                                CourseGroup::getId,
                                CourseGroup::getName
                        ))
        );
        dto.setParticipants(participants);
        return dto;
    }
}
