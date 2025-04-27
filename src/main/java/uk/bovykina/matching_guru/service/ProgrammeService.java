package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.mapper.ProgrammeMapper;
import uk.bovykina.matching_guru.repository.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final OrganisationRepository organisationRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ProgrammeMapper programmeMapper;

    /**
     * Creates a new programme and links it to an existing organisation.
     */
    public ProgrammeDto createProgramme(ProgrammeCreateDto dto) {
        log.info("Creating programme for organisation ID {}", dto.getOrganisationId());

        Organisation organisation = organisationRepository.findById(dto.getOrganisationId())
                .orElseThrow(() -> {
                    log.error("Organisation not found with ID: {}", dto.getOrganisationId());
                    return new IllegalArgumentException("Organisation not found");
                });

        Set<CourseGroup> courseGroups = courseGroupRepository.findAllById(dto.getCourseGroupIds())
                .stream().collect(Collectors.toSet());

        Programme programme = new Programme();
        programme.setName(dto.getName());
        programme.setDescription(dto.getDescription());
        programme.setOrganisation(organisation);
        programme.setEligibleCourseGroups(courseGroups);

        Programme saved = programmeRepository.save(programme);
        int participants = participantRepository.countDistinctParticipantsByProgrammeId(saved.getId());

        log.info("Programme created with ID: {}", saved.getId());
        return programmeMapper.toDto(saved, participants);
    }

    /**
     * Updates an existing programme's name and description.
     */
    public ProgrammeDto updateProgramme(Long id, ProgrammeUpdateDto dto) {
        log.info("Updating programme ID {}", id);

        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Programme not found with ID: {}", id);
                    return new IllegalArgumentException("Programme not found");
                });

        programme.setName(dto.getName());
        programme.setDescription(dto.getDescription());

        Programme updated = programmeRepository.save(programme);
        int participants = participantRepository.countDistinctParticipantsByProgrammeId(updated.getId());

        log.info("Programme updated: ID {}", updated.getId());
        return programmeMapper.toDto(updated, participants);
    }

    /**
     * Deletes a programme by its ID.
     */
    public void deleteProgramme(Long id) {
        if (!programmeRepository.existsById(id)) {
            log.error("Programme not found with ID: {}", id);
            throw new IllegalArgumentException("Programme not found");
        }
        programmeRepository.deleteById(id);
        log.info("Deleted programme with ID: {}", id);
    }

    /**
     * Retrieves all programmes with participant counts.
     */
    @Transactional(readOnly = true)
    public List<ProgrammeDto> getAllProgrammes() {
        log.info("Fetching all programmes");
        return programmeRepository.findAll().stream()
                .map(p -> programmeMapper.toDto(p, participantRepository.countDistinctParticipantsByProgrammeId(p.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all programmes associated with a specific organisation.
     */
    @Transactional(readOnly = true)
    public List<ProgrammeDto> getProgrammesByOrganisation(Long organisationId) {
        log.info("Fetching programmes for organisation ID {}", organisationId);
        return programmeRepository.findByOrganisationId(organisationId).stream()
                .map(p -> programmeMapper.toDto(p, participantRepository.countDistinctParticipantsByProgrammeId(p.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves active programmes for a given organisation.
     */
    @Transactional(readOnly = true)
    public List<ProgrammeDto> getActiveProgrammesByOrganisation(Long organisationId) {
        log.info("Fetching active programmes for organisation ID {}", organisationId);
        return programmeRepository.findActiveProgrammesByOrganisationId(organisationId).stream()
                .map(p -> programmeMapper.toDto(p, participantRepository.countDistinctParticipantsByProgrammeId(p.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a programme by its ID.
     */
    @Transactional(readOnly = true)
    public ProgrammeDto getProgrammeById(Long id) {
        log.info("Fetching programme by ID {}", id);
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Programme not found with ID: {}", id);
                    return new IllegalArgumentException("Programme not found");
                });

        int participants = participantRepository.countDistinctParticipantsByProgrammeId(programme.getId());
        return programmeMapper.toDto(programme, participants);
    }

    /**
     * Retrieves all programmes linked to a specific user.
     */
    @Transactional(readOnly = true)
    public List<ProgrammeDto> getProgrammesByUserId(Long userId) {
        log.info("Fetching programmes for user ID {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        List<Programme> programmes = programmeRepository.findProgrammesByUserId(userId);

        return programmes.stream()
                .map(p -> programmeMapper.toDto(p, participantRepository.countDistinctParticipantsByProgrammeId(p.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves both current and available programmes for a given user.
     */
    @Transactional(readOnly = true)
    public ProgrammeParticipantViewDto getMyAndAvailableProgrammes(Long userId) {
        log.info("Fetching my and available programmes for user ID {}", userId);
        List<ProgrammeDto> myProgrammes = getProgrammesByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        CourseGroup userCourseGroup = user.getCourse() != null ? user.getCourse().getGroup() : null;
        List<Programme> activeProgrammes = programmeRepository.findActiveProgrammesByOrganisationId(user.getOrganisation().getId());

        List<ProgrammeDto> availableProgrammes = activeProgrammes.stream()
                .filter(prog -> {
                    Set<CourseGroup> eligibleGroups = prog.getEligibleCourseGroups();
                    return eligibleGroups == null || eligibleGroups.isEmpty() ||
                            (userCourseGroup != null && eligibleGroups.contains(userCourseGroup));
                })
                .filter(prog -> myProgrammes.stream().noneMatch(mp -> mp.getId().equals(prog.getId())))
                .map(p -> programmeMapper.toDto(p, participantRepository.countDistinctParticipantsByProgrammeId(p.getId())))
                .toList();

        return new ProgrammeParticipantViewDto(myProgrammes, availableProgrammes);
    }
}
