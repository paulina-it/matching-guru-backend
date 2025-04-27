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
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.mapper.MatchMapper;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service handling all match-related operations, including creation, updates,
 * participant decisions, coordinator actions, and bulk status changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final MatchMapper matchMapper;
    private final UserRepository userRepository;

    /**
     * Checks whether a match exists between a mentor and mentee.
     */
    public boolean doesMatchExist(Long mentorId, Long menteeId) {
        boolean exists = matchRepository.existsByMentorIdAndMenteeId(mentorId, menteeId);
        log.debug("Match exists between mentor {} and mentee {}: {}", mentorId, menteeId, exists);
        return exists;
    }

    /**
     * Creates a match between a mentor and a mentee if it doesn't already exist.
     */
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

    /**
     * Updates the status of a single match.
     */
    @Transactional
    public MatchResponseDto updateMatchStatus(Long matchId, MatchStatusUpdateDto dto) {
        log.info("Updating match ID {} to status {}", matchId, dto.getStatus());

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        match.setStatus(dto.getStatus());
        Match updated = matchRepository.save(match);
        log.info("Match ID {} updated to {}", matchId, updated.getStatus());
        return matchMapper.toResponseDto(updated);
    }

    /**
     * Retrieves a page of matches for a given ProgrammeYear.
     */
    public Page<MatchResponseDto> getMatchesByProgrammeYearId(Long programmeYearId, Pageable pageable) {
        log.info("Fetching matches for programmeYearId={}", programmeYearId);
        return matchRepository.findByProgrammeYearId(programmeYearId, pageable)
                .map(matchMapper::toResponseDto);
    }

    /**
     * Retrieves a match by ID as a simple response DTO.
     */
    public MatchResponseDto getMatchById(Long matchId) {
        log.info("Fetching match ID {}", matchId);
        return matchMapper.toResponseDto(fetchMatch(matchId));
    }

    /**
     * Retrieves a detailed view of a match by its ID.
     */
    public DetailedMatchResponseDto getDetailedMatchById(Long matchId) {
        log.info("Fetching detailed match for ID {}", matchId);
        return matchMapper.toDetailedResponseDto(fetchMatch(matchId));
    }

    /**
     * Retrieves a detailed match for a participant within a specific programme year.
     */
    public DetailedMatchResponseDto getDetailedMatchByParticipantId(Long participantId, Long programmeYearId) {
        log.info("Fetching match for participantId={} in programmeYearId={}", participantId, programmeYearId);
        Match match = matchRepository.findByParticipantAndProgrammeYear(participantId, programmeYearId)
                .orElseThrow(() -> new IllegalArgumentException("No match found"));
        return matchMapper.toDetailedResponseDto(match);
    }

    /**
     * Updates the status of multiple matches in bulk and records who performed the update.
     */
    @Transactional
    public void updateMatchStatus(List<Long> matchIds, MatchStatusUpdateDto statusUpdateDto) {
        log.info("Bulk updating matches to status={}, IDs={}", statusUpdateDto.getStatus(), matchIds);

        List<Match> matches = (matchIds == null || matchIds.isEmpty())
                ? matchRepository.findAll()
                : matchRepository.findAllById(matchIds);

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No matches found");
        }

        User user = userRepository.findById(statusUpdateDto.getEditedByUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID " + statusUpdateDto.getEditedByUserId()));

        matches.forEach(match -> {
            match.setStatus(statusUpdateDto.getStatus());
            match.setEditedBy(user);
            match.setEditedByRole(user.getRole());

            if (statusUpdateDto.getStatus() == MatchStatus.DECLINED) {
                match.setRejectionReason(statusUpdateDto.getRejectionReason());

                ParticipantInProgrammeYear mentor = match.getMentor();
                ParticipantInProgrammeYear mentee = match.getMentee();

                if (mentor != null && Boolean.TRUE.equals(mentor.getIsMatched())) mentor.setIsMatched(false);
                if (mentee != null && Boolean.TRUE.equals(mentee.getIsMatched())) mentee.setIsMatched(false);
            }
        });

        matchRepository.saveAll(matches);
        log.info("✅ Updated {} matches to status {}", matches.size(), statusUpdateDto.getStatus());
    }

    /**
     * Searches matches for a coordinator by query, status, and sort options.
     */
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

    /**
     * Deletes all matches for a programme year and resets participant match flags.
     */
    @Transactional
    public void deleteMatchesByProgrammeYear(Long programmeYearId) {
        log.info("Deleting all matches for programmeYearId={}", programmeYearId);

        matchRepository.deleteByProgrammeYearId(programmeYearId);

        List<ParticipantInProgrammeYear> participants = participantRepository.findByProgrammeYearId(programmeYearId);
        participants.forEach(p -> p.setIsMatched(false));
        participantRepository.saveAll(participants);

        log.info("Reset isMatched=false for {} participants", participants.size());
    }

    /**
     * Processes a participant's decision (accept/reject) on a match.
     */
    @Transactional
    public void processParticipantDecision(MatchDecisionDto request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        MatchStatus newStatus = request.getDecision();

        if (newStatus != MatchStatus.ACCEPTED && newStatus != MatchStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid decision: must be ACCEPTED or REJECTED");
        }

        if (newStatus == MatchStatus.REJECTED) {
            match.setEditedBy(userRepository.getReferenceById(request.getUserId()));
            match.setRejectionReason(request.getRejectionReason());
            match.setStatus(MatchStatus.REJECTED);
            match.getMentor().setIsMatched(false);
            match.getMentee().setIsMatched(false);
            participantRepository.saveAll(List.of(match.getMentor(), match.getMentee()));
        } else if (newStatus == MatchStatus.ACCEPTED) {
            if (match.getStatus() == MatchStatus.ACCEPTED_BY_ONE_PARTY) {
                match.setStatus(MatchStatus.ACCEPTED_BY_BOTH);
            } else {
                match.setStatus(MatchStatus.ACCEPTED_BY_ONE_PARTY);
            }
        }

        matchRepository.save(match);
        log.info("✅ Match {} updated to status {}", match.getId(), match.getStatus());
    }

    private ParticipantInProgrammeYear fetchParticipant(Long id, String role) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(role + " not found"));
    }

    private Match fetchMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
