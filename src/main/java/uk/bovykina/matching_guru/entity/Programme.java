package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "programmes")
public class Programme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "organisationId", nullable = false)
    @ToString.Exclude
    private Organisation organisation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Programme)) return false;
        Programme programme = (Programme) o;
        return id != null && id.equals(programme.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
