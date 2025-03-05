package uk.bovykina.matching_guru.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum AgeGroup {
    AGE_18_20, AGE_21_24, AGE_25_29, AGE_30_PLUS;

    private static final Map<String, AgeGroup> FORMAT_MAP = new HashMap<>();

    static {
        for (AgeGroup ageGroup : values()) {
            FORMAT_MAP.put(ageGroup.name(), ageGroup);
            FORMAT_MAP.put(ageGroup.name().replace("AGE_", "").replace("_", "-"), ageGroup); // Support "21-24"
        }
    }

    @JsonCreator
    public static AgeGroup fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        AgeGroup ageGroup = FORMAT_MAP.get(value);
        if (ageGroup == null) {
            throw new IllegalArgumentException("Invalid age group: " + value);
        }
        return ageGroup;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
