package uk.bovykina.matching_guru.dto.coms;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import uk.bovykina.matching_guru.entity.enums.CommunicationStatus;
import uk.bovykina.matching_guru.entity.enums.CommunicationType;

import java.time.LocalDateTime;

@Data
public class CommunicationLogCreateDto {
    @NotNull(message = "Match ID must not be null")
    private Long matchId;

    @NotNull(message = "Communication type is required")
    private CommunicationType type;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @NotNull(message = "Communication status is required")
    private CommunicationStatus status;
}