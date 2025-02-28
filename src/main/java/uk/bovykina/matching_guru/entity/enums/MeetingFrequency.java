package uk.bovykina.matching_guru.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum MeetingFrequency {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    BIWEEKLY("Biweekly"),
    LESS_OFTEN("Less Often");

    private final String formattedName;
    private static final Map<String, MeetingFrequency> FORMAT_MAP = new HashMap<>();

    static {
        for (MeetingFrequency frequency : values()) {
            FORMAT_MAP.put(frequency.formattedName.toLowerCase(), frequency); // Space-based format
            FORMAT_MAP.put(frequency.name().toLowerCase(), frequency); // Underscore-based format
        }
    }

    MeetingFrequency(String formattedName) {
        this.formattedName = formattedName;
    }

    @JsonCreator
    public static MeetingFrequency fromString(String value) {
        if (value == null) {
            return null;
        }
        MeetingFrequency frequency = FORMAT_MAP.get(value.toLowerCase());
        if (frequency == null) {
            throw new IllegalArgumentException("Invalid meeting frequency: " + value);
        }
        return frequency;
    }

    @JsonValue
    public String toValue() {
        return this.formattedName;
    }
}
