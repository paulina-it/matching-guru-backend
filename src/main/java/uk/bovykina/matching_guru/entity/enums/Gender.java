package uk.bovykina.matching_guru.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum Gender {
    OTHER, PREFER_NOT_TO_SAY, NON_BINARY, FEMALE, MALE;

    private static final Map<String, Gender> FORMAT_MAP = new HashMap<>();

    static {
        for (Gender gender : values()) {
            FORMAT_MAP.put(gender.name().toLowerCase(), gender);
        }
    }

    @JsonCreator
    public static Gender fromString(String value) {
        if (value == null) {
            return null;
        }
        Gender gender = FORMAT_MAP.get(value.toLowerCase());
        if (gender == null) {
            throw new IllegalArgumentException("Invalid gender: " + value);
        }
        return gender;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
