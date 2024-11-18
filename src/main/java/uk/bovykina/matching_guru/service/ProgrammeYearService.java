package uk.bovykina.matching_guru.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
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

    @Transactional
    public ProgrammeYearResponseDto createProgrammeYear(ProgrammeYearCreateDto createDto) {
        Programme programme = programmeRepository.findById(createDto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        ProgrammeYear programmeYear = new ProgrammeYear();
        programmeYear.setProgramme(programme);
        programmeYear.setAcademicYear(createDto.getAcademicYear());
        programmeYear.setIsActive(true);
        programmeYear.setJoinCode(generateJoinCode());
        programmeYear.setCustomSettings(createDto.getCustomSettings());
        programmeYear.setPreferredAlgorithm(createDto.getPreferredAlgorithm());

        ProgrammeYear savedProgrammeYear = programmeYearRepository.save(programmeYear);
        return toProgrammeYearResponseDto(savedProgrammeYear);
    }

    public ProgrammeYearResponseDto getProgrammeYear(Long id) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));
        return toProgrammeYearResponseDto(programmeYear);
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
        if (updateDto.getCustomSettings() != null) {
            programmeYear.setCustomSettings(updateDto.getCustomSettings());
        }
        programmeYear.setIsActive(updateDto.isActive());

        ProgrammeYear updatedProgrammeYear = programmeYearRepository.save(programmeYear);
        return toProgrammeYearResponseDto(updatedProgrammeYear);
    }

    private ProgrammeYearResponseDto toProgrammeYearResponseDto(ProgrammeYear programmeYear) {
        ProgrammeYearResponseDto dto = new ProgrammeYearResponseDto();
        dto.setId(programmeYear.getId());
        dto.setProgrammeId(programmeYear.getProgramme().getId());
        dto.setProgrammeName(programmeYear.getProgramme().getName());
        dto.setAcademicYear(programmeYear.getAcademicYear());
        dto.setActive(programmeYear.getIsActive());
        dto.setJoinCode(programmeYear.getJoinCode());
        dto.setCustomSettings(programmeYear.getCustomSettings());
        dto.setPreferredAlgorithm(programmeYear.getPreferredAlgorithm());
        return dto;
    }

    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 8); // Generates a unique 8-character join code
    }
}
