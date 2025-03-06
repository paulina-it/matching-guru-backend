package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "endSurveyResponses")
public class EndSurveyResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participantInProgramme", nullable = false)
    @ToString.Exclude
    private ParticipantInProgrammeYear participantInProgramme;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    @ToString.Exclude
    private Match match;

    @Column(columnDefinition = "json")
    private String responseData;

    private LocalDateTime completedAt;

    private String feedbackConfirmationCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndSurveyResponse)) return false;
        EndSurveyResponse that = (EndSurveyResponse) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
