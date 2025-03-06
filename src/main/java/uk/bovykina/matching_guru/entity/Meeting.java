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
@Table(name = "meetings")
public class Meeting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    @ToString.Exclude
    private Match match;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    @ToString.Exclude
    private ParticipantInProgrammeYear scheduledBy;

    private String title;
    private LocalDateTime dateTime;
    private String location;
    private String notes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meeting)) return false;
        Meeting meeting = (Meeting) o;
        return id != null && id.equals(meeting.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
