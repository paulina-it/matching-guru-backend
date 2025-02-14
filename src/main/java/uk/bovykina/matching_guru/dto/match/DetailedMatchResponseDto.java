package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailedMatchResponseDto {

    private Long id;
    private Long programmeYearId;
    private MatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ParticipantDto mentor;
    private ParticipantDto mentee;
    private double compatibilityScore;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantDto {
        private Long participantId;
        private String firstName;
        private String lastName;
        private String email;
        private String academicStage;
        private String course;
        private List<String> availableDays;
        private String timePreference;
    }
}
