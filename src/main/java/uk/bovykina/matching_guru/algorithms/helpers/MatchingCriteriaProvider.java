package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchingCriteriaProvider {
    private final ProgrammeMatchingCriteriaRepository repo;

    public Map<String, Integer> loadWeights(Long programmeYearId) {
        return repo.findByProgrammeYearId(programmeYearId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCriterionType().name(),
                        ProgrammeMatchingCriteria::getWeight
                ));
    }
}

