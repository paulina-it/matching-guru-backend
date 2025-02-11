package uk.bovykina.matching_guru.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.user.*;
import uk.bovykina.matching_guru.entity.Auth;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.enums.UserRole;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.repository.AuthRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.UserRepository;
import uk.bovykina.matching_guru.util.CloudinaryService;
import uk.bovykina.matching_guru.util.JwtUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtService;
    private final UserMapper userMapper = new UserMapper();

    @Value("${jwt.secret}")
    private String jwtSecret;
    private final CloudinaryService cloudinaryService;

    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String imageUrl = cloudinaryService.uploadImage(file);
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    public boolean isJoinCodeValid(String joinCode) {
        return organisationRepository.existsByJoinCode(joinCode);
    }

    public UserDto createUser(UserCreateDto userCreateDto) {
        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        if (userCreateDto.getRole() == UserRole.ADMIN && userCreateDto.getUniEmail() == null) {
            userCreateDto.setUniEmail(userCreateDto.getEmail());
        }

        User user = userMapper.toUser(userCreateDto);

        if (userCreateDto.getJoinCode() != null) {
            Organisation organisation = organisationRepository.findByJoinCode(userCreateDto.getJoinCode())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid join code."));
            user.setOrganisation(organisation);
        }

        User savedUser = userRepository.save(user);
        System.out.println("User saved: " + savedUser);

        // Create Auth record
        Auth auth = new Auth();
        auth.setUser(savedUser);
        auth.setPasswordHash(passwordEncoder.encode(userCreateDto.getPassword()));
        auth.setLastLogin(LocalDateTime.now());
        authRepository.save(auth);
        System.out.println("Auth details saved for user: " + savedUser.getEmail());

        return userMapper.toUserDto(savedUser);
    }

    public UserDto updateUser(UserUpdateDto updateDto) {
        User user = userRepository.findById(updateDto.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + updateDto.getId()));

        if (updateDto.getFirstName() != null) user.setFirstName(updateDto.getFirstName());
        if (updateDto.getLastName() != null) user.setLastName(updateDto.getLastName());
        if (updateDto.getEmail() != null) user.setEmail(updateDto.getEmail());
        if (updateDto.getUniEmail() != null) user.setUniEmail(updateDto.getUniEmail());
        if (updateDto.getStudentNumber() != null) user.setStudentNumber(updateDto.getStudentNumber());
        if (updateDto.getRole() != null) user.setRole(updateDto.getRole());
        if (updateDto.getPersonalityType() != null) user.setPersonalityType(updateDto.getPersonalityType());
        if (updateDto.getGender() != null) user.setGender(updateDto.getGender());
        if (updateDto.getEthnicity() != null) user.setEthnicity(updateDto.getEthnicity());
        if (updateDto.getNationality() != null) user.setNationality(updateDto.getNationality());
        if (updateDto.getHomeCountry() != null) user.setHomeCountry(updateDto.getHomeCountry());
        if (updateDto.getLivingArrangement() != null) user.setLivingArrangement(updateDto.getLivingArrangement());
        if (updateDto.getDisability() != null) user.setDisability(updateDto.getDisability());

        User savedUser = userRepository.save(user);
        return userMapper.toUserDto(savedUser);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        Auth auth = authRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Auth record not found for user."));
        auth.setPasswordHash(passwordEncoder.encode(newPassword));
        authRepository.save(auth);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }


    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Auth auth = authRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Auth record not found"));

        if (!passwordEncoder.matches(password, auth.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(email, jwtSecret);

        UserResponseDto userDto = userMapper.toUserResponseDto(user);

        return new LoginResponse(token, userDto);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toUserResponseDto(user);
    }

    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponseDto(user);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    private static class UserMapper {

        User toUser(UserCreateDto userCreateDto) {
            User user = new User();
            user.setFirstName(userCreateDto.getFirstName());
            user.setLastName(userCreateDto.getLastName());
            user.setEmail(userCreateDto.getEmail());
            user.setUniEmail(userCreateDto.getUniEmail());
            user.setStudentNumber(userCreateDto.getStudentNumber());
            user.setRole(userCreateDto.getRole());
            user.setPersonalityType(userCreateDto.getPersonalityType());
            user.setGender(userCreateDto.getGender());
            user.setEthnicity(userCreateDto.getEthnicity());
            user.setNationality(userCreateDto.getNationality());
            user.setHomeCountry(userCreateDto.getHomeCountry());
            user.setLivingArrangement(userCreateDto.getLivingArrangement());
            user.setDisability(userCreateDto.getDisability());
            return user;
        }

        UserDto toUserDto(User user) {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDto.setUniEmail(user.getUniEmail());
            userDto.setStudentNumber(user.getStudentNumber());
            userDto.setRole(user.getRole());
            userDto.setPersonalityType(user.getPersonalityType());
            userDto.setGender(user.getGender());
            userDto.setEthnicity(user.getEthnicity());
            userDto.setNationality(user.getNationality());
            userDto.setHomeCountry(user.getHomeCountry());
            userDto.setLivingArrangement(user.getLivingArrangement());
            userDto.setDisability(user.getDisability());
            return userDto;
        }

        UserResponseDto toUserResponseDto(User user) {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(user.getId());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDto.setUniEmail(user.getUniEmail());
            userDto.setStudentNumber(user.getStudentNumber());
            userDto.setRole(user.getRole());
            userDto.setPersonalityType(user.getPersonalityType());
            userDto.setGender(user.getGender());
            userDto.setEthnicity(user.getEthnicity());
            userDto.setNationality(user.getNationality());
            userDto.setHomeCountry(user.getHomeCountry());
            userDto.setLivingArrangement(user.getLivingArrangement());
            userDto.setDisability(user.getDisability());

            if (user.getOrganisation() != null) {
                userDto.setOrganisationId(user.getOrganisation().getId());
                userDto.setOrganisationName(user.getOrganisation().getName());
            }

            userDto.setParticipations(
                    user.getParticipations().stream()
                            .map(part -> new UserParticipationDto(
                                    part.getProgrammeYear().getAcademicYear(),
                                    part.getRole()
                            ))
                            .collect(Collectors.toList())
            );

            return userDto;
        }
    }
}
