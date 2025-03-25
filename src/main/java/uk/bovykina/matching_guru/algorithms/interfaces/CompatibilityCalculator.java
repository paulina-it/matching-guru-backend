package uk.bovykina.matching_guru.algorithms.interfaces;

import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;

import java.util.Map;

public interface CompatibilityCalculator {
    double calculate(ParticipantInProgrammeYear mentor, ParticipantInProgrammeYear mentee, Map<String, Integer> weights);
}
