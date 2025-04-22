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
        return participantRepository.findByProgrammeYearId(programmeYearId).stream()
                .filter(p -> p.getRole() == ParticipantRole.MENTEE)
                .sorted(Comparator.comparing(p -> Boolean.TRUE.equals(p.getIsMatched())))
                .toList();
    }

    public List<ParticipantInProgrammeYear> loadMentors(Long programmeYearId) {
        return participantRepository.findByProgrammeYearId(programmeYearId).stream()
                .filter(p -> p.getRole() == ParticipantRole.MENTOR)
                .sorted(Comparator.comparing(p -> Boolean.TRUE.equals(p.getIsMatched())))
                .toList();
    }
}
