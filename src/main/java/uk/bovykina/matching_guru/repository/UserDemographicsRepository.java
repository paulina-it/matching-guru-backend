package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.UserDemographics;

public interface UserDemographicsRepository extends JpaRepository<UserDemographics, Long> {
}
