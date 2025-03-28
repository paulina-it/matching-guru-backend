package uk.bovykina.matching_guru.dto.dashboards;

import java.time.LocalDateTime;

public interface ActivityJoinProjection {
    String getProgrammeYearName();
    Long getProgrammeYearId();
    Long getProgrammeId();
    int getCount();
    LocalDateTime getTimestamp();
}
