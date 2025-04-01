package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.*;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;

    public boolean doesMatchExist(Long mentorId, Long menteeId) {
        return matchRepository.existsByMentorIdAndMenteeId(mentorId, menteeId);
    }

    @Transactional
    public MatchResponseDto createMatch(MatchCreateDto matchCreateDto) {
        log.info("Creating a match between Mentor ID: {} and Mentee ID: {}", matchCreateDto.getMentorId(), matchCreateDto.getMenteeId());

        ParticipantInProgrammeYear mentor = participantRepository.findById(matchCreateDto.getMentorId())
                .orElseThrow(() -> {
                    log.error("Mentor with ID {} not found", matchCreateDto.getMentorId());
                    return new IllegalArgumentException("Mentor not found");
                });

        ParticipantInProgrammeYear mentee = participantRepository.findById(matchCreateDto.getMenteeId())
                .orElseThrow(() -> {
                    log.error("Mentee with ID {} not found", matchCreateDto.getMenteeId());
                    return new IllegalArgumentException("Mentee not found");
                });

        if (doesMatchExist(matchCreateDto.getMentorId(), matchCreateDto.getMenteeId())) {
            log.warn("⚠ Skipping duplicate match: Mentor {} → Mentee {}", matchCreateDto.getMentorId(), matchCreateDto.getMenteeId());
            return null;
        }

        Match match = new Match();
        match.setMentor(mentor);
        match.setMentee(mentee);
        match.setStatus(matchCreateDto.getStatus());
        match.setCompatibilityScore(matchCreateDto.getCompatibilityScore());
        match.setProgrammeYear(mentor.getProgrammeYear());

        Match savedMatch = matchRepository.save(match);
        log.info("Match created successfully with ID: {}", savedMatch.getId());

        return MatchMapper.toResponseDto(savedMatch);
    }

    @Transactional
    public MatchResponseDto updateMatchStatus(Long matchId, MatchStatusUpdateDto matchUpdateDto) {
        log.info("Updating status of match ID: {} to {}", matchId, matchUpdateDto.getStatus());

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match with ID {} not found", matchId);
                    return new IllegalArgumentException("Match not found");
                });

        match.setStatus(matchUpdateDto.getStatus());
        Match updatedMatch = matchRepository.save(match);

        log.info("Match ID: {} status updated successfully to {}", matchId, updatedMatch.getStatus());
        return MatchMapper.toResponseDto(updatedMatch);
    }

    public List<MatchResponseDto> getAllMatches() {
        log.info("Fetching all matches");
        return matchRepository.findAll().stream()
                .map(MatchMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public Page<MatchResponseDto> getMatchesByProgrammeYearId(Long programmeYearId, Pageable pageable) {
        return matchRepository.findByProgrammeYearId(programmeYearId, pageable)
                .map(MatchMapper::toResponseDto);
    }

    public MatchResponseDto getMatchById(Long matchId) {
        log.info("Fetching match details for ID: {}", matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match with ID {} not found", matchId);
                    return new IllegalArgumentException("Match not found");
                });

        return MatchMapper.toResponseDto(match);
    }

    public DetailedMatchResponseDto getDetailedMatchById(Long matchId) {
        log.info("Fetching detailed match information for ID: {}", matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match with ID {} not found", matchId);
                    return new IllegalArgumentException("Match not found");
                });

        log.info("Successfully retrieved detailed match information for ID: {}", matchId);
        return MatchMapper.toDetailedResponseDto(match);
    }

    public DetailedMatchResponseDto getDetailedMatchByParticipantId(Long participantId, Long programmeYearId) {
        log.info("Fetching detailed match for participant ID: {} in programme year: {}", participantId, programmeYearId);

        List<Match> matches = matchRepository.findByMentorIdOrMenteeId(participantId);

        Match match = matches.stream()
                .filter(m -> m.getProgrammeYear().getId().equals(programmeYearId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No match found for participant ID {} in programme year {}", participantId, programmeYearId);
                    return new IllegalArgumentException("No match found for this participant in the given programme year");
                });

        log.info("Successfully retrieved match for participant ID: {} in programme year {}", participantId, programmeYearId);
        return MatchMapper.toDetailedResponseDto(match);
    }

    @Transactional
    public void updateMatchStatus(List<Long> matchIds, MatchStatus status) {
        if (matchIds == null || matchIds.isEmpty()) {
            log.info("Updating status of all matches to {}", status);
            List<Match> matches = matchRepository.findAll();
            matches.forEach(match -> match.setStatus(status));
            matchRepository.saveAll(matches);
        } else {
            log.info("Updating status of matches with IDs {} to {}", matchIds, status);
            List<Match> matches = matchRepository.findAllById(matchIds);
            if (matches.isEmpty()) {
                throw new IllegalArgumentException("No matches found for provided IDs");
            }
            matches.forEach(match -> match.setStatus(status));
            matchRepository.saveAll(matches);
        }
    }

    public Page<MatchResponseDto> searchMatches(
            Long programmeYearId, String query, MatchStatus status, int page, int size, String sortBy, String sortOrder) {

        log.info("Fetching matches for ProgrammeYear: {}, Query: '{}', Status: {}, SortBy: {}, Order: {}",
                programmeYearId, query, status, sortBy, sortOrder);

        if (query == null || query.trim().isEmpty()) query = "%";

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Match> matches = matchRepository.searchMatches(programmeYearId, query, status, pageable);

        log.info("✅ Found {} matches for ProgrammeYear: {}, Query: '{}', Status: {}",
                matches.getTotalElements(), programmeYearId, query, status);

        return matches.map(MatchMapper::toResponseDto);
    }


    private static class MatchMapper {

        static MatchResponseDto toResponseDto(Match match) {
            return new MatchResponseDto(
                    match.getId(),
                    Optional.ofNullable(match.getProgrammeYear()).map(p -> p.getId()).orElse(null),

                    match.getMentor().getId(),
                    formatFullName(match.getMentor()),
                    match.getMentor().getAcademicStage().name(),
                    Optional.ofNullable(match.getMentor().getCourse()).map(c -> c.getName()).orElse("N/A"),
                    match.getCompatibilityScore(),
                    match.getMentee().getId(),
                    formatFullName(match.getMentee()),
                    match.getMentee().getAcademicStage().name(),
                    Optional.ofNullable(match.getMentee().getCourse()).map(c -> c.getName()).orElse("N/A"),

                    match.getStatus()
            );
        }

        static DetailedMatchResponseDto toDetailedResponseDto(Match match) {
            return new DetailedMatchResponseDto(
                    match.getId(),
                    Optional.ofNullable(match.getProgrammeYear()).map(p -> p.getId()).orElse(null),
                    match.getStatus(),
                    match.getCreatedAt(),
                    match.getUpdatedAt(),
                    toParticipantDto(match.getMentor()),
                    toParticipantDto(match.getMentee()),
                    match.getCompatibilityScore()
            );
        }

        static DetailedMatchResponseDto.ParticipantDto toParticipantDto(ParticipantInProgrammeYear participant) {
            return new DetailedMatchResponseDto.ParticipantDto(
                    participant.getId(),
                    participant.getUser().getFirstName(),
                    participant.getUser().getLastName(),
                    participant.getUser().getEmail(),
                    participant.getAcademicStage().name(),
                    Optional.ofNullable(participant.getCourse()).map(c -> c.getName()).orElse("N/A"),
                    participant.getAvailableDays().stream().map(Enum::name).collect(Collectors.toList()),
                    participant.getTimeRange().name(),
                    participant.getSkills(),
                    participant.getUser().getPersonalityType(),
                    participant.getUser().getGender(),
                    participant.getUser().getEthnicity(),
                    participant.getUser().getHomeCountry(),
                    participant.getUser().getLivingArrangement(),
                    participant.getUser().getDisability(),
                    participant.getUser().getProfileImageUrl(),
                    participant.getUser().getAgeGroup()
            );
        }

        private static String formatFullName(ParticipantInProgrammeYear participant) {
            return participant.getUser().getFirstName() + " " + participant.getUser().getLastName();
        }
    }
}
