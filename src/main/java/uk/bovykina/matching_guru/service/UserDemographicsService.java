package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import uk.bovykina.matching_guru.dto.user.*;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.UserDemographics;
import uk.bovykina.matching_guru.repository.UserDemographicsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDemographicsService {

    @Autowired
    private UserDemographicsRepository userDemographicsRepository;
    @Autowired
    private final UserRepository userRepository;

    public UserDemographicsDto createUserDemographics(UserDemographicsCreateDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDemographics demographics = new UserDemographics();
        demographics.setUser(user);
        demographics.setGender(dto.getGender());
        demographics.setAge(dto.getAge());
        demographics.setEthnicity(dto.getEthnicity());
        demographics.setNationality(dto.getNationality());
        demographics.setHomeCountry(dto.getHomeCountry());
        demographics.setHomeCity(dto.getHomeCity());
        demographics.setLivingArrangement(dto.getLivingArrangement());
        demographics.setDisability(dto.getDisability());
        demographics.setDbsCertificate(dto.getDbsCertificate());

        UserDemographics savedDemographics = userDemographicsRepository.save(demographics);
        return convertToDto(savedDemographics);
    }

    public Optional<UserDemographicsDto> getUserDemographics(Long demographicsId) {
        return userDemographicsRepository.findById(demographicsId).map(this::convertToDto);
    }

    public UserDemographicsDto updateUserDemographics(Long demographicsId, UserDemographicsUpdateDto dto) {
        UserDemographics demographics = userDemographicsRepository.findById(demographicsId)
                .orElseThrow(() -> new IllegalArgumentException("UserDemographics not found"));

        demographics.setGender(dto.getGender());
        demographics.setAge(dto.getAge());
        demographics.setEthnicity(dto.getEthnicity());
        demographics.setNationality(dto.getNationality());
        demographics.setHomeCountry(dto.getHomeCountry());
        demographics.setHomeCity(dto.getHomeCity());
        demographics.setLivingArrangement(dto.getLivingArrangement());
        demographics.setDisability(dto.getDisability());
        demographics.setDbsCertificate(dto.getDbsCertificate());

        UserDemographics updatedDemographics = userDemographicsRepository.save(demographics);
        return convertToDto(updatedDemographics);
    }

    private UserDemographicsDto convertToDto(UserDemographics demographics) {
        UserDemographicsDto dto = new UserDemographicsDto();
        dto.setDemographicsId(demographics.getDemographicsId());
        dto.setUserId(demographics.getUser().getId());
        dto.setGender(demographics.getGender());
        dto.setAge(demographics.getAge());
        dto.setEthnicity(demographics.getEthnicity());
        dto.setNationality(demographics.getNationality());
        dto.setHomeCountry(demographics.getHomeCountry());
        dto.setHomeCity(demographics.getHomeCity());
        dto.setLivingArrangement(demographics.getLivingArrangement());
        dto.setDisability(demographics.getDisability());
        dto.setDbsCertificate(demographics.getDbsCertificate());
        return dto;
    }
}
