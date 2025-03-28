package uk.bovykina.matching_guru.dto.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearSummaryDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private List<ProgrammeYearSummaryDto> activeProgrammeYears;

    private List<RecentActivityDto> recentActivity;

    private double averageMatchRate;
    private double averageEngagement;
    private double averageDropoffRate;

    private LocalDateTime lastUpdated;
}
