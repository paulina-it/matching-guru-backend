package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.bovykina.matching_guru.entity.enums.Availability;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.entity.enums.AcademicStage;
import uk.bovykina.matching_guru.entity.enums.TimeRange;

import javax.net.ssl.SSLSession;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "participantsInProgrammeYear")
public class ParticipantInProgrammeYear extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "programmeYearId", nullable = false)
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

    @ElementCollection
    @CollectionTable(name = "participant_availability_days", joinColumns = @JoinColumn(name = "participantId"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dayOfWeek")
    private Set<DayOfWeek> availableDays = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private TimeRange timeRange;

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

