package uk.bovykina.matching_guru.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.user.*;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.repository.CourseRepository;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final CourseRepository courseRepository;

    public User toUser(UserCreateDto userCreateDto) {
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

        if (userCreateDto.getCourseId() != null) {
            courseRepository.findById(userCreateDto.getCourseId())
                    .ifPresent(user::setCourse);
        }

        log.info("Successfully mapped UserCreateDto to User entity for email: {}", userCreateDto.getEmail());
        return user;
    }

    public UserDto toUserDto(User user) {
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
        userDto.setCourse(user.getCourse());

        log.info("Successfully converted User entity to UserDto for user ID: {}", user.getId());
        return userDto;
    }

    public UserResponseDto toUserResponseDto(User user) {
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

        if (user.getCourse() != null) {
            userDto.setCourseId(user.getCourse().getId());
            userDto.setCourseName(user.getCourse().getName());
        }

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
                                    part.getRole()))
                            .collect(Collectors.toList())
            );
        } else {
            userDto.setParticipations(new ArrayList<>());
        }

        log.info("Successfully transformed User entity to UserResponseDto for user ID: {}", user.getId());
        return userDto;
    }

    public UserSummaryDto toUserSummaryDto(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
