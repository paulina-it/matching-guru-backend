package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.Auth;
import uk.bovykina.matching_guru.entity.User;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByUser(User user);

    Optional<Auth> findByUserEmail(String email);
    Optional<Auth> findByUserId(Long userId);
}
