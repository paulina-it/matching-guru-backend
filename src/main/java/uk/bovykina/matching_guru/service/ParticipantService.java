package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantCreateDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantUpdateDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ProgrammeYearRepository programmeYearRepository;
    private final CourseRepository courseRepository;
    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final EndSurveyResponseRepository endSurveyResponseRepository;

    @Transactional
    public ParticipantResponseDto createParticipant(ParticipantCreateDto createDto) {
        log.info("Creating participant for User ID: {}", createDto.getUserId());

        User user = userRepository.findById(createDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProgrammeYear programmeYear = programmeYearRepository.findById(createDto.getProgrammeYearId())
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        log.info("Updating User with ID: {}", createDto.getUserId());
        updateUserFields(user, createDto);

        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setUser(user);
        participant.setProgrammeYear(programmeYear);
        participant.setRole(createDto.getRole());
        participant.setMenteesNumber(createDto.getMenteesNumber() != null ? createDto.getMenteesNumber() : 0);
        participant.setIsMatched(createDto.getIsMatched() != null ? createDto.getIsMatched() : false);
        participant.setAcademicStage(createDto.getAcademicStage());
        participant.setHadPlacement(createDto.getHadPlacement());
        participant.setPlacementDescription(createDto.getPlacementDescription());
        participant.setMotivation(createDto.getMotivation());
        participant.setIsReturningParticipant(createDto.getIsReturningParticipant());
        participant.setAvailableDays(createDto.getAvailableDays());
        participant.setTimeRange(createDto.getTimeRange());
        participant.setMeetingsFrequency(createDto.getMeetingsFrequency());
        participant.setSkills(createDto.getSkills());

        ParticipantInProgrammeYear savedParticipant = participantRepository.save(participant);
        log.info("Participant created successfully for User ID: {}", createDto.getUserId());

        return toParticipantResponseDto(savedParticipant);
    }

    public ParticipantResponseDto getParticipant(Long id) {
        return participantRepository.findById(id)
                .map(this::toParticipantResponseDto)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
    }

    public Object getParticipantInfoByUserId(Long userId) {
        log.info("Fetching participant info for ID: {}", userId);

        ParticipantInProgrammeYear participant = participantRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Participant with ID {} not found", userId);
                    return new IllegalArgumentException("Participant not found");
                });

        List<Match> matches = matchRepository.findByMentorIdOrMenteeId(participant.getId());
        log.info("🔍 Found {} match(es) for participant ID {}: {}", matches.size(), participant.getId(), matches);

        if (!matches.isEmpty()) {
            List<DetailedMatchResponseDto> matchDtos = matches.stream()
                    .map(match -> matchService.getDetailedMatchById(match.getId()))
                    .toList();

            log.info("✅ Returning {} detailed match DTOs for participant ID {}", matchDtos.size(), participant.getId());
            return matchDtos;
        } else {
            log.info("❌ No match found for participant ID: {}, returning ParticipantResponseDto", participant.getId());
            return toParticipantResponseDto(participant);
        }
    }


    public ParticipantResponseDto getParticipantByUserId(Long userId) {
        return participantRepository.findByUserId(userId)
                .map(this::toParticipantResponseDto)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
    }

    public List<ParticipantResponseDto> getAllParticipants() {
        return participantRepository.findAll().stream()
                .map(this::toParticipantResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipantResponseDto updateParticipant(Long id, ParticipantUpdateDto updateDto) {
        ParticipantInProgrammeYear participant = participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (updateDto.getRole() != null) participant.setRole(updateDto.getRole());
        if (updateDto.getMenteesNumber() != null) participant.setMenteesNumber(updateDto.getMenteesNumber());
        if (updateDto.getIsMatched() != null) participant.setIsMatched(updateDto.getIsMatched());
        if (updateDto.getAcademicStage() != null) participant.setAcademicStage(updateDto.getAcademicStage());
        if (updateDto.getHadPlacement() != null) participant.setHadPlacement(updateDto.getHadPlacement());
        if (updateDto.getPlacementDescription() != null)
            participant.setPlacementDescription(updateDto.getPlacementDescription());
        if (updateDto.getMotivation() != null) participant.setMotivation(updateDto.getMotivation());
        if (updateDto.getIsReturningParticipant() != null)
            participant.setIsReturningParticipant(updateDto.getIsReturningParticipant());
        if (updateDto.getAvailableDays() != null) participant.setAvailableDays(updateDto.getAvailableDays());
        if (updateDto.getTimeRange() != null) participant.setTimeRange(updateDto.getTimeRange());
        if (updateDto.getMeetingsFrequency() != null)
            participant.setMeetingsFrequency(updateDto.getMeetingsFrequency());
        if (updateDto.getSkills() != null) participant.setSkills(updateDto.getSkills());

        ParticipantInProgrammeYear updatedParticipant = participantRepository.save(participant);
        return toParticipantResponseDto(updatedParticipant);
    }

    public Page<ParticipantResponseDto> getParticipantsByProgrammeYearId(
            Long programmeYearId,
            int page,
            int size,
            String search,
            String sortBy,
            String sortOrder,
            String roleFilter
    ) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(programmeYearId)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ParticipantInProgrammeYear> participantPage;

        ParticipantRole roleEnum = null;
        if (roleFilter != null && !roleFilter.isBlank()) {
            try {
                roleEnum = ParticipantRole.valueOf(roleFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid participant role: " + roleFilter);
            }
        }

        if (search != null && !search.isBlank() && roleEnum != null) {
            participantPage = participantRepository.searchByProgrammeYearAndRoleAndUserNameOrEmail(
                    programmeYear, roleEnum, search, pageable);
        } else if (search != null && !search.isBlank()) {
            participantPage = participantRepository.searchByProgrammeYearAndUserNameOrEmail(
                    programmeYear, search, pageable);
        } else if (roleEnum != null) {
            participantPage = participantRepository.findByProgrammeYearAndRole(
                    programmeYear, roleEnum, pageable);
        } else {
            participantPage = participantRepository.findAllByProgrammeYear(programmeYear, pageable);
        }

        return participantPage.map(this::toParticipantResponseDto);
    }

    public List<ParticipantResponseDto> getDetailedParticipantsByProgrammeYearId(Long programmeYearId) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(programmeYearId)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        List<ParticipantInProgrammeYear> participants = participantRepository.findByProgrammeYearId(programmeYearId);

        return participants.stream()
                .map(this::toParticipantResponseDto)
                .collect(Collectors.toList());
    }


    private ParticipantResponseDto toParticipantResponseDto(ParticipantInProgrammeYear participant) {
        ParticipantResponseDto dto = new ParticipantResponseDto();
        dto.setId(participant.getId());

        User user = participant.getUser();
        dto.setUserId(user.getId());
        dto.setUserName(user.getFirstName() + " " + user.getLastName());
        dto.setUserEmail(user.getEmail());
        dto.setUserGender(user.getGender());
        dto.setUserHomeCountry(user.getHomeCountry());
        dto.setUserNationality(user.getNationality());
        dto.setUserPersonalityType(user.getPersonalityType());
        dto.setUserCourseId(user.getCourse() != null ? user.getCourse().getId() : null);
        dto.setUserCourseName(user.getCourse().getName());
        dto.setUserLivingArrangement(user.getLivingArrangement());
        dto.setUserDbsCertificate(user.getDbsCertificate());
        dto.setUserDisability(user.getDisability());
        dto.setUserAgeGroup(user.getAgeGroup());

        ProgrammeYear programmeYear = participant.getProgrammeYear();
        dto.setProgrammeYearId(programmeYear.getId());
        dto.setProgrammeName(programmeYear.getProgramme().getName());
        dto.setAcademicYear(programmeYear.getAcademicYear());

        dto.setRole(participant.getRole());
        dto.setMenteesNumber(participant.getMenteesNumber());
        dto.setIsMatched(participant.getIsMatched());
        dto.setAcademicStage(participant.getAcademicStage());
        dto.setHadPlacement(participant.getHadPlacement());
        dto.setPlacementDescription(participant.getPlacementDescription());
        dto.setMotivation(participant.getMotivation());
        dto.setIsReturningParticipant(participant.getIsReturningParticipant());
        dto.setAvailableDays(participant.getAvailableDays());
        dto.setTimeRange(participant.getTimeRange());
        dto.setMeetingsFrequency(participant.getMeetingsFrequency());
        dto.setSkills(participant.getSkills());
        boolean hasFeedback = endSurveyResponseRepository.existsByParticipantInProgramme(participant);
        dto.setHasSubmittedFeedback(hasFeedback);

        return dto;
    }

    public Object getParticipantInfoByUserIdAndProgrammeYearId(Long userId, Long programmeYearId) {
        log.info("Fetching participant info for userId={} and programmeYearId={}", userId, programmeYearId);

        ParticipantInProgrammeYear participant = participantRepository
                .findByUserIdAndProgrammeYearId(userId, programmeYearId)
                .orElseThrow(() -> {
                    log.error("Participant not found for userId={} and programmeYearId={}", userId, programmeYearId);
                    return new IllegalArgumentException("Participant not found");
                });

        List<Match> matches = matchRepository.findByMentorIdOrMenteeId(participant.getId());
        log.info("🔍 Found {} potential match(es) for participantId={}", matches.size(), participant.getId());

        if (!matches.isEmpty()) {
            log.info("✅ Match(es) found in programmeYearId={}, returning detailed match DTOs", programmeYearId);
            return matches.stream()
                    .map(match -> matchService.getDetailedMatchById(match.getId()))
                    .toList();
        } else {
            log.info("❌ No match found, returning participant response DTO");
            return toParticipantResponseDto(participant);
        }
    }


    private void updateUserFields(User user, ParticipantCreateDto createDto) {
        boolean updated = false;

        if (createDto.getGender() != null && user.getGender() == null) {
            log.info("Updating User ID {} Gender: {}", user.getId(), createDto.getGender());
            user.setGender(createDto.getGender());
            updated = true;
        }

        if (createDto.getAgeGroup() != null && user.getAgeGroup() == null) {
            log.info("Updating User ID {} Age Group: {}", user.getId(), createDto.getAgeGroup());
            user.setAgeGroup(createDto.getAgeGroup());
            updated = true;
        }

        if (createDto.getHomeCountry() != null && user.getHomeCountry() == null) {
            log.info("Updating User ID {} Home Country: {}", user.getId(), createDto.getHomeCountry());
            user.setHomeCountry(createDto.getHomeCountry());
            updated = true;
        }

        if (createDto.getNationality() != null && user.getNationality() == null) {
            log.info("Updating User ID {} Nationality: {}", user.getId(), createDto.getNationality());
            user.setNationality(createDto.getNationality());
            updated = true;
        }

        if (createDto.getPersonalityType() != null &&
                !createDto.getPersonalityType().equals(user.getPersonalityType())) {
            log.info("Updating User ID {} Personality Type: {}", user.getId(), createDto.getPersonalityType());
            user.setPersonalityType(createDto.getPersonalityType());
            updated = true;
        }

        if (createDto.getCourseId() != null && (user.getCourse() == null || !user.getCourse().getId().equals(createDto.getCourseId()))) {
            Course course = courseRepository.findById(createDto.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + createDto.getCourseId()));
            log.info("Updating User ID {} Course: {}", user.getId(), course.getName());
            user.setCourse(course);
            updated = true;
        }

        if (createDto.getLivingArrangement() != null && user.getLivingArrangement() == null) {
            log.info("Updating User ID {} Living Arrangement: {}", user.getId(), createDto.getLivingArrangement());
            user.setLivingArrangement(createDto.getLivingArrangement());
            updated = true;
        }

        if (createDto.getDisability() != null && user.getDisability() == null) {
            log.info("Updating User ID {} Disability: {}", user.getId(), createDto.getDisability());
            user.setDisability(createDto.getDisability());
            updated = true;
        }

        if (createDto.getDbsCertificate() != null && user.getDbsCertificate() == null) {
            log.info("Updating User ID {} DBS Certificate: {}", user.getId(), createDto.getDbsCertificate());
            user.setDbsCertificate(createDto.getDbsCertificate());
            updated = true;
        }

        if (updated) {
            log.info("Saving updated user ID {}", user.getId());
            userRepository.save(user);
        }
    }

}
