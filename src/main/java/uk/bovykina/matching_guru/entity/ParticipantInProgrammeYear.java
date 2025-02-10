package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.bovykina.matching_guru.entity.enums.*;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "participants_in_programme_year")
public class ParticipantInProgrammeYear extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "programme_year_id", nullable = false)
    @ToString.Exclude
    private ProgrammeYear programmeYear;

    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    private Integer menteesNumber;
    private Boolean isMatched;

    @Enumerated(EnumType.STRING)
    private AcademicStage academicStage;

    private Boolean hadPlacement;
    private String placementDescription;
    private String motivation;
    private Boolean isReturningParticipant;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "participant_availability_days", joinColumns = @JoinColumn(name = "participant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> availableDays = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private TimeRange timeRange;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Skill.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "participant_skills", joinColumns = @JoinColumn(name = "participant_id"))
    @Column(name = "skill")
    private Set<Skill> skills = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantInProgrammeYear that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Course getCourse() {
        return user.getCourse();
    }

    public CourseGroup getCourseGroup() {
        return user.getCourse().getGroup();
    }
}
