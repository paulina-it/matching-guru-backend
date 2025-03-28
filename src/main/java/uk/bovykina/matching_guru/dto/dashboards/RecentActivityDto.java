package uk.bovykina.matching_guru.dto.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentActivityDto {
    private String description;
    private LocalDateTime timestamp;
}
