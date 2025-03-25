package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantLoader {

    private final ParticipantRepository participantRepository;

    public List<ParticipantInProgrammeYear> loadMentees(Long programmeYearId) {
        List<ParticipantInProgrammeYear> allMentees = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTEE);

        return allMentees.stream()
                .sorted(Comparator.comparing(p -> Boolean.TRUE.equals(p.getIsMatched()))) // false (unmatched) first
                .toList();
    }

    public List<ParticipantInProgrammeYear> loadMentors(Long programmeYearId) {
        List<ParticipantInProgrammeYear> allMentors = participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTOR);

        return allMentors.stream()
                .sorted(Comparator.comparing(p -> Boolean.TRUE.equals(p.getIsMatched()))) // false (unmatched) first
                .toList();
    }
}
