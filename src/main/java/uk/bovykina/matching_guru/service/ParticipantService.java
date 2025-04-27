package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantCreateDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantUpdateDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.mapper.ParticipantMapper;
import uk.bovykina.matching_guru.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ProgrammeYearRepository programmeYearRepository;
    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final ParticipantMapper participantMapper;

    /**
     * Creates a new participant in a programme year and updates user details if necessary.
     */
    @Transactional
    public ParticipantResponseDto createParticipant(ParticipantCreateDto createDto) {
        log.info("Creating participant for User ID: {}", createDto.getUserId());

        User user = userRepository.findById(createDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProgrammeYear programmeYear = programmeYearRepository.findById(createDto.getProgrammeYearId())
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        updateUserFields(user, createDto);

        ParticipantInProgrammeYear participant = participantMapper.toEntity(createDto, user, programmeYear);

        boolean wasUnmatchedInPast = participantRepository.findAllByUserId(user.getId()).stream()
                .filter(p -> !p.getProgrammeYear().getId().equals(programmeYear.getId()))
                .filter(p -> p.getProgrammeYear().getProgramme().getId().equals(programmeYear.getProgramme().getId()))
                .anyMatch(p -> Boolean.FALSE.equals(p.getIsMatched()));

        participant.setWasMatchedLastYear(!wasUnmatchedInPast);

        ParticipantInProgrammeYear saved = participantRepository.save(participant);
        log.info("Participant created with ID: {}", saved.getId());
        return participantMapper.toDto(saved);
    }

    /**
     * Fetches participant by ID.
     */
    public ParticipantResponseDto getParticipant(Long id) {
        log.debug("Fetching participant by ID: {}", id);
        return participantRepository.findById(id)
                .map(participantMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
    }

    /**
     * Fetches participant info or match details by user ID.
     */
    public Object getParticipantInfoByUserId(Long userId) {
        log.debug("Getting participant info for user ID: {}", userId);

        ParticipantInProgrammeYear participant = participantRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        List<Match> matches = matchRepository.findByParticipantId(participant.getId());

        if (!matches.isEmpty()) {
            log.info("Returning {} matches for participant ID {}", matches.size(), participant.getId());
            return matches.stream().map(m -> matchService.getDetailedMatchById(m.getId())).toList();
        }

        log.info("No matches found, returning participant DTO");
        return participantMapper.toDto(participant);
    }

    /**
     * Fetches participant DTO by user ID.
     */
    public ParticipantResponseDto getParticipantByUserId(Long userId) {
        return participantRepository.findByUserId(userId)
                .map(participantMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
    }

    /**
     * Returns a list of all participants.
     */
    public List<ParticipantResponseDto> getAllParticipants() {
        return participantRepository.findAll().stream()
                .map(participantMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing participant.
     */
    @Transactional
    public ParticipantResponseDto updateParticipant(Long id, ParticipantUpdateDto updateDto) {
        log.info("Updating participant ID: {}", id);
        ParticipantInProgrammeYear participant = participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        participantMapper.updateEntity(participant, updateDto);
        ParticipantInProgrammeYear updated = participantRepository.save(participant);
        log.info("Participant ID {} updated successfully", id);
        return participantMapper.toDto(updated);
    }

    /**
     * Retrieves paginated participant records for a given programme year, with filters.
     */
    public Page<ParticipantResponseDto> getParticipantsByProgrammeYearId(Long programmeYearId, int page, int size,
                                                                         String search, String sortBy,
                                                                         String sortOrder, String roleFilter) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(programmeYearId)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        ParticipantRole roleEnum = null;
        if (roleFilter != null && !roleFilter.isBlank()) {
            try {
                roleEnum = ParticipantRole.valueOf(roleFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid participant role: " + roleFilter);
            }
        }

        Page<ParticipantInProgrammeYear> participantPage;
        if (search != null && !search.isBlank() && roleEnum != null) {
            participantPage = participantRepository.searchByProgrammeYearAndRoleAndUserNameOrEmail(programmeYear, roleEnum, search, pageable);
        } else if (search != null && !search.isBlank()) {
            participantPage = participantRepository.searchByProgrammeYearAndUserNameOrEmail(programmeYear, search, pageable);
        } else if (roleEnum != null) {
            participantPage = participantRepository.findByProgrammeYearAndRole(programmeYear, roleEnum, pageable);
        } else {
            participantPage = participantRepository.findAllByProgrammeYear(programmeYear, pageable);
        }

        return participantPage.map(participantMapper::toDto);
    }

    /**
     * Returns all participants for a specific programme year.
     */
    public List<ParticipantResponseDto> getDetailedParticipantsByProgrammeYearId(Long programmeYearId) {
        programmeYearRepository.findById(programmeYearId)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        return participantRepository.findByProgrammeYearId(programmeYearId).stream()
                .map(participantMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns participant data or match details by user ID and programme year.
     */
    public Object getParticipantInfoByUserIdAndProgrammeYearId(Long userId, Long programmeYearId) {
        ParticipantInProgrammeYear participant = participantRepository.findByUserIdAndProgrammeYearId(userId, programmeYearId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        List<Match> matches = matchRepository.findByParticipantId(participant.getId());

        if (!matches.isEmpty()) {
            return matches.stream()
                    .map(match -> matchService.getDetailedMatchById(match.getId()))
                    .toList();
        }

        return participantMapper.toDto(participant);
    }

    /**
     * Updates basic user profile fields during participant creation if missing.
     */
    private void updateUserFields(User user, ParticipantCreateDto createDto) {
        boolean updated = participantMapper.updateUserFromCreateDto(user, createDto);
        if (updated) {
            log.info("Saving updated user ID {}", user.getId());
            userRepository.save(user);
        }
    }
}
