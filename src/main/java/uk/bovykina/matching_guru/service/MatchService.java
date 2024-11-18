package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.match.*;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    private final ParticipantRepository participantRepository;

    public MatchDto createMatch(MatchCreateDto matchCreateDto) {
        ParticipantInProgrammeYear mentor = participantRepository.findById(matchCreateDto.getMentorId())
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        ParticipantInProgrammeYear mentee = participantRepository.findById(matchCreateDto.getMenteeId())
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Match match = new Match();
        match.setMentor(mentor);
        match.setMentee(mentee);
        match.setStatus(MatchStatus.PENDING);

        Match savedMatch = matchRepository.save(match);
        return convertToDto(savedMatch);
    }

    public MatchDto updateMatchStatus(Long matchId, MatchUpdateDto matchUpdateDto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        match.setStatus(matchUpdateDto.getStatus());
        Match updatedMatch = matchRepository.save(match);
        return convertToDto(updatedMatch);
    }

    public List<MatchDto> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MatchDto> getMatchesByProgrammeYearId(Long programmeYearId) {
        List<Match> matches = matchRepository.findByMentorProgrammeYearId(programmeYearId);
        return matches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MatchDto convertToDto(Match match) {
        MatchDto matchDto = new MatchDto();
        matchDto.setId(match.getId());
        matchDto.setMentorId(match.getMentor().getId());
        matchDto.setMenteeId(match.getMentee().getId());
        matchDto.setStatus(match.getStatus());
        return matchDto;
    }
}
