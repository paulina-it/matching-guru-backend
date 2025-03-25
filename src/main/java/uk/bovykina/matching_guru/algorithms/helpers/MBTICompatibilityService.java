package uk.bovykina.matching_guru.algorithms.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.enums.CriterionType;
import uk.bovykina.matching_guru.entity.enums.PersonalityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MBTICompatibilityService {

    public int getScore(PersonalityType mentorType, PersonalityType menteeType, Map<String, Integer> weights) {
        if (mentorType == null || menteeType == null) {
            return 0;
        }

        Map<PersonalityType, List<PersonalityType>> mbtiBestMatches = new HashMap<>();
        mbtiBestMatches.put(PersonalityType.ARCHITECT_INTJ, List.of(PersonalityType.DEBATER_ENTP, PersonalityType.COMMANDER_ENTJ, PersonalityType.ADVOCATE_INFJ));
        mbtiBestMatches.put(PersonalityType.LOGICIAN_INTP, List.of(PersonalityType.CAMPAIGNER_ENFP, PersonalityType.PROTAGONIST_ENFJ, PersonalityType.ARCHITECT_INTJ));
        mbtiBestMatches.put(PersonalityType.COMMANDER_ENTJ, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.DEBATER_ENTP, PersonalityType.EXECUTIVE_ESTJ));
        mbtiBestMatches.put(PersonalityType.DEBATER_ENTP, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.COMMANDER_ENTJ, PersonalityType.ENTREPRENEUR_ESTP));

        mbtiBestMatches.put(PersonalityType.ADVOCATE_INFJ, List.of(PersonalityType.COMMANDER_ENTJ, PersonalityType.CAMPAIGNER_ENFP, PersonalityType.ADVOCATE_INFJ));
        mbtiBestMatches.put(PersonalityType.MEDIATOR_INFP, List.of(PersonalityType.PROTAGONIST_ENFJ, PersonalityType.CAMPAIGNER_ENFP, PersonalityType.DEFENDER_ISFJ));
        mbtiBestMatches.put(PersonalityType.PROTAGONIST_ENFJ, List.of(PersonalityType.MEDIATOR_INFP, PersonalityType.CAMPAIGNER_ENFP, PersonalityType.LOGICIAN_INTP));
        mbtiBestMatches.put(PersonalityType.CAMPAIGNER_ENFP, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.LOGICIAN_INTP, PersonalityType.PROTAGONIST_ENFJ));

        mbtiBestMatches.put(PersonalityType.LOGISTICIAN_ISTJ, List.of(PersonalityType.DEFENDER_ISFJ, PersonalityType.EXECUTIVE_ESTJ, PersonalityType.LOGICIAN_INTP));
        mbtiBestMatches.put(PersonalityType.DEFENDER_ISFJ, List.of(PersonalityType.LOGISTICIAN_ISTJ, PersonalityType.MEDIATOR_INFP, PersonalityType.CONSUL_ESFJ));
        mbtiBestMatches.put(PersonalityType.EXECUTIVE_ESTJ, List.of(PersonalityType.COMMANDER_ENTJ, PersonalityType.LOGISTICIAN_ISTJ, PersonalityType.CONSUL_ESFJ));
        mbtiBestMatches.put(PersonalityType.CONSUL_ESFJ, List.of(PersonalityType.EXECUTIVE_ESTJ, PersonalityType.DEFENDER_ISFJ, PersonalityType.ENTERTAINER_ESFP));

        mbtiBestMatches.put(PersonalityType.VIRTUOSO_ISTP, List.of(PersonalityType.ENTREPRENEUR_ESTP, PersonalityType.ADVENTURER_ISFP, PersonalityType.LOGICIAN_INTP));
        mbtiBestMatches.put(PersonalityType.ADVENTURER_ISFP, List.of(PersonalityType.VIRTUOSO_ISTP, PersonalityType.ENTERTAINER_ESFP, PersonalityType.MEDIATOR_INFP));
        mbtiBestMatches.put(PersonalityType.ENTREPRENEUR_ESTP, List.of(PersonalityType.VIRTUOSO_ISTP, PersonalityType.DEBATER_ENTP, PersonalityType.EXECUTIVE_ESTJ));
        mbtiBestMatches.put(PersonalityType.ENTERTAINER_ESFP, List.of(PersonalityType.ADVENTURER_ISFP, PersonalityType.CONSUL_ESFJ, PersonalityType.CAMPAIGNER_ENFP));

        // Same approach for "Good Matches"
        Map<PersonalityType, List<PersonalityType>> mbtiGoodMatches = new HashMap<>();
        mbtiGoodMatches.put(PersonalityType.ARCHITECT_INTJ, List.of(PersonalityType.LOGICIAN_INTP, PersonalityType.LOGISTICIAN_ISTJ));
        mbtiGoodMatches.put(PersonalityType.LOGICIAN_INTP, List.of(PersonalityType.ARCHITECT_INTJ, PersonalityType.VIRTUOSO_ISTP));
        mbtiGoodMatches.put(PersonalityType.COMMANDER_ENTJ, List.of(PersonalityType.EXECUTIVE_ESTJ, PersonalityType.ADVOCATE_INFJ));
        mbtiGoodMatches.put(PersonalityType.DEBATER_ENTP, List.of(PersonalityType.ENTREPRENEUR_ESTP, PersonalityType.CAMPAIGNER_ENFP));

        mbtiGoodMatches.put(PersonalityType.ADVOCATE_INFJ, List.of(PersonalityType.ADVOCATE_INFJ, PersonalityType.COMMANDER_ENTJ));
        mbtiGoodMatches.put(PersonalityType.MEDIATOR_INFP, List.of(PersonalityType.PROTAGONIST_ENFJ, PersonalityType.ADVENTURER_ISFP));
        mbtiGoodMatches.put(PersonalityType.PROTAGONIST_ENFJ, List.of(PersonalityType.CAMPAIGNER_ENFP, PersonalityType.MEDIATOR_INFP));
        mbtiGoodMatches.put(PersonalityType.CAMPAIGNER_ENFP, List.of(PersonalityType.PROTAGONIST_ENFJ, PersonalityType.DEBATER_ENTP));

        mbtiGoodMatches.put(PersonalityType.LOGISTICIAN_ISTJ, List.of(PersonalityType.LOGICIAN_INTP, PersonalityType.EXECUTIVE_ESTJ));
        mbtiGoodMatches.put(PersonalityType.DEFENDER_ISFJ, List.of(PersonalityType.CONSUL_ESFJ, PersonalityType.EXECUTIVE_ESTJ));
        mbtiGoodMatches.put(PersonalityType.EXECUTIVE_ESTJ, List.of(PersonalityType.COMMANDER_ENTJ, PersonalityType.LOGISTICIAN_ISTJ));
        mbtiGoodMatches.put(PersonalityType.CONSUL_ESFJ, List.of(PersonalityType.ENTERTAINER_ESFP, PersonalityType.DEFENDER_ISFJ));

        mbtiGoodMatches.put(PersonalityType.VIRTUOSO_ISTP, List.of(PersonalityType.LOGICIAN_INTP, PersonalityType.ENTREPRENEUR_ESTP));
        mbtiGoodMatches.put(PersonalityType.ADVENTURER_ISFP, List.of(PersonalityType.ENTERTAINER_ESFP, PersonalityType.MEDIATOR_INFP));
        mbtiGoodMatches.put(PersonalityType.ENTREPRENEUR_ESTP, List.of(PersonalityType.DEBATER_ENTP, PersonalityType.VIRTUOSO_ISTP));
        mbtiGoodMatches.put(PersonalityType.ENTERTAINER_ESFP, List.of(PersonalityType.CONSUL_ESFJ, PersonalityType.ADVENTURER_ISFP));

        int basePersonalityScore = weights.getOrDefault(CriterionType.PERSONALITY.name(), 7);

        if (mbtiBestMatches.getOrDefault(mentorType, List.of()).contains(menteeType)) {
            return basePersonalityScore + 3;
        }
        if (mbtiGoodMatches.getOrDefault(mentorType, List.of()).contains(menteeType)) {
            return basePersonalityScore + 2;
        }

        return basePersonalityScore;
    }
}
