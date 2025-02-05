package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.match.*;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.enums.DayOfWeek;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public MatchResponseDto createMatch(MatchCreateDto matchCreateDto) {
        ParticipantInProgrammeYear mentor = participantRepository.findById(matchCreateDto.getMentorId())
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        ParticipantInProgrammeYear mentee = participantRepository.findById(matchCreateDto.getMenteeId())
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Match match = new Match();
        match.setMentor(mentor);
        match.setMentee(mentee);
        match.setStatus(MatchStatus.PENDING);

        Match savedMatch = matchRepository.save(match);
        return convertToResponseDto(savedMatch);
    }

    @Transactional
    public MatchResponseDto updateMatchStatus(Long matchId, MatchUpdateDto matchUpdateDto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        match.setStatus(matchUpdateDto.getStatus());
        Match updatedMatch = matchRepository.save(match);
        return convertToResponseDto(updatedMatch);
    }

    public List<MatchResponseDto> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<MatchResponseDto> getMatchesByProgrammeYearId(Long programmeYearId) {
        List<Match> matches = matchRepository.findByMentorProgrammeYearId(programmeYearId);
        return matches.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private MatchResponseDto convertToResponseDto(Match match) {
        return new MatchResponseDto(
                match.getId(),

                match.getMentor().getId(),
                match.getMentor().getUser().getFirstName() + " " + match.getMentor().getUser().getLastName(),
                match.getMentor().getAcademicStage().name(),
                match.getMentor().getCourse().getName(),

                match.getMentee().getId(),
                match.getMentee().getUser().getFirstName() + " " + match.getMentee().getUser().getLastName(),
                match.getMentee().getAcademicStage().name(),
                match.getMentee().getCourse().getName(),

                match.getStatus()
        );
    }

    public DetailedMatchResponseDto getDetailedMatchById(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        return new DetailedMatchResponseDto(
                match.getId(),
                match.getStatus(),
                match.getCreatedAt(),
                match.getUpdatedAt(),

                new DetailedMatchResponseDto.ParticipantDto(
                        match.getMentor().getId(),
                        match.getMentor().getUser().getFirstName(),
                        match.getMentor().getUser().getLastName(),
                        match.getMentor().getUser().getEmail(),
                        match.getMentor().getAcademicStage().name(),
                        match.getMentor().getCourse().getName(),
                        match.getMentor().getAvailableDays().stream()
                                .map(day -> day.name())
                                .collect(Collectors.toList()),
                        match.getMentor().getTimeRange().name()
                ),

                new DetailedMatchResponseDto.ParticipantDto(
                        match.getMentee().getId(),
                        match.getMentee().getUser().getFirstName(),
                        match.getMentee().getUser().getLastName(),
                        match.getMentee().getUser().getEmail(),
                        match.getMentee().getAcademicStage().name(),
                        match.getMentee().getCourse().getName(),
                        match.getMentee().getAvailableDays().stream()
                                .map(day -> day.name())
                                .collect(Collectors.toList()),
                        match.getMentee().getTimeRange().name()
                )
        );
    }


    public MatchResponseDto getMatchById(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        return new MatchResponseDto(
                match.getId(),
                match.getMentor().getId(),
                match.getMentor().getUser().getFirstName() + " " + match.getMentor().getUser().getLastName(),
                match.getMentor().getAcademicStage().name(),
                match.getMentor().getCourse().getName(),

                match.getMentee().getId(),
                match.getMentee().getUser().getFirstName() + " " + match.getMentee().getUser().getLastName(),
                match.getMentee().getAcademicStage().name(),
                match.getMentee().getCourse().getName(),

                match.getStatus()
        );
    }

}
