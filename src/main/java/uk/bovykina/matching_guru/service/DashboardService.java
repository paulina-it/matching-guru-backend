package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.dashboards.*;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearSummaryDto;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeYearRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private final ProgrammeYearRepository programmeYearRepository;
    private final ParticipantRepository participantRepository;

    public AdminDashboardDto getAdminDashboard(Long organisationId) {
        log.info("📊 Fetching Admin Dashboard for organisation ID: {}", organisationId);

        List<ProgrammeYear> activeProgrammeYears = programmeYearRepository.findByProgrammeOrganisationIdAndIsActiveTrue(organisationId);
        log.info("✅ Found {} active programme years", activeProgrammeYears.size());

        List<ProgrammeYearSummaryDto> summaries = activeProgrammeYears.stream().limit(5).map(py -> {
            int participantCount = participantRepository.countByProgrammeYearId(py.getId());
            int matchesCount = participantRepository.countByProgrammeYearIdAndIsMatchedTrue(py.getId());
            log.debug("🔹 {} {} → Participants: {}, Matches: {}",
                    py.getProgramme().getName(), py.getAcademicYear(), participantCount, matchesCount);
            return new ProgrammeYearSummaryDto(
                    py.getId(),
                    py.getProgramme().getId(),
                    py.getProgramme().getName() + " " + py.getAcademicYear(),
                    Boolean.TRUE.equals(py.getIsActive()),
                    participantCount,
                    matchesCount
            );
        }).collect(Collectors.toList());

        List<ActivityJoinProjection> joinProjections = participantRepository.findRecentJoinsByOrganisationId(
                organisationId, LocalDateTime.now().minusDays(21));
        log.info("🕒 Found {} recent join activity entries", joinProjections.size());

        List<RecentActivityDto> activity = joinProjections.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(5)
                .map(p -> new RecentActivityDto(
                        p.getCount() + " people joined " + p.getProgrammeYearName(),
                        p.getTimestamp()
                ))
                .collect(Collectors.toList());


        double totalParticipants = summaries.stream().mapToInt(ProgrammeYearSummaryDto::getParticipantsCount).sum();
        double totalMatches = summaries.stream().mapToInt(ProgrammeYearSummaryDto::getMatchesCount).sum();
        double matchRate = totalParticipants > 0 ? (totalMatches / totalParticipants) * 100.0 : 0.0;

        log.info("📈 Total Participants: {}, Matches: {}, Match Rate: {}%", totalParticipants, totalMatches, matchRate);

        return new AdminDashboardDto(
                summaries,
                activity,
                matchRate,
                0.0,
                0.0,
                LocalDateTime.now()
        );
    }
}
