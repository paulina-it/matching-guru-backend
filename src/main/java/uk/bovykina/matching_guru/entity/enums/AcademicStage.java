package uk.bovykina.matching_guru.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AcademicStage {
    FOUNDATION,
    PG_PHD,
    PLACEMENT,
    INCOMING,
    FINAL_YEAR_P,
    SECOND_YEAR,
    PG_MASTERS,
    GRADUATE,
    FIRST_YEAR,
    FINAL_YEAR,
    SECOND_YEAR_P;

    @JsonCreator
    public static AcademicStage fromValue(String value) {
        switch (value) {
            case "Foundation":
                return FOUNDATION;
            case "First Year Undergraduate":
                return FIRST_YEAR;
            case "Second Year Undergraduate":
                return SECOND_YEAR;
            case "Placement Year":
                return PLACEMENT;
            case "Final Year Undergraduate":
                return FINAL_YEAR;
            case "Postgraduate Masters":
                return PG_MASTERS;
            case "Postgraduate PhD":
                return PG_PHD;
            case "Graduate":
                return GRADUATE;
            default:
                throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        switch (this) {
            case FOUNDATION:
                return "Foundation";
            case FIRST_YEAR:
                return "First Year Undergraduate";
            case SECOND_YEAR:
                return "Second Year Undergraduate";
            case PLACEMENT:
                return "Placement Year";
            case FINAL_YEAR:
                return "Final Year Undergraduate";
            case PG_MASTERS:
                return "Postgraduate Masters";
            case PG_PHD:
                return "Postgraduate PhD";
            case GRADUATE:
                return "Graduate";
            default:
                return this.name();
        }
    }

    public int getLevel() {
        switch (this) {
            case INCOMING: return 0;
            case FOUNDATION: return 0;
            case FIRST_YEAR: return 1;
            case SECOND_YEAR: return 2;
            case SECOND_YEAR_P: return 2;
            case PLACEMENT: return 3;
            case FINAL_YEAR: return 4;
            case FINAL_YEAR_P: return 4;
            case PG_MASTERS: return 5;
            case PG_PHD: return 6;
            case GRADUATE: return 7;
            default: return -1;
        }
    }

}
