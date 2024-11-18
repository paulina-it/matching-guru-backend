package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.bovykina.matching_guru.entity.enums.CommunicationStatus;
import uk.bovykina.matching_guru.entity.enums.CommunicationType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "communicationLog")
public class CommunicationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "matchId", nullable = false)
    @ToString.Exclude
    private Match match;

    @Enumerated(EnumType.STRING)
    private CommunicationType type;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private CommunicationStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunicationLog)) return false;
        CommunicationLog that = (CommunicationLog) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
