package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.Eligibility;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.mapper.EligibilityMapper;
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
    private final EligibilityMapper eligibilityMapper;

    /**
     * Creates an eligibility rule for a programme.
     */
    public EligibilityDto createEligibility(EligibilityCreateDto dto) {
        Programme programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        Eligibility eligibility = eligibilityMapper.toEntity(dto, programme);
        eligibility = eligibilityRepository.save(eligibility);
        return eligibilityMapper.toDto(eligibility);
    }

    /**
     * Updates an existing eligibility rule.
     */
    public EligibilityDto updateEligibility(Long id, EligibilityCreateDto dto) {
        Eligibility existing = eligibilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eligibility record not found"));

        Programme programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        Eligibility updated = eligibilityMapper.toEntity(dto, programme);
        updated.setId(existing.getId());

        return eligibilityMapper.toDto(eligibilityRepository.save(updated));
    }

    /**
     * Retrieves all eligibility rules for a given programme.
     */
    public List<EligibilityDto> getEligibilityByProgramme(Long programmeId) {
        return eligibilityRepository.findByProgrammeId(programmeId).stream()
                .map(eligibilityMapper::toDto)
                .collect(Collectors.toList());
    }
}
