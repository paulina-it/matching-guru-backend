package uk.bovykina.matching_guru.dto.programme;

import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.CriterionType;

@Getter
@Setter
public class MatchingCriteriaDto {
    private CriterionType criterionType;
    private Integer weight;

}