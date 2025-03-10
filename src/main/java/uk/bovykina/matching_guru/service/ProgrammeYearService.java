package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.programme.MatchingCriteriaDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearResponseDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearUpdateDto;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;
import uk.bovykina.matching_guru.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgrammeYearService {

    private final ProgrammeYearRepository programmeYearRepository;
    private final ProgrammeRepository programmeRepository;
    private final ProgrammeMatchingCriteriaRepository matchingCriteriaRepository;
    private final ParticipantRepository participantRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public ProgrammeYearResponseDto createProgrammeYear(ProgrammeYearCreateDto createDto) {
        Programme programme = programmeRepository.findById(createDto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        ProgrammeYear programmeYear = new ProgrammeYear();
        programmeYear.setProgramme(programme);
        programmeYear.setAcademicYear(createDto.getAcademicYear());
        programmeYear.setIsActive(true);
        programmeYear.setJoinCode(generateJoinCode());
        programmeYear.setPreferredAlgorithm(createDto.getPreferredAlgorithm());
        programmeYear.generateFeedbackConfirmationCode();

        programmeYear.setMatchApprovalType(createDto.getMatchApprovalType());
        if (createDto.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
            if (createDto.getApprovalThreshold() == null || createDto.getApprovalThreshold() < 0 || createDto.getApprovalThreshold() > 100) {
                throw new IllegalArgumentException("Approval threshold must be between 0 and 100 when using THRESHOLD mode.");
            }
            programmeYear.setApprovalThreshold(createDto.getApprovalThreshold());
        } else {
            programmeYear.setApprovalThreshold(null);
        }

        ProgrammeYear savedProgrammeYear = programmeYearRepository.save(programmeYear);

        if (createDto.getMatchingCriteria() != null) {
            saveMatchingCriteria(savedProgrammeYear, createDto.getMatchingCriteria());
        }

        return toProgrammeYearResponseDto(savedProgrammeYear);
    }

    public  ProgrammeYear getById(Long id) {
        return programmeYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProgrammeYear not found"));
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

    @Transactional(readOnly = true)
    public List<ProgrammeYearResponseDto> getAllProgrammeYearsByProgrammeId(Long programmeId) {
        return programmeYearRepository.findProgrammeYearByProgrammeId(programmeId).stream()
                .map(this::toProgrammeYearResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchingCriteriaDto> getMatchingCriteriaByProgrammeYear(Long programmeYearId) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(programmeYearId)
                .orElseThrow(() -> new IllegalArgumentException("ProgrammeYear not found with ID: " + programmeYearId));

        return matchingCriteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .map(this::toMatchingCriteriaDto)
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

        programmeYear.setIsActive(updateDto.isActive());

        if (updateDto.getMatchApprovalType() != null) {
            programmeYear.setMatchApprovalType(updateDto.getMatchApprovalType());
            if (updateDto.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
                if (updateDto.getApprovalThreshold() == null || updateDto.getApprovalThreshold() < 0 || updateDto.getApprovalThreshold() > 100) {
                    throw new IllegalArgumentException("Approval threshold must be between 0 and 100 when using THRESHOLD mode.");
                }
                programmeYear.setApprovalThreshold(updateDto.getApprovalThreshold());
            } else {
                programmeYear.setApprovalThreshold(null);
            }
        }

        if (updateDto.getMatchingCriteria() != null) {
            saveMatchingCriteria(programmeYear, updateDto.getMatchingCriteria());
        }

        ProgrammeYear updatedProgrammeYear = programmeYearRepository.save(programmeYear);
        return toProgrammeYearResponseDto(updatedProgrammeYear);
    }

    private void saveMatchingCriteria(ProgrammeYear programmeYear, List<MatchingCriteriaDto> criteriaDtos) {
        int totalWeight = criteriaDtos.stream()
                .mapToInt(MatchingCriteriaDto::getWeight)
                .sum();

        if (totalWeight != 100) {
            throw new IllegalArgumentException("The total weight of all criteria must equal 100%");
        }

        matchingCriteriaRepository.deleteByProgrammeYearId(programmeYear.getId());

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
        dto.setPreferredAlgorithm(programmeYear.getPreferredAlgorithm());
        dto.setMatchApprovalType(programmeYear.getMatchApprovalType());
        dto.setApprovalThreshold(programmeYear.getApprovalThreshold());

        int participantCount = participantRepository.countByProgrammeYearId(programmeYear.getId());
        dto.setParticipantCount(participantCount);

        boolean hasMatches = matchRepository.existsByProgrammeYearId(programmeYear.getId());
        dto.setInitialMatchingIsDone(hasMatches);

        return dto;
    }

    private MatchingCriteriaDto toMatchingCriteriaDto(ProgrammeMatchingCriteria criterion) {
        MatchingCriteriaDto dto = new MatchingCriteriaDto();
        dto.setCriterionType(criterion.getCriterionType());
        dto.setWeight(criterion.getWeight());
        return dto;
    }

    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public void deleteProgrammeYear(Long id) {
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));
        programmeYearRepository.delete(programmeYear);
    }

}
