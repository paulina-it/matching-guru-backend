package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.dashboards.*;
import uk.bovykina.matching_guru.dto.match.MatchSummaryDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearSummaryDto;
import uk.bovykina.matching_guru.entity.CommunicationLog;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.repository.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private final ProgrammeYearRepository programmeYearRepository;
    private final ParticipantRepository participantRepository;
    private final MatchRepository matchRepository;
    private final EndSurveyResponseRepository endSurveyRepository;
    private final CommunicationLogRepository communicationLogRepository;

    public AdminDashboardDto getAdminDashboard(Long organisationId) {
        log.info("📊 Fetching Admin Dashboard for organisation ID: {}", organisationId);

        List<ProgrammeYear> activeProgrammeYears =
                programmeYearRepository.findByProgrammeOrganisationIdAndIsActiveTrue(organisationId);
        log.info("✅ Found {} active programme years", activeProgrammeYears.size());

        List<ProgrammeYearSummaryDto> summaries = activeProgrammeYears.stream().limit(5).map(py -> {
            int participantCount = participantRepository.countByProgrammeYearId(py.getId());
            int matchesCount = participantRepository.countByProgrammeYearIdAndIsMatchedTrue(py.getId());
            return new ProgrammeYearSummaryDto(
                    py.getId(),
                    py.getProgramme().getId(),
                    py.getProgramme().getName() + " " + py.getAcademicYear(),
                    Boolean.TRUE.equals(py.getIsActive()),
                    participantCount,
                    matchesCount
            );
        }).collect(Collectors.toList());

        List<ActivityJoinProjection> joinProjections =
                participantRepository.findRecentJoinsByOrganisationId(organisationId, LocalDateTime.now().minusDays(21));
        log.info("🕒 Found {} recent join activity entries", joinProjections.size());

        List<RecentActivityDto> activity = new ArrayList<>();

        // Add join activity with links
        joinProjections.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(5)
                .forEach(p -> activity.add(
                        new RecentActivityDto(
                                "🧑‍🤝‍🧑 " + p.getCount() + " people joined " + p.getProgrammeYearName(),
                                p.getTimestamp(),
                                "/coordinator/programmes/" + p.getProgrammeId() + "/years/" + p.getProgrammeYearId() + "/participants"
                        )
                ));

        // Add pending matches activity with links
        activeProgrammeYears.forEach(py -> {
            int pendingCount = matchRepository.countPendingMatches(py.getId());
            if (pendingCount > 0) {
                String description = String.format(
                        "⏳ %s (%s) has %d pending match%s",
                        py.getProgramme().getName(),
                        py.getAcademicYear(),
                        pendingCount,
                        pendingCount == 1 ? "" : "es"
                );
                String link = "/coordinator/programmes/" + py.getProgramme().getId() + "/years/" + py.getId() + "/matches";
                activity.add(new RecentActivityDto(description, LocalDateTime.now(), link));
            }
        });


        double totalParticipants = summaries.stream().mapToInt(ProgrammeYearSummaryDto::getParticipantsCount).sum();
        double totalMatches = summaries.stream().mapToInt(ProgrammeYearSummaryDto::getMatchesCount).sum();
        double matchRate = totalParticipants > 0 ? (totalMatches / totalParticipants) * 100.0 : 0.0;

        return new AdminDashboardDto(
                summaries,
                activity.stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).limit(6).toList(),
                matchRate,
                0.0,
                0.0,
                LocalDateTime.now()
        );
    }

    public ParticipantDashboardDto getParticipantDashboard(Long userId) {
        log.info("\uD83D\uDCCA Building dashboard for participant user ID: {}", userId);

        List<ParticipantInProgrammeYear> participations = participantRepository.findAllByUserId(userId);
        List<MatchSummaryDto> matches = new ArrayList<>();
        List<ProgrammeParticipationSummaryDto> programmeSummaries = new ArrayList<>();

        boolean hasUnconfirmedMatches = false;
        boolean hasOverdueInteractions = false;
        boolean hasFeedbackPending = false;

        LocalDateTime latestInteraction = null;
        LocalDateTime nextSuggestedMeeting = null;
        String suggestedMeetingDay = null;

        for (ParticipantInProgrammeYear p : participations) {
            List<Match> userMatches = matchRepository.findByMentorIdOrMenteeId(p.getId());
            boolean feedbackSubmitted = endSurveyRepository.existsByParticipantInProgramme(p);

            programmeSummaries.add(new ProgrammeParticipationSummaryDto(
                    p.getProgrammeYear().getId(),
                    p.getProgrammeYear().getProgramme().getId(),
                    p.getProgrammeYear().getProgramme().getName(),
                    p.getProgrammeYear().getAcademicYear(),
                    p.getRole(),
                    p.getIsMatched(),
                    feedbackSubmitted
            ));

            for (Match m : userMatches) {
                boolean isMentor = m.getMentor().getId().equals(p.getId());
                ParticipantInProgrammeYear other = isMentor ? m.getMentee() : m.getMentor();
                var user = other.getUser();

                Optional<CommunicationLog> latest = communicationLogRepository.findTopByMatchIdOrderByTimestampDesc(m.getId());
                LocalDateTime interactionDate = latest.map(CommunicationLog::getTimestamp).orElse(null);
                if (interactionDate != null && (latestInteraction == null || interactionDate.isAfter(latestInteraction))) {
                    latestInteraction = interactionDate;
                }
                if (interactionDate == null || Duration.between(interactionDate, LocalDateTime.now()).toDays() > 14) {
                    hasOverdueInteractions = true;
                }

                if (m.getStatus() == MatchStatus.PENDING) hasUnconfirmedMatches = true;

                matches.add(MatchSummaryDto.builder()
                        .matchId(m.getId())
                        .programmeYearId(p.getProgrammeYear().getId())
                        .programmeName(p.getProgrammeYear().getProgramme().getName())
                        .academicYear(p.getProgrammeYear().getAcademicYear())
                        .status(m.getStatus())
                        .isMentor(isMentor)
                        .matchWithName(user.getFirstName() + " " + user.getLastName())
                        .matchWithEmail(user.getEmail())
                        .compatibilityScore(m.getCompatibilityScore())
                        .feedbackSubmitted(feedbackSubmitted)
                        .lastInteractionDate(interactionDate)
                        .lastInteractionStatus(latest.map(CommunicationLog::getStatus).orElse(null))
                        .lastInteractionType(latest.map(CommunicationLog::getType).orElse(null))
                        .build());
            }

            ProgrammeYear py = p.getProgrammeYear();
            if (!feedbackSubmitted && py.getSurveyOpenDate() != null && py.getSurveyCloseDate() != null) {
                LocalDate today = LocalDate.now();
                if (!today.isBefore(py.getSurveyOpenDate().toLocalDate()) && !today.isAfter(py.getSurveyCloseDate().toLocalDate())) {
                    hasFeedbackPending = true;
                }
            }
        }

        return ParticipantDashboardDto.builder()
                .participantName(participations.isEmpty() ? null : participations.get(0).getUser().getFirstName())
                .organisationName(participations.isEmpty() ? null : participations.get(0).getUser().getOrganisation().getName())
                .activeParticipations(programmeSummaries)
                .matches(matches)
                .hasUnconfirmedMatches(hasUnconfirmedMatches)
                .hasFeedbackPending(hasFeedbackPending)
                .hasOverdueInteractions(hasOverdueInteractions)
                .lastInteraction(latestInteraction)
                .nextSuggestedMeetingDate(null) // future enhancement
                .suggestedMeetingDay(null)     // future enhancement
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
