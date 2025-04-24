package uk.bovykina.matching_guru.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.dto.user.UserSummaryDto;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.enums.UserRole;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    Page<User> findByOrganisationId(Long organisationId, Pageable pageable);

    Optional<User> findByEmail(String email);

    @Query("""
    SELECT new uk.bovykina.matching_guru.dto.user.UserSummaryDto(
        u.id,
        u.firstName,
        u.lastName,
        u.email,
        u.role
    )
    FROM User u
    LEFT JOIN Auth a ON a.user = u
    WHERE u.organisation.id = :organisationId
      AND (:role IS NULL OR u.role = :role)
      AND LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<UserSummaryDto> findUsersByOrganisationWithFilters(
            @Param("organisationId") Long organisationId,
            @Param("search") String search,
            @Param("role") UserRole role,
            Pageable pageable
    );

    Optional<User> findByUniEmail(String uniEmail);

    Optional<User> findByStudentNumber(String studentNumber);
}
