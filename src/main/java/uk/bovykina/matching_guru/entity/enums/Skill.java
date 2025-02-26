package uk.bovykina.matching_guru.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum Skill {
    STATISTICS, LEADERSHIP, MARKETING, ADAPTABILITY, PROJECT_MANAGEMENT,
    TEAMWORK, LABORATORY_TECHNIQUES, CRITICAL_THINKING, NOTE_TAKING,
    ENGINEERING, NETWORKING, TIME_MANAGEMENT, COMMUNICATION, EXAM_PREPARATION,
    PROGRAMMING, ENTERPRENEURSHIP, PROBLEM_SOLVING, DESIGN, WRITING_AND_RESEARCH,
    FINANCIAL_LITERACY, FOREIGN_LANGUAGES, ANALYTICAL_SKILLS;

    private static final Map<String, Skill> FORMAT_MAP = new HashMap<>();

    static {
        for (Skill skill : values()) {
            FORMAT_MAP.put(skill.name().toLowerCase().replace("_", " "), skill);
        }
    }

    @JsonCreator
    public static Skill fromString(String value) {
        Skill skill = FORMAT_MAP.get(value.toLowerCase());
        if (skill == null) {
            throw new IllegalArgumentException("Invalid skill: " + value);
        }
        return skill;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

