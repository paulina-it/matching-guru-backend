package uk.bovykina.matching_guru.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.repository.EndSurveyResponseRepository;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchMapper {

    private final EndSurveyResponseRepository endSurveyResponseRepository;


    public MatchResponseDto toResponseDto(Match match) {
        Long editedByUserId = null;
        String editedByUserName = null;
        if (match.getEditedBy() != null) {
            editedByUserId = match.getEditedBy().getId();
            editedByUserName = match.getEditedBy().getFullName();
        }

        return new MatchResponseDto(
                match.getId(),
                Optional.ofNullable(match.getProgrammeYear()).map(p -> p.getId()).orElse(null),

                match.getMentor().getId(),
                formatFullName(match.getMentor()),
                match.getMentor().getAcademicStage().name(),
                Optional.ofNullable(match.getMentor().getCourse()).map(c -> c.getName()).orElse("N/A"),
                match.getCompatibilityScore(),

                match.getRejectionReason(),

                editedByUserId,
                editedByUserName,
                match.getEditedByRole(),

                match.getMentee().getId(),
                formatFullName(match.getMentee()),
                match.getMentee().getAcademicStage().name(),
                Optional.ofNullable(match.getMentee().getCourse()).map(c -> c.getName()).orElse("N/A"),

                match.getStatus()
        );
    }

    public DetailedMatchResponseDto toDetailedResponseDto(Match match) {
        return new DetailedMatchResponseDto(
                match.getId(),
                Optional.ofNullable(match.getProgrammeYear()).map(p -> p.getId()).orElse(null),
                match.getStatus(),
                match.getCreatedAt(),
                match.getUpdatedAt(),
                toParticipantDto(match.getMentor()),
                toParticipantDto(match.getMentee()),
                match.getCompatibilityScore(),
                match.getRejectionReason(),

                Optional.ofNullable(match.getEditedBy()).map(user -> user.getId()).orElse(null),
                Optional.ofNullable(match.getEditedBy()).map(user -> formatFullName(user)).orElse(null),
                Optional.ofNullable(match.getEditedByRole()).orElse(null)
        );
    }

    public DetailedMatchResponseDto.ParticipantDto toParticipantDto(ParticipantInProgrammeYear participant) {
        boolean hasFeedback = endSurveyResponseRepository.existsByParticipantInProgramme(participant);

        return new DetailedMatchResponseDto.ParticipantDto(
                participant.getId(),
                participant.getUser().getFirstName(),
                participant.getUser().getLastName(),
                participant.getUser().getEmail(),
                participant.getAcademicStage().name(),
                Optional.ofNullable(participant.getCourse()).map(c -> c.getName()).orElse("N/A"),
                participant.getAvailableDays().stream().map(Enum::name).collect(Collectors.toList()),
                participant.getTimeRange().name(),
                participant.getSkills(),
                participant.getUser().getPersonalityType(),
                participant.getUser().getGender(),
                participant.getUser().getEthnicity(),
                participant.getUser().getHomeCountry(),
                participant.getUser().getLivingArrangement(),
                participant.getUser().getDisability(),
                participant.getUser().getProfileImageUrl(),
                participant.getUser().getAgeGroup(),
                hasFeedback
        );
    }

    private String formatFullName(ParticipantInProgrammeYear participant) {
        return participant.getUser().getFirstName() + " " + participant.getUser().getLastName();
    }

    private String formatFullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
