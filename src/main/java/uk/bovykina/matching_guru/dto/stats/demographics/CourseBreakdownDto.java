package uk.bovykina.matching_guru.dto.stats.demographics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseBreakdownDto {
    private String course;
    private long count;
}
