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
    private final MeetingMapper meetingMapper = new MeetingMapper();

    /**
     * Schedules a new meeting between participants in a match.
     */
    @Transactional
    public MeetingDto scheduleMeeting(MeetingDto meetingDto) {
        log.info("Scheduling meeting for participant ID: {} in match ID: {}",
                meetingDto.getParticipantId(), meetingDto.getMatchId());

        Match match = matchRepository.findById(meetingDto.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        ParticipantInProgrammeYear participant = participantRepository.findById(meetingDto.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        Meeting meeting = new Meeting();
        meeting.setMatch(match);
        meeting.setScheduledBy(participant);
        meeting.setTitle(meetingDto.getTitle());
        meeting.setDateTime(meetingDto.getDateTime());
        meeting.setLocation(meetingDto.getLocation());
        meeting.setNotes(meetingDto.getNotes());

        meeting = meetingRepository.save(meeting);
        log.info("Meeting scheduled successfully: ID={}", meeting.getId());

        return meetingMapper.toDto(meeting);
    }

    private static class MeetingMapper {

        public MeetingDto toDto(Meeting meeting) {
            MeetingDto dto = new MeetingDto();
            dto.setId(meeting.getId());
            dto.setMatchId(meeting.getMatch().getId());
            dto.setParticipantId(meeting.getScheduledBy().getId());
            dto.setTitle(meeting.getTitle());
            dto.setDateTime(meeting.getDateTime());
            dto.setLocation(meeting.getLocation());
            dto.setNotes(meeting.getNotes());
            return dto;
        }
    }
}
