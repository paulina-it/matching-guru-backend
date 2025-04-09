package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime signupOpenDate;
    private LocalDateTime signupCloseDate;
    private String academicYear;
    private Boolean isActive;
    private String joinCode;
    private String feedbackConfirmationCode;
    @Column(length = 2048)
    private String surveyUrl;
    private LocalDateTime surveyOpenDate;
    private LocalDateTime surveyCloseDate;
    private Boolean strictAcademicStage;
    private Boolean strictCourseGroup;
    @Column(length = 2048)
    private String certificateTemplateUrl;

    @ManyToOne
    @JoinColumn(name = "programme_id")
    private Programme programme;

    @Enumerated(EnumType.STRING)
    private AlgorithmType preferredAlgorithm;

    @Enumerated(EnumType.STRING)
    private MatchApprovalType matchApprovalType;

    private Integer approvalThreshold;

    @OneToMany(mappedBy = "programmeYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProgrammeMatchingCriteria> matchingCriteria;

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

    public String generateFeedbackConfirmationCode() {
        this.feedbackConfirmationCode = UUID.randomUUID().toString();
        return this.feedbackConfirmationCode;
    }

    public boolean isSurveyOpen() {
        LocalDateTime now = LocalDateTime.now();
        return surveyOpenDate != null && surveyCloseDate != null
                && now.isAfter(surveyOpenDate)
                && now.isBefore(surveyCloseDate);
    }

    public boolean isSignupOpen() {
        LocalDateTime now = LocalDateTime.now();
        return signupOpenDate != null && signupCloseDate != null
                && now.isAfter(signupOpenDate)
                && now.isBefore(signupCloseDate);
    }

    public boolean isCurrentlyRunning() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && endDate != null
                && now.isAfter(startDate)
                && now.isBefore(endDate);
    }
}
