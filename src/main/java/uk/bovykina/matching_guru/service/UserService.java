package uk.bovykina.matching_guru.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtService;
    private final CloudinaryService cloudinaryService;
    private final UserMapper userMapper = new UserMapper();

    /**
     * Uploads a profile image for a user.
     */
    public String uploadProfileImage(String email, MultipartFile file) throws IOException {
        log.info("📸 Uploading profile image for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ User not found with email: {}", email);
                    return new UserNotFoundException("User not found.");
                });

        String imageUrl = cloudinaryService.uploadImage(file);
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        log.info("✅ Profile image uploaded successfully for email: {}", email);
        return imageUrl;
    }

    /**
     * Registers a new user.
     */
    public UserDto createUser(UserCreateDto userCreateDto) {
        log.info("📝 Attempting to create user with email: {}", userCreateDto.getEmail());

        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            log.warn("⚠️ User with email {} already exists!", userCreateDto.getEmail());
            throw new IllegalArgumentException("User with this email already exists.");
        }

        User user = userMapper.toUser(userCreateDto);

        if (userCreateDto.getJoinCode() != null) {
            log.info("🔎 Finding organisation by join code: {}", userCreateDto.getJoinCode());
            Organisation organisation = organisationRepository.findByJoinCode(userCreateDto.getJoinCode())
                    .orElseThrow(() -> {
                        log.error("❌ Invalid join code: {}", userCreateDto.getJoinCode());
                        return new IllegalArgumentException("Invalid join code.");
                    });
            user.setOrganisation(organisation);
        }

        User savedUser = userRepository.save(user);
        log.info("✅ User created successfully: {}", savedUser.getId());

        Auth auth = new Auth();
        auth.setUser(savedUser);
        auth.setPasswordHash(passwordEncoder.encode(userCreateDto.getPassword()));
        auth.setLastLogin(LocalDateTime.now());
        authRepository.save(auth);
        log.info("🔒 Auth details saved for user: {}", savedUser.getEmail());

        return userMapper.toUserDto(savedUser);
    }

    /**
     * Updates a user’s details.
     */
    public UserResponseDto updateUser(UserUpdateDto updateDto) {
        log.info("🔄 Updating user with ID: {}", updateDto.getId());

        User user = userRepository.findById(updateDto.getId())
                .orElseThrow(() -> {
                    log.warn("⚠️ User not found with ID: {}", updateDto.getId());
                    return new UserNotFoundException("User not found.");
                });

        if (updateDto.getFirstName() != null) user.setFirstName(updateDto.getFirstName());
        if (updateDto.getLastName() != null) user.setLastName(updateDto.getLastName());
        if (updateDto.getEmail() != null) user.setEmail(updateDto.getEmail());

        User savedUser = userRepository.save(user);
        log.info("✅ User updated successfully: {}", savedUser.getId());
        return userMapper.toUserResponseDto(savedUser);
    }

    /**
     * Deletes a user by ID.
     */
    public void deleteUser(Long id) {
        log.info("🗑️ Attempting to delete user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("⚠️ User not found with ID: {}", id);
            throw new UserNotFoundException("User not found.");
        }

        userRepository.deleteById(id);
        log.info("✅ User deleted successfully: {}", id);
    }

    /**
     * Fetches a user by email.
     */
    public UserResponseDto getUserByEmail(String email) {
        log.info("🔍 Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toUserResponseDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Fetches a user by ID.
     */
    public UserResponseDto getUserById(Long id) {
        log.info("🔍 Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toUserResponseDto)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    /**
     * Fetches all users with pagination.
     */
    public Page<UserResponseDto> getAllUsers(int page, int size) {
        log.info("📌 Fetching users - Page: {}, Size: {}", page, size);
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));
        return users.map(userMapper::toUserResponseDto);
    }

    /**
     * Handles user login with JWT role claim.
     */
    public LoginResponse login(String email, String password) {
        log.info("🔑 User attempting to log in: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("⚠️ User not found for login: {}", email);
                    return new UserNotFoundException("User not found.");
                });

        Auth auth = authRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("❌ Auth record not found for user: {}", email);
                    return new UserNotFoundException("Auth record not found.");
                });

        if (!passwordEncoder.matches(password, auth.getPasswordHash())) {
            log.warn("⚠️ Invalid credentials for user: {}", email);
            throw new IllegalArgumentException("Invalid credentials.");
        }

        String token = jwtService.generateToken(email, user.getRole().name());
        log.info("✅ User logged in successfully: {}", email);

        UserResponseDto userDto = userMapper.toUserResponseDto(user);
        return new LoginResponse(token, userDto);
    }

    /**
     * Resets a user’s password.
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("🔒 Resetting password for user ID: {}", userId);

        Auth auth = authRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("❌ Auth record not found for user ID: {}", userId);
                    return new UserNotFoundException("Auth record not found.");
                });

        auth.setPasswordHash(passwordEncoder.encode(newPassword));
        authRepository.save(auth);
        log.info("✅ Password reset successful for user ID: {}", userId);
    }


    private static class UserMapper {
        private static final Logger log = LoggerFactory.getLogger(UserMapper.class);

        User toUser(UserCreateDto userCreateDto) {
            log.info("Mapping UserCreateDto to User entity for email: {}", userCreateDto.getEmail());
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
            user.setAgeGroup(userCreateDto.getAgeGroup());
            user.setHomeCountry(userCreateDto.getHomeCountry());
            user.setLivingArrangement(userCreateDto.getLivingArrangement());
            user.setDisability(userCreateDto.getDisability());
            user.setProfileImageUrl(userCreateDto.getProfileImageUrl());
            log.info("Successfully mapped UserCreateDto to User entity for email: {}", userCreateDto.getEmail());
            return user;
        }

        UserDto toUserDto(User user) {
            log.info("Converting User entity to UserDto for user ID: {}", user.getId());
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
            userDto.setAgeGroup(user.getAgeGroup());
            userDto.setHomeCountry(user.getHomeCountry());
            userDto.setLivingArrangement(user.getLivingArrangement());
            userDto.setDisability(user.getDisability());
            userDto.setProfileImageUrl(user.getProfileImageUrl());
            log.info("Successfully converted User entity to UserDto for user ID: {}", user.getId());
            return userDto;
        }

        UserResponseDto toUserResponseDto(User user) {
            log.info("Transforming User entity to UserResponseDto for user ID: {}", user.getId());

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
            userDto.setAgeGroup(user.getAgeGroup());
            userDto.setHomeCountry(user.getHomeCountry());
            userDto.setLivingArrangement(user.getLivingArrangement());
            userDto.setDisability(user.getDisability());
            userDto.setProfileImageUrl(user.getProfileImageUrl());

            if (user.getOrganisation() != null) {
                userDto.setOrganisationId(user.getOrganisation().getId());
                userDto.setOrganisationName(user.getOrganisation().getName());
                log.info("User ID: {} belongs to Organisation ID: {}", user.getId(), user.getOrganisation().getId());
            }

            if (user.getParticipations() != null) {
                userDto.setParticipations(
                        user.getParticipations().stream()
                                .map(part -> new UserParticipationDto(
                                        part.getProgrammeYear().getAcademicYear(),
                                        part.getRole()
                                ))
                                .collect(Collectors.toList())
                );
            } else {
                userDto.setParticipations(new ArrayList<>());
            }

            log.info("Successfully transformed User entity to UserResponseDto for user ID: {}", user.getId());
            return userDto;
        }
    }
}
