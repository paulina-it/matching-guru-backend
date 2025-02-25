package uk.bovykina.matching_guru.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.entity.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
        private Set<Skill> skills;
        private PersonalityType personalityType;
        private Gender gender;
        private String ethnicity;
        private String homeCountry;
        private LivingArrangement livingArrangement;
        private String disability;
        private String profileImageUrl;
        private AgeGroup ageGroup;
    }
}
