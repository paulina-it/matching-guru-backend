package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Builder
@Table(name = "matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mentor_id", "mentee_id"})
})
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    @ToString.Exclude
    private ParticipantInProgrammeYear mentor;

    @ManyToOne
    @JoinColumn(name = "mentee_id", nullable = false)
    @ToString.Exclude
    private ParticipantInProgrammeYear mentee;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    private double compatibilityScore;

    @ManyToOne
    @JoinColumn(name = "programme_year_id", nullable = false)
    @ToString.Exclude
    private ProgrammeYear programmeYear;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(mentor, match.mentor) &&
                Objects.equals(mentee, match.mentee) &&
                Objects.equals(programmeYear, match.programmeYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mentor, mentee, programmeYear);
    }
}
