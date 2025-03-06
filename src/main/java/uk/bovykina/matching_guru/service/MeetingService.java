package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.meeting.MeetingDto;
import uk.bovykina.matching_guru.entity.Meeting;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.repository.MeetingRepository;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public MeetingDto scheduleMeeting(MeetingDto meetingDto) {
        log.info("Attempting to schedule meeting for participant ID: {} in match ID: {}", meetingDto.getParticipantId(), meetingDto.getMatchId());

        // Fetching the match
        Match match = matchRepository.findById(meetingDto.getMatchId())
                .orElseThrow(() -> {
                    log.error("Match with ID: {} not found", meetingDto.getMatchId());
                    return new IllegalArgumentException("Match not found");
                });

        // Fetching the participant scheduling the meeting
        ParticipantInProgrammeYear participant = participantRepository.findById(meetingDto.getParticipantId())
                .orElseThrow(() -> {
                    log.error("Participant with ID: {} not found", meetingDto.getParticipantId());
                    return new IllegalArgumentException("Participant not found");
                });

        // Creating the meeting
        Meeting meeting = new Meeting();
        meeting.setMatch(match);
        meeting.setScheduledBy(participant);
        meeting.setTitle(meetingDto.getTitle());
        meeting.setDateTime(meetingDto.getDateTime());
        meeting.setLocation(meetingDto.getLocation());
        meeting.setNotes(meetingDto.getNotes());

        // Saving the meeting to the database
        meeting = meetingRepository.save(meeting);
        log.info("Successfully scheduled meeting with ID: {}", meeting.getId());

        return toDto(meeting);
    }

    private MeetingDto toDto(Meeting meeting) {
        MeetingDto dto = new MeetingDto();
        dto.setId(meeting.getId());
        dto.setMatchId(meeting.getMatch().getId());
        dto.setParticipantId(meeting.getScheduledBy().getId()); // The participant who scheduled the meeting
        dto.setTitle(meeting.getTitle());
        dto.setDateTime(meeting.getDateTime());
        dto.setLocation(meeting.getLocation());
        dto.setNotes(meeting.getNotes());
        return dto;
    }
}
