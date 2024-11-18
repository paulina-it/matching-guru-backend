package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.bovykina.matching_guru.entity.enums.Gender;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "userDemographics")
public class UserDemographics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long demographicsId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    @ToString.Exclude
    private User user;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Integer age;
    private String ethnicity;
    private String nationality;
    private String homeCountry;
    private String homeCity;
    private String livingArrangement;
    private String disability;

    private String dbsCertificate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDemographics)) return false;
        UserDemographics that = (UserDemographics) o;
        return demographicsId != null && demographicsId.equals(that.getDemographicsId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
