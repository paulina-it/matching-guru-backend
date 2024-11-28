package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.programme.ProgrammeCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeUpdateDto;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final OrganisationRepository organisationRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final ParticipantRepository participantRepository;

    public ProgrammeDto createProgramme(ProgrammeCreateDto programmeCreateDto) {
        // Fetch organisation by ID
        Organisation organisation = organisationRepository.findById(programmeCreateDto.getOrganisationId())
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with ID: " + programmeCreateDto.getOrganisationId()));

        // Fetch course groups by IDs
        Set<CourseGroup> courseGroups = courseGroupRepository.findAllById(programmeCreateDto.getCourseGroupIds())
                .stream()
                .collect(Collectors.toSet());

        // Create a new programme
        Programme programme = new Programme();
        programme.setName(programmeCreateDto.getName());
        programme.setDescription(programmeCreateDto.getDescription());
        programme.setOrganisation(organisation);
        programme.setEligibleCourseGroups(courseGroups);

        Programme savedProgramme = programmeRepository.save(programme);

        // Convert entity to DTO
        return toProgrammeDto(savedProgramme);
    }

    public ProgrammeDto updateProgramme(Long id, ProgrammeUpdateDto updateDto) {
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found with ID: " + id));

        programme.setName(updateDto.getName());
        programme.setDescription(updateDto.getDescription());

        return toProgrammeDto(programmeRepository.save(programme));
    }

    public void deleteProgramme(Long id) {
        if (!programmeRepository.existsById(id)) {
            throw new IllegalArgumentException("Programme not found with ID: " + id);
        }
        programmeRepository.deleteById(id);
    }

    public List<ProgrammeDto> getAllProgrammes() {
        return programmeRepository.findAll()
                .stream()
                .map(this::toProgrammeDto)
                .collect(Collectors.toList());
    }

    public List<ProgrammeDto> getProgrammesByOrganisation(Long organisationId) {
        return programmeRepository.findByOrganisationId(organisationId)
                .stream()
                .map(this::toProgrammeDto)
                .collect(Collectors.toList());
    }

    public ProgrammeDto getProgrammeById(Long id) {
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found with ID: " + id));

        return toProgrammeDto(programme);
    }

    private ProgrammeDto toProgrammeDto(Programme programme) {
        ProgrammeDto dto = new ProgrammeDto();
        dto.setId(programme.getId());
        dto.setName(programme.getName());
        dto.setDescription(programme.getDescription());
        dto.setOrganisationId(programme.getOrganisation().getId());
        dto.setCourseGroupIds(
                programme.getEligibleCourseGroups().stream()
                        .map(CourseGroup::getId)
                        .collect(Collectors.toSet())
        );

        Integer participants = participantRepository.countParticipantsByProgrammeId(programme.getId());
        dto.setParticipants(participants);

        return dto;
    }
}
