package uk.bovykina.matching_guru.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.Auth;
import uk.bovykina.matching_guru.repository.AuthRepository;
import uk.bovykina.matching_guru.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Auth auth = authRepository.findByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException("Authentication details not found for user: " + email));
        UserDetails userDetails = new CustomUserDetails(user, auth);
        log.info("👤 Loaded User: {}, Role: {}", userDetails.getUsername(), userDetails.getAuthorities());
        return userDetails;
    }
}
