package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;

import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "matches")
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mentorId", nullable = false)
    @ToString.Exclude
    private ParticipantInProgrammeYear mentor;

    @ManyToOne
    @JoinColumn(name = "menteeId", nullable = false)
    @ToString.Exclude
    private ParticipantInProgrammeYear mentee;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match)) return false;
        Match match = (Match) o;
        return id != null && id.equals(match.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
