package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.programme.ProgrammeCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeParticipantViewDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeUpdateDto;
import uk.bovykina.matching_guru.dto.user.UserResponseDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;
import uk.bovykina.matching_guru.repository.UserRepository;

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
    private final UserRepository userRepository;

    public ProgrammeDto createProgramme(ProgrammeCreateDto programmeCreateDto) {
        Organisation organisation = organisationRepository.findById(programmeCreateDto.getOrganisationId())
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with ID: " + programmeCreateDto.getOrganisationId()));

        Set<CourseGroup> courseGroups = courseGroupRepository.findAllById(programmeCreateDto.getCourseGroupIds())
                .stream()
                .collect(Collectors.toSet());

        Programme programme = new Programme();
        programme.setName(programmeCreateDto.getName());
        programme.setDescription(programmeCreateDto.getDescription());
        programme.setOrganisation(organisation);
        programme.setEligibleCourseGroups(courseGroups);

        Programme savedProgramme = programmeRepository.save(programme);

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

    public List<ProgrammeDto> getActiveProgrammesByOrganisation(Long organisationId) {
        return programmeRepository.findActiveProgrammesByOrganisationId(organisationId)
                .stream()
                .map(this::toProgrammeDto)
                .collect(Collectors.toList());
    }

    public ProgrammeDto getProgrammeById(Long id) {
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found with ID: " + id));

        return toProgrammeDto(programme);
    }

    public List<ProgrammeDto> getProgrammesByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Programme> programmes = programmeRepository.findProgrammesByUserId(userId);

        return programmes.stream()
                .map(this::toProgrammeDto)
                .collect(Collectors.toList());
    }

    public ProgrammeParticipantViewDto getMyAndAvailableProgrammes(Long userId) {
        List<ProgrammeDto> myProgrammes = getProgrammesByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Course userCourse = user.getCourse();
        CourseGroup userCourseGroup = (userCourse != null) ? userCourse.getGroup() : null;

        List<Programme> activeProgrammes = programmeRepository.findActiveProgrammesByOrganisationId(user.getOrganisation().getId());

        List<ProgrammeDto> availableProgrammes = activeProgrammes.stream()
                .filter(prog -> {
                    Set<CourseGroup> eligibleGroups = prog.getEligibleCourseGroups();
                    if (eligibleGroups == null || eligibleGroups.isEmpty()) {
                        return true;
                    }
                    return userCourseGroup != null && eligibleGroups.contains(userCourseGroup);
                })
                .filter(prog -> myProgrammes.stream().noneMatch(myProg -> myProg.getId().equals(prog.getId())))
                .map(this::toProgrammeDto)
                .toList();

        return new ProgrammeParticipantViewDto(myProgrammes, availableProgrammes);
    }

    private ProgrammeDto toProgrammeDto(Programme programme) {
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

        Integer participants = participantRepository.countDistinctParticipantsByProgrammeId(programme.getId());
        dto.setParticipants(participants);

        return dto;
    }

}
