package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.bovykina.matching_guru.dto.user.*;
import uk.bovykina.matching_guru.entity.Auth;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.enums.*;
import uk.bovykina.matching_guru.exception.UserNotFoundException;
import uk.bovykina.matching_guru.mapper.UserMapper;
import uk.bovykina.matching_guru.repository.AuthRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.UserRepository;
import uk.bovykina.matching_guru.util.CloudinaryService;
import uk.bovykina.matching_guru.util.JwtUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthRepository authRepository;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    private User user;
    private Auth auth;
    private UserCreateDto createDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);

        auth = new Auth();
        auth.setPasswordHash("hashedpassword");
        auth.setUser(user);
        auth.setLastLogin(LocalDateTime.now());

        createDto = new UserCreateDto();
        createDto.setFirstName("Test");
        createDto.setLastName("User");
        createDto.setEmail("test@example.com");
        createDto.setPassword("securepass123");
        createDto.setRole(UserRole.USER);
        createDto.setGender(Gender.FEMALE);
        createDto.setPersonalityType(PersonalityType.ADVENTURER_ISFP);
        createDto.setAge(21);
        createDto.setLivingArrangement(LivingArrangement.ON_CAMPUS);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toUser(createDto)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode("securepass123")).thenReturn("hashedpassword");
        when(userMapper.toUserDto(user)).thenReturn(new UserDto());

        UserDto result = userService.createUser(createDto);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(authRepository).save(any(Auth.class));
    }

    @Test
    void getUserByEmail_shouldReturnDto() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
    }

    @Test
    void getUserById_shouldReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.getUserById(1L);

        assertNotNull(result);
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void login_shouldReturnTokenAndUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authRepository.findByUser(user)).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("securepass123", "hashedpassword")).thenReturn(true);
        when(jwtUtils.generateToken("test@example.com", user.getRole().name())).thenReturn("jwt-token");
        when(userMapper.toUserResponseDto(user)).thenReturn(new UserResponseDto());

        LoginResponse response = userService.login("test@example.com", "securepass123");

        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
    }

    @Test
    void resetPassword_shouldUpdateAuthRecord() {
        when(authRepository.findByUserId(1L)).thenReturn(Optional.of(auth));
        when(passwordEncoder.encode("newpassword")).thenReturn("newhashed");
        ArgumentCaptor<Auth> captor = ArgumentCaptor.forClass(Auth.class);
        verify(authRepository).save(captor.capture());
    }

    @Test
    void getAllUsers_shouldReturnPagedResults() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(userMapper.toUserResponseDto(user)).thenReturn(new UserResponseDto());

        Page<UserResponseDto> result = userService.getAllUsers(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateUser_shouldUpdateFields() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setId(1L);
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(new UserResponseDto());

        UserResponseDto result = userService.updateUser(updateDto);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_shouldThrowIfInvalidJoinCode() {
        createDto.setJoinCode("invalid123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toUser(createDto)).thenReturn(user);
        when(organisationRepository.findByJoinCode("invalid123")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(createDto));
    }
}
