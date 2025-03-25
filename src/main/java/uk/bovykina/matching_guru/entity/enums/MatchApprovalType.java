package uk.bovykina.matching_guru.entity.enums;

public enum MatchApprovalType {
    AUTO, MANUAL, THRESHOLD;

    public static MatchApprovalType fromString(String value) {
        if (value == null) return null;
        try {
            return MatchApprovalType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
