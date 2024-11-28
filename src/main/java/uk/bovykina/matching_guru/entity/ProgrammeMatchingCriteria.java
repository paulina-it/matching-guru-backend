package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uk.bovykina.matching_guru.entity.enums.CriterionType;


@Entity
@Getter
@Setter
@Table(name = "programmeMatchingCriteria")
public class ProgrammeMatchingCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CriterionType criterionType;

    private Integer weight;

    @ManyToOne
    @JoinColumn(name = "programme_year_id", nullable = false)
    private ProgrammeYear programmeYear;
}