package uk.bovykina.matching_guru.dto.meeting;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingDto {

    private Long id;
    private Long matchId;
    private Long participantId;
    private String title;
    private LocalDateTime dateTime;
    private String location;
    private String notes;

}
