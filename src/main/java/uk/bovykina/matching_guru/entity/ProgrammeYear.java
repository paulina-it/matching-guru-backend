package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "programmeYear")
public class ProgrammeYear extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "programmeId", nullable = false)
    @ToString.Exclude
    private Programme programme;

    private String academicYear;
    private Boolean isActive;
    private String joinCode;

    @Column(columnDefinition = "json")
    private String customSettings;

    @Enumerated(EnumType.STRING)
    private AlgorithmType preferredAlgorithm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgrammeYear)) return false;
        ProgrammeYear that = (ProgrammeYear) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
