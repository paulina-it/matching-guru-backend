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
import uk.bovykina.matching_guru.mapper.UserMapper;
import uk.bovykina.matching_guru.repository.AuthRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
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
    private final UserMapper userMapper;

    /**
     * Uploads a profile image and returns the image URL.
     */
    public String uploadProfileImage(MultipartFile file) throws IOException {
        return cloudinaryService.uploadImage(file, "profile_pictures");
    }

    /**
     * Registers a new user with an optional profile image.
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

        if (userCreateDto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(userCreateDto.getProfileImageUrl());
            log.info("✅ Profile image linked for user: {}", userCreateDto.getEmail());
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

        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(updateDto.getEmail()).isPresent()) {
                log.warn("⚠️ Email {} already exists!", updateDto.getEmail());
                throw new IllegalArgumentException("Email already in use.");
            }
            user.setEmail(updateDto.getEmail());
        }

        if (updateDto.getFirstName() != null) user.setFirstName(updateDto.getFirstName());
        if (updateDto.getLastName() != null) user.setLastName(updateDto.getLastName());
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
        if (updateDto.getProfileImageUrl() != null) user.setProfileImageUrl(updateDto.getProfileImageUrl());
        if (updateDto.getAgeGroup() != null) user.setAgeGroup(updateDto.getAgeGroup());

        if (updateDto.getOrganisationId() != null) {
            Organisation organisation = organisationRepository.findById(updateDto.getOrganisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
            user.setOrganisation(organisation);
        }

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
    public void changeOwnPassword(Long userId, String oldPassword, String newPassword) {
        log.info("🔐 User-initiated password change for user ID: {}", userId);

        Auth auth = authRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("❌ Auth record not found for user ID: {}", userId);
                    return new UserNotFoundException("Auth record not found.");
                });

        if (!passwordEncoder.matches(oldPassword, auth.getPasswordHash())) {
            log.warn("❌ Incorrect old password provided by user ID: {}", userId);
            throw new IllegalArgumentException("Old password is incorrect");
        }

        auth.setPasswordHash(passwordEncoder.encode(newPassword));
        authRepository.save(auth);
        log.info("✅ Password successfully changed for user ID: {}", userId);
    }

}
