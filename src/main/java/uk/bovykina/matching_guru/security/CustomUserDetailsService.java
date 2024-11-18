package uk.bovykina.matching_guru.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.Auth;
import uk.bovykina.matching_guru.repository.AuthRepository;
import uk.bovykina.matching_guru.repository.UserRepository;

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

        return new CustomUserDetails(user, auth);
    }
}
