package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.Eligibility;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.repository.EligibilityRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EligibilityService {

    private final EligibilityRepository eligibilityRepository;
    private final ProgrammeRepository programmeRepository;
    private final EligibilityMapper eligibilityMapper = new EligibilityMapper();

    public EligibilityDto createEligibility(EligibilityCreateDto dto) {
        Programme programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        Eligibility eligibility = eligibilityMapper.toEntity(dto, programme);
        eligibility = eligibilityRepository.save(eligibility);
        return eligibilityMapper.toDto(eligibility);
    }

    public EligibilityDto updateEligibility(Long id, EligibilityCreateDto dto) {
        Eligibility eligibility = eligibilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eligibility record not found"));

        Programme programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        if (!eligibility.getProgramme().equals(programme)) {
            eligibility.setProgramme(programme);
        }
        if (!eligibility.getAcademicStage().equals(dto.getAcademicStage())) {
            eligibility.setAcademicStage(dto.getAcademicStage());
        }
        if (!eligibility.getRole().equals(dto.getRole())) {
            eligibility.setRole(dto.getRole());
        }

        eligibility = eligibilityRepository.save(eligibilityMapper.toEntity(dto, programme));
        return eligibilityMapper.toDto(eligibility);
    }

    public List<EligibilityDto> getEligibilityByProgramme(Long programmeId) {
        return eligibilityRepository.findByProgrammeId(programmeId).stream()
                .map(eligibilityMapper::toDto)
                .collect(Collectors.toList());
    }


    private static class EligibilityMapper {
        public EligibilityDto toDto(Eligibility eligibility) {
            EligibilityDto dto = new EligibilityDto();
            dto.setId(eligibility.getId());
            dto.setProgrammeId(eligibility.getProgramme().getId());
            dto.setAcademicStage(eligibility.getAcademicStage());
            dto.setRole(eligibility.getRole());
            return dto;
        }

        public Eligibility toEntity(EligibilityCreateDto dto, Programme programme) {
            Eligibility eligibility = new Eligibility();
            eligibility.setProgramme(programme);
            eligibility.setAcademicStage(dto.getAcademicStage());
            eligibility.setRole(dto.getRole());
            return eligibility;
        }
    }
}
