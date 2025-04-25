package uk.bovykina.matching_guru.dto.stats.engagement;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProgrammeYearEngagementStatsDto {
    private String academicYear;
    private int participants;
    private int mentors;
    private int mentees;
    private int bothRoles;
    private int inactivePairs;
    private int feedbackSubmitted;
    private double avgInteractionsPerMatch;
    private double feedbackCompletionRate;
    private Map<String, Integer> communicationBreakdown;
    private List<WeeklyEngagementPoint> weeklyEngagement;
}