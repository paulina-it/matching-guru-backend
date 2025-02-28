package uk.bovykina.matching_guru.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum PersonalityType {
    // Analysts
    ARCHITECT_INTJ("Architect INTJ"),
    LOGICIAN_INTP("Logician INTP"),
    COMMANDER_ENTJ("Commander ENTJ"),
    DEBATER_ENTP("Debater ENTP"),

    // Diplomats
    ADVOCATE_INFJ("Advocate INFJ"),
    MEDIATOR_INFP("Mediator INFP"),
    PROTAGONIST_ENFJ("Protagonist ENFJ"),
    CAMPAIGNER_ENFP("Campaigner ENFP"),

    // Sentinels
    LOGISTICIAN_ISTJ("Logistician ISTJ"),
    DEFENDER_ISFJ("Defender ISFJ"),
    EXECUTIVE_ESTJ("Executive ESTJ"),
    CONSUL_ESFJ("Consul ESFJ"),

    // Explorers
    VIRTUOSO_ISTP("Virtuoso ISTP"),
    ADVENTURER_ISFP("Adventurer ISFP"),
    ENTREPRENEUR_ESTP("Entrepreneur ESTP"),
    ENTERTAINER_ESFP("Entertainer ESFP");

    private final String formattedName;
    private static final Map<String, PersonalityType> FORMAT_MAP = new HashMap<>();

    static {
        for (PersonalityType type : values()) {
            FORMAT_MAP.put(type.formattedName.toLowerCase(), type);
            FORMAT_MAP.put(type.name().toLowerCase(), type);
        }
    }

    PersonalityType(String formattedName) {
        this.formattedName = formattedName;
    }

    @JsonCreator
    public static PersonalityType fromString(String value) {
        if (value == null) {
            return null;
        }
        PersonalityType personalityType = FORMAT_MAP.get(value.toLowerCase());
        if (personalityType == null) {
            throw new IllegalArgumentException("Invalid personality type: " + value);
        }
        return personalityType;
    }

    @JsonValue
    public String toValue() {
        return this.formattedName;
    }
}
