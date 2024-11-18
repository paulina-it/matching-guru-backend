package uk.bovykina.matching_guru.dto.coms;

import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.CommunicationStatus;
import uk.bovykina.matching_guru.entity.enums.CommunicationType;

import java.time.LocalDateTime;

@Data
public class CommunicationLogDto {
    private Long id;
    private Long matchId;
    private CommunicationType type;
    private LocalDateTime timestamp;
    private CommunicationStatus status;
}