package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
            name = "programme_course_groups",
            joinColumns = @JoinColumn(name = "programmeId"),
            inverseJoinColumns = @JoinColumn(name = "courseGroupId")
    )
    private Set<CourseGroup> eligibleCourseGroups = new HashSet<>();


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
