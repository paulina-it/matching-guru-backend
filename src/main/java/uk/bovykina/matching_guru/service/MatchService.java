package uk.bovykina.matching_guru.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.*;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.mapper.MatchMapper;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final MatchMapper matchMapper;

    public boolean doesMatchExist(Long mentorId, Long menteeId) {
        boolean exists = matchRepository.existsByMentorIdAndMenteeId(mentorId, menteeId);
        log.debug("Match exists between mentor {} and mentee {}: {}", mentorId, menteeId, exists);
        return exists;
    }

    @Transactional
    public MatchResponseDto createMatch(MatchCreateDto dto) {
        log.info("Creating match: mentorId={}, menteeId={}", dto.getMentorId(), dto.getMenteeId());

        ParticipantInProgrammeYear mentor = fetchParticipant(dto.getMentorId(), "Mentor");
        ParticipantInProgrammeYear mentee = fetchParticipant(dto.getMenteeId(), "Mentee");

        if (doesMatchExist(dto.getMentorId(), dto.getMenteeId())) {
            log.warn("Duplicate match attempt: mentorId={}, menteeId={}", dto.getMentorId(), dto.getMenteeId());
            return null;
        }

        Match match = Match.builder()
                .mentor(mentor)
                .mentee(mentee)
                .status(dto.getStatus())
                .compatibilityScore(dto.getCompatibilityScore())
                .programmeYear(mentor.getProgrammeYear())
                .build();

        Match saved = matchRepository.save(match);
        log.info("Match created with ID: {}", saved.getId());
        return matchMapper.toResponseDto(saved);
    }

    @Transactional
    public MatchResponseDto updateMatchStatus(Long matchId, MatchStatusUpdateDto dto) {
        log.info("Updating match ID {} to status {}", matchId, dto.getStatus());

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match not found: ID={}", matchId);
                    return new IllegalArgumentException("Match not found");
                });

        match.setStatus(dto.getStatus());
        Match updated = matchRepository.save(match);
        log.info("Match ID {} updated to {}", matchId, updated.getStatus());
        return matchMapper.toResponseDto(updated);
    }

    public Page<MatchResponseDto> getMatchesByProgrammeYearId(Long programmeYearId, Pageable pageable) {
        log.info("Fetching matches for programmeYearId={}", programmeYearId);
        return matchRepository.findByProgrammeYearId(programmeYearId, pageable)
                .map(matchMapper::toResponseDto);
    }

    public MatchResponseDto getMatchById(Long matchId) {
        log.info("Fetching match ID {}", matchId);
        return matchMapper.toResponseDto(fetchMatch(matchId));
    }

    public DetailedMatchResponseDto getDetailedMatchById(Long matchId) {
        log.info("Fetching detailed match for ID {}", matchId);
        return matchMapper.toDetailedResponseDto(fetchMatch(matchId));
    }

    public DetailedMatchResponseDto getDetailedMatchByParticipantId(Long participantId, Long programmeYearId) {
        log.info("Fetching match for participantId={} in programmeYearId={}", participantId, programmeYearId);
        Match match = matchRepository.findByParticipantAndProgrammeYear(participantId, programmeYearId)
                .orElseThrow(() -> {
                    log.error("No match found for participantId={} in programmeYearId={}", participantId, programmeYearId);
                    return new IllegalArgumentException("No match found");
                });
        return matchMapper.toDetailedResponseDto(match);
    }

    @Transactional
    public void updateMatchStatus(List<Long> matchIds, MatchStatus status) {
        log.info("Bulk updating matches to status={}, IDs={}", status, matchIds);

        List<Match> matches = (matchIds == null || matchIds.isEmpty())
                ? matchRepository.findAll()
                : matchRepository.findAllById(matchIds);

        if (matches.isEmpty()) {
            log.error("No matches found for provided IDs: {}", matchIds);
            throw new IllegalArgumentException("No matches found");
        }

        matches.forEach(match -> {
            match.setStatus(status);

            if (status == MatchStatus.DECLINED) {
                ParticipantInProgrammeYear mentor = match.getMentor();
                ParticipantInProgrammeYear mentee = match.getMentee();

                if (mentor != null && Boolean.TRUE.equals(mentor.getIsMatched())) {
                    mentor.setIsMatched(false);
                }

                if (mentee != null && Boolean.TRUE.equals(mentee.getIsMatched())) {
                    mentee.setIsMatched(false);
                }
            }
        });

        matchRepository.saveAll(matches);
        log.info("✅ Updated {} matches to status {}", matches.size(), status);
    }

    public Page<CoordinatorMatchDto> searchMatches(
            Long programmeYearId,
            String query,
            MatchStatus status,
            int page,
            int size,
            String sortBy,
            String sortOrder) {
        log.info("Coordinator search: programmeYearId={}, query='{}', status={}", programmeYearId, query, status);
        Pageable pageable = buildPageable(page, size, sortBy, sortOrder);
        String safeQuery = (query == null || query.trim().isEmpty()) ? "" : query.trim();

        return matchRepository.searchMatchSummaries(programmeYearId, safeQuery, status, pageable);
    }

    @Transactional
    public void deleteMatchesByProgrammeYear(Long programmeYearId) {
        log.info("Deleting all matches for programmeYearId={}", programmeYearId);

        matchRepository.deleteByProgrammeYearId(programmeYearId);

        List<ParticipantInProgrammeYear> participants = participantRepository.findByProgrammeYearId(programmeYearId);
        participants.forEach(p -> p.setIsMatched(false));
        participantRepository.saveAll(participants);

        log.info("Reset isMatched=false for {} participants", participants.size());
    }

    @Transactional
    public void processParticipantDecision(MatchDecisionDto request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        MatchStatus newStatus = request.getDecision();
        if (newStatus != MatchStatus.ACCEPTED && newStatus != MatchStatus.DECLINED) {
            throw new IllegalArgumentException("Invalid decision: must be ACCEPTED or DECLINED");
        }

        match.setStatus(newStatus);
        matchRepository.save(match);

        if (newStatus == MatchStatus.DECLINED) {
            match.getMentor().setIsMatched(false);
            match.getMentee().setIsMatched(false);
            participantRepository.saveAll(List.of(match.getMentor(), match.getMentee()));
        }

        log.info("✅ Match {} updated to status {}", match.getId(), newStatus);
    }

    private ParticipantInProgrammeYear fetchParticipant(Long id, String role) {
        return participantRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("{} not found: ID={}", role, id);
                    return new IllegalArgumentException(role + " not found");
                });
    }

    private Match fetchMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> {
                    log.error("Match not found: ID={}", matchId);
                    return new IllegalArgumentException("Match not found");
                });
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
