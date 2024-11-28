package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.CriterionType;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;
import uk.bovykina.matching_guru.repository.ProgrammeYearRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgrammeYearService {

    private final ProgrammeYearRepository programmeYearRepository;
    private final ProgrammeRepository programmeRepository;
    private final ProgrammeMatchingCriteriaRepository matchingCriteriaRepository;

    @Transactional
    public ProgrammeYearResponseDto createProgrammeYear(ProgrammeYearCreateDto createDto) {
        Programme programme = programmeRepository.findById(createDto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        ProgrammeYear programmeYear = new ProgrammeYear();
        programmeYear.setProgramme(programme);
        programmeYear.setAcademicYear(createDto.getAcademicYear());
        programmeYear.setIsActive(true);
        programmeYear.setJoinCode(generateJoinCode());
//        programmeYear.setCustomSettings(createDto.getCustomSettings());
        programmeYear.setPreferredAlgorithm(createDto.getPreferredAlgorithm());

        ProgrammeYear savedProgrammeYear = programmeYearRepository.save(programmeYear);

        // Save matching criteria
        if (createDto.getMatchingCriteria() != null) {
            saveMatchingCriteria(savedProgrammeYear, createDto.getMatchingCriteria());
        }

        return toProgrammeYearResponseDto(savedProgrammeYear);
    }

    public ProgrammeYearResponseDto getProgrammeYear(Long id) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        ProgrammeYearResponseDto responseDto = toProgrammeYearResponseDto(programmeYear);
        responseDto.setMatchingCriteria(
                matchingCriteriaRepository.findByProgrammeYearId(programmeYear.getId()).stream()
                        .map(this::toMatchingCriteriaDto)
                        .collect(Collectors.toList())
        );

        return responseDto;
    }

    public List<ProgrammeYearResponseDto> getAllProgrammeYears() {
        return programmeYearRepository.findAll().stream()
                .map(this::toProgrammeYearResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgrammeYearResponseDto updateProgrammeYear(Long id, ProgrammeYearUpdateDto updateDto) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        if (updateDto.getAcademicYear() != null) {
            programmeYear.setAcademicYear(updateDto.getAcademicYear());
        }
        if (updateDto.getPreferredAlgorithm() != null) {
            programmeYear.setPreferredAlgorithm(updateDto.getPreferredAlgorithm());
        }
//        if (updateDto.getCustomSettings() != null) {
//            programmeYear.setCustomSettings(updateDto.getCustomSettings());
//        }
        programmeYear.setIsActive(updateDto.isActive());

        // Update matching criteria if provided
        if (updateDto.getMatchingCriteria() != null) {
            saveMatchingCriteria(programmeYear, updateDto.getMatchingCriteria());
        }

        ProgrammeYear updatedProgrammeYear = programmeYearRepository.save(programmeYear);
        return toProgrammeYearResponseDto(updatedProgrammeYear);
    }

    private void saveMatchingCriteria(ProgrammeYear programmeYear, List<MatchingCriteriaDto> criteriaDtos) {
        // Validate total weight
        int totalWeight = criteriaDtos.stream()
                .mapToInt(MatchingCriteriaDto::getWeight)
                .sum();

        if (totalWeight != 100) {
            throw new IllegalArgumentException("The total weight of all criteria must equal 100%");
        }

        // Clear existing criteria for this programme year
        matchingCriteriaRepository.deleteByProgrammeYearId(programmeYear.getId());

        // Save new criteria
        List<ProgrammeMatchingCriteria> criteria = criteriaDtos.stream()
                .map(dto -> {
                    ProgrammeMatchingCriteria criterion = new ProgrammeMatchingCriteria();
                    criterion.setProgrammeYear(programmeYear);
                    criterion.setCriterionType(dto.getCriterionType());
                    criterion.setWeight(dto.getWeight());
                    return criterion;
                })
                .collect(Collectors.toList());

        matchingCriteriaRepository.saveAll(criteria);
    }

    private ProgrammeYearResponseDto toProgrammeYearResponseDto(ProgrammeYear programmeYear) {
        ProgrammeYearResponseDto dto = new ProgrammeYearResponseDto();
        dto.setId(programmeYear.getId());
        dto.setProgrammeId(programmeYear.getProgramme().getId());
        dto.setProgrammeName(programmeYear.getProgramme().getName());
        dto.setAcademicYear(programmeYear.getAcademicYear());
        dto.setIsActive(programmeYear.getIsActive());
        dto.setJoinCode(programmeYear.getJoinCode());
//        dto.setCustomSettings(programmeYear.getCustomSettings());
        dto.setPreferredAlgorithm(programmeYear.getPreferredAlgorithm());
        return dto;
    }

    private MatchingCriteriaDto toMatchingCriteriaDto(ProgrammeMatchingCriteria criterion) {
        MatchingCriteriaDto dto = new MatchingCriteriaDto();
        dto.setCriterionType(criterion.getCriterionType());
        dto.setWeight(criterion.getWeight());
        return dto;
    }

    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 8); // Generates a unique 8-character join code
    }

    @Transactional
    public void deleteProgrammeYear(Long id) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));
        programmeYearRepository.delete(programmeYear);
    }

}
