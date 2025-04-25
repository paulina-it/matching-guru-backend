package uk.bovykina.matching_guru.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WeeklyEngagementPoint {
    private String week;
    private int interactions;
}
