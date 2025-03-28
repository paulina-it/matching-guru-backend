package uk.bovykina.matching_guru.dto.dashboards;

import java.time.LocalDateTime;

public interface ActivityJoinProjection {
    Long getCount();
    String getProgrammeYearName();
    LocalDateTime getTimestamp();
}