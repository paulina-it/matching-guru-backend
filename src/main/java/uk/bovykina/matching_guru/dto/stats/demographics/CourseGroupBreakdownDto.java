package uk.bovykina.matching_guru.dto.stats.demographics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseGroupBreakdownDto {
    private String group;
    private int    count;
}
