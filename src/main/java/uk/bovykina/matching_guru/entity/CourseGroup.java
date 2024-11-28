package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "course_groups")
public class CourseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "organisationId", nullable = false)
    @ToString.Exclude
    private Organisation organisation;

    @ManyToMany(mappedBy = "eligibleCourseGroups")
    private Set<Programme> programmes = new HashSet<>();
}
