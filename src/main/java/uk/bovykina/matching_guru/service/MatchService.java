package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        Match match = new Match();
        match.setMentor(mentor);
        match.setMentee(mentee);
        match.setStatus(MatchStatus.PENDING);
        match.setCompatibilityScore(matchCreateDto.getCompatibilityScore());
        match.setProgrammeYear(mentor.getProgrammeYear());

        Match savedMatch = matchRepository.save(match);
        log.info("Match created successfully with ID: {}", savedMatch.getId());

        return MatchMapper.toResponseDto(savedMatch);
    }

    @Transactional
    public MatchResponseDto updateMatchStatus(Long matchId, MatchUpdateDto matchUpdateDto) {
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

    private static class MatchMapper {

        static MatchResponseDto toResponseDto(Match match) {
            return new MatchResponseDto(
                    match.getId(),
                    Optional.ofNullable(match.getProgrammeYear()).map(p -> p.getId()).orElse(null),

                    match.getMentor().getId(),
                    formatFullName(match.getMentor()),
                    match.getMentor().getAcademicStage().name(),
                    Optional.ofNullable(match.getMentor().getCourse()).map(c -> c.getName()).orElse("N/A"),

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
                    participant.getTimeRange().name()
            );
        }

        private static String formatFullName(ParticipantInProgrammeYear participant) {
            return participant.getUser().getFirstName() + " " + participant.getUser().getLastName();
        }
    }
}
