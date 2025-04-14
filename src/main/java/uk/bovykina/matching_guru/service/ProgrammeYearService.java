package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeMatchingCriteria;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;
import uk.bovykina.matching_guru.repository.*;
import uk.bovykina.matching_guru.util.CloudinaryService;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgrammeYearService {

    private final ProgrammeYearRepository programmeYearRepository;
    private final ProgrammeRepository programmeRepository;
    private final ProgrammeMatchingCriteriaRepository matchingCriteriaRepository;
    private final ParticipantRepository participantRepository;
    private final MatchRepository matchRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public ProgrammeYearResponseDto createProgrammeYear(ProgrammeYearCreateDto createDto) {
        log.info("📥 Creating ProgrammeYear for academicYear={}, programmeId={}, algorithm={}, approvalType={}, threshold={}",
                createDto.getAcademicYear(),
                createDto.getProgrammeId(),
                createDto.getPreferredAlgorithm(),
                createDto.getMatchApprovalType(),
                createDto.getApprovalThreshold()
        );

        Programme programme = programmeRepository.findById(createDto.getProgrammeId())
                .orElseThrow(() -> {
                    log.error("❌ Programme not found with ID: {}", createDto.getProgrammeId());
                    return new IllegalArgumentException("Programme not found");
                });

        ProgrammeYear programmeYear = new ProgrammeYear();
        programmeYear.setProgramme(programme);
        programmeYear.setAcademicYear(createDto.getAcademicYear());
        programmeYear.setIsActive(true);
        programmeYear.setJoinCode(generateJoinCode());
        programmeYear.setPreferredAlgorithm(createDto.getPreferredAlgorithm());
        programmeYear.generateFeedbackConfirmationCode();
        programmeYear.setStrictAcademicStage(createDto.getStrictAcademicStage());
        programmeYear.setStrictCourseGroup(createDto.getStrictCourseGroup());
        programmeYear.setSurveyOpenDate(createDto.getSurveyOpenDate());
        programmeYear.setSurveyCloseDate(createDto.getSurveyCloseDate());
        programmeYear.setFeedbackConfirmationCode(programmeYear.generateFeedbackConfirmationCode());
        programmeYear.setMatchApprovalType(createDto.getMatchApprovalType());
        programmeYear.setSurveyUrl(createDto.getSurveyUrl());
        programmeYear.setStartDate(createDto.getStartDate());
        programmeYear.setEndDate(createDto.getEndDate());
        programmeYear.setSignupOpenDate(createDto.getSignupOpenDate());
        programmeYear.setSignupCloseDate(createDto.getSignupCloseDate());
        programmeYear.setCertificateTemplateUrl(createDto.getCertificateTemplateUrl());

        if (createDto.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
            if (createDto.getApprovalThreshold() == null || createDto.getApprovalThreshold() < 0 || createDto.getApprovalThreshold() > 100) {
                log.error("❌ Invalid threshold value: {}", createDto.getApprovalThreshold());
                throw new IllegalArgumentException("Approval threshold must be between 0 and 100 when using THRESHOLD mode.");
            }
            programmeYear.setApprovalThreshold(createDto.getApprovalThreshold());
            log.info("✅ Set approvalThreshold = {}", createDto.getApprovalThreshold());
        } else {
            programmeYear.setApprovalThreshold(null);
            log.info("➖ Approval type is not THRESHOLD; threshold cleared.");
        }

        ProgrammeYear savedProgrammeYear = programmeYearRepository.save(programmeYear);
        log.info("💾 ProgrammeYear saved with ID = {}", savedProgrammeYear.getId());

        if (createDto.getMatchingCriteria() != null) {
            log.info("📊 Saving {} matching criteria...", createDto.getMatchingCriteria().size());
            saveMatchingCriteria(savedProgrammeYear, createDto.getMatchingCriteria());
        }

        return toProgrammeYearResponseDto(savedProgrammeYear);
    }

    public ProgrammeYear getById(Long id) {
        log.debug("🔍 Fetching ProgrammeYear by ID: {}", id);
        return programmeYearRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ ProgrammeYear not found with ID: {}", id);
                    return new RuntimeException("ProgrammeYear not found");
                });
    }

    public ProgrammeYearResponseDto getProgrammeYear(Long id) {
        log.info("📄 Getting full ProgrammeYearResponseDto for ID: {}", id);
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Programme year not found: {}", id);
                    return new IllegalArgumentException("Programme year not found");
                });

        ProgrammeYearResponseDto responseDto = toProgrammeYearResponseDto(programmeYear);
        responseDto.setMatchingCriteria(
                matchingCriteriaRepository.findByProgrammeYearId(programmeYear.getId()).stream()
                        .map(this::toMatchingCriteriaDto)
                        .collect(Collectors.toList())
        );

        return responseDto;
    }

    public List<ProgrammeYearResponseDto> getAllProgrammeYears() {
        log.info("📚 Fetching all programme years");
        return programmeYearRepository.findAll().stream()
                .map(this::toProgrammeYearResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgrammeYearResponseDto> getAllProgrammeYearsByProgrammeId(Long programmeId) {
        log.info("📚 Fetching all programme years for programme ID: {}", programmeId);
        return programmeYearRepository.findProgrammeYearByProgrammeId(programmeId).stream()
                .map(this::toProgrammeYearResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchingCriteriaDto> getMatchingCriteriaByProgrammeYear(Long programmeYearId) {
        log.info("📊 Fetching matching criteria for ProgrammeYear ID: {}", programmeYearId);
        ProgrammeYear programmeYear = programmeYearRepository.findById(programmeYearId)
                .orElseThrow(() -> {
                    log.error("❌ ProgrammeYear not found with ID: {}", programmeYearId);
                    return new IllegalArgumentException("ProgrammeYear not found with ID: " + programmeYearId);
                });

        return matchingCriteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .map(this::toMatchingCriteriaDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgrammeYearResponseDto updateProgrammeYear(Long id, ProgrammeYearUpdateDto updateDto) {
        log.info("Update DTO: {}", updateDto);
        log.info("✏️ Updating ProgrammeYear ID: {}", id);
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Programme year not found: {}", id);
                    return new IllegalArgumentException("Programme year not found");
                });

        if (updateDto.getAcademicYear() != null) {
            programmeYear.setAcademicYear(updateDto.getAcademicYear());
            log.info("🔄 Updated academicYear to {}", updateDto.getAcademicYear());
        }
        if (updateDto.getPreferredAlgorithm() != null) {
            programmeYear.setPreferredAlgorithm(updateDto.getPreferredAlgorithm());
            log.info("🔄 Updated preferredAlgorithm to {}", updateDto.getPreferredAlgorithm());
        }

        if (updateDto.getStrictAcademicStage() != null) {
            programmeYear.setStrictAcademicStage(updateDto.getStrictAcademicStage());
            log.info("🔄 Updated strictAcademicStage to {}", updateDto.getStrictAcademicStage());
        }

        if (updateDto.getStrictCourseGroup() != null) {
            programmeYear.setStrictCourseGroup(updateDto.getStrictCourseGroup());
            log.info("🔄 Updated strictCourseGroup to {}", updateDto.getStrictCourseGroup());
        }

        if (updateDto.getSurveyOpenDate() != null) {
            programmeYear.setSurveyOpenDate(updateDto.getSurveyOpenDate());
            log.info("📅 Updated surveyOpenDate to {}", updateDto.getSurveyOpenDate());
        }
        if (updateDto.getSurveyCloseDate() != null) {
            programmeYear.setSurveyCloseDate(updateDto.getSurveyCloseDate());
            log.info("📅 Updated surveyCloseDate to {}", updateDto.getSurveyCloseDate());
        }

        if (updateDto.getSurveyUrl() != null) {
            programmeYear.setSurveyUrl(updateDto.getSurveyUrl());
            log.info("📅 Updated surveyUrl to {}", updateDto.getSurveyUrl());
        }

        if (updateDto.getIsActive() != null) {
            programmeYear.setIsActive(updateDto.getIsActive());
            log.info("🔄 Updated isActive to {}", updateDto.getIsActive());
        }

        if (updateDto.getStartDate() != null) {
            programmeYear.setStartDate(updateDto.getStartDate());
            log.info("📅 Updated startDate to {}", updateDto.getStartDate());
        }
        if (updateDto.getEndDate() != null) {
            programmeYear.setEndDate(updateDto.getEndDate());
            log.info("📅 Updated endDate to {}", updateDto.getEndDate());
        }
        if (updateDto.getSignupOpenDate() != null) {
            programmeYear.setSignupOpenDate(updateDto.getSignupOpenDate());
            log.info("📅 Updated signupOpenDate to {}", updateDto.getSignupOpenDate());
        }
        if (updateDto.getSignupCloseDate() != null) {
            programmeYear.setSignupCloseDate(updateDto.getSignupCloseDate());
            log.info("📅 Updated signupCloseDate to {}", updateDto.getSignupCloseDate());
        }

        if (updateDto.getMatchApprovalType() != null) {
            programmeYear.setMatchApprovalType(updateDto.getMatchApprovalType());
            log.info("🔄 Updated matchApprovalType to {}", updateDto.getMatchApprovalType());

            if (updateDto.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
                if (updateDto.getApprovalThreshold() == null || updateDto.getApprovalThreshold() < 0 || updateDto.getApprovalThreshold() > 100) {
                    log.error("❌ Invalid approvalThreshold: {}", updateDto.getApprovalThreshold());
                    throw new IllegalArgumentException("Approval threshold must be between 0 and 100 when using THRESHOLD mode.");
                }
                programmeYear.setApprovalThreshold(updateDto.getApprovalThreshold());
                log.info("✅ Set approvalThreshold to {}", updateDto.getApprovalThreshold());
            } else {
                programmeYear.setApprovalThreshold(null);
                log.info("➖ Threshold cleared since approvalType is not THRESHOLD");
            }
        }

        if (updateDto.getMatchingCriteria() != null) {
            log.info("📊 Updating {} matching criteria", updateDto.getMatchingCriteria().size());
            saveMatchingCriteria(programmeYear, updateDto.getMatchingCriteria());
        }

        if (updateDto.getCertificateTemplateUrl() != null) {
            log.info("📊 Updating certificateTemplateUrl to {}", updateDto.getCertificateTemplateUrl());
            programmeYear.setCertificateTemplateUrl(updateDto.getCertificateTemplateUrl());
        }

        ProgrammeYear updatedProgrammeYear = programmeYearRepository.save(programmeYear);
        log.info("💾 ProgrammeYear updated with ID = {}", updatedProgrammeYear.getId());

        return toProgrammeYearResponseDto(updatedProgrammeYear);
    }

    @Transactional
    public void deleteProgrammeYear(Long id) {
        log.warn("🗑️ Deleting ProgrammeYear ID: {}", id);
        ProgrammeYear programmeYear = programmeYearRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Cannot delete. Programme year not found: {}", id);
                    return new IllegalArgumentException("Programme year not found");
                });
        programmeYearRepository.delete(programmeYear);
        log.info("✅ ProgrammeYear deleted successfully");
    }

    public String uploadCertificateTemplate(MultipartFile file) throws IOException {
        return cloudinaryService.uploadImage(file, "certificate_templates");
    }

    public Optional<ProgrammeYearDto> findLatestByProgrammeId(Long programmeId) {
        return programmeYearRepository
                .findFirstByProgrammeIdOrderByCreatedAtDesc(programmeId)
                .map(this::toProgrammeYearDto);
    }


    private void saveMatchingCriteria(ProgrammeYear programmeYear, List<MatchingCriteriaDto> criteriaDtos) {
        int totalWeight = criteriaDtos.stream()
                .mapToInt(MatchingCriteriaDto::getWeight)
                .sum();

        if (totalWeight != 100) {
            log.error("❌ Total weight is {}%, must be 100%", totalWeight);
            throw new IllegalArgumentException("The total weight of all criteria must equal 100%");
        }

        matchingCriteriaRepository.deleteByProgrammeYearId(programmeYear.getId());
        log.info("🧹 Deleted existing matching criteria for ProgrammeYear ID: {}", programmeYear.getId());

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
        log.info("✅ Saved {} new matching criteria", criteria.size());
    }

    private ProgrammeYearResponseDto toProgrammeYearResponseDto(ProgrammeYear programmeYear) {
        ProgrammeYearResponseDto dto = new ProgrammeYearResponseDto();
        dto.setId(programmeYear.getId());
        dto.setProgrammeId(programmeYear.getProgramme().getId());
        dto.setProgrammeName(programmeYear.getProgramme().getName());
        dto.setProgrammeDescription(programmeYear.getProgramme().getDescription());
        dto.setContactEmail(programmeYear.getProgramme().getContactEmail());
        dto.setAcademicYear(programmeYear.getAcademicYear());
        dto.setIsActive(programmeYear.getIsActive());
        dto.setJoinCode(programmeYear.getJoinCode());
        dto.setPreferredAlgorithm(programmeYear.getPreferredAlgorithm());
        dto.setMatchApprovalType(programmeYear.getMatchApprovalType());
        dto.setApprovalThreshold(programmeYear.getApprovalThreshold());
        dto.setStrictAcademicStage(programmeYear.getStrictAcademicStage());
        dto.setStrictCourseGroup(programmeYear.getStrictCourseGroup());
        dto.setSurveyOpenDate(programmeYear.getSurveyOpenDate());
        dto.setSurveyCloseDate(programmeYear.getSurveyCloseDate());
        dto.setFeedbackConfirmationCode(programmeYear.getFeedbackConfirmationCode());
        dto.setSurveyUrl(programmeYear.getSurveyUrl());
        dto.setStartDate(programmeYear.getStartDate());
        dto.setEndDate(programmeYear.getEndDate());
        dto.setSignupOpenDate(programmeYear.getSignupOpenDate());
        dto.setSignupCloseDate(programmeYear.getSignupCloseDate());
        dto.setSurveyOpen(programmeYear.isSurveyOpen());
        dto.setSignupOpen(programmeYear.isSignupOpen());
        dto.setCurrentlyRunning(programmeYear.isCurrentlyRunning());
        dto.setCertificateTemplateUrl(programmeYear.getCertificateTemplateUrl());

        int participantCount = participantRepository.countByProgrammeYearId(programmeYear.getId());
        dto.setParticipantCount(participantCount);

        boolean hasMatches = matchRepository.existsByProgrammeYearId(programmeYear.getId());
        dto.setInitialMatchingIsDone(hasMatches);

        int unmatchedCount = participantRepository.countByProgrammeYearIdAndIsMatchedFalse(programmeYear.getId());
        dto.setUnmatchedCount(unmatchedCount);

        return dto;
    }

    private ProgrammeYearDto toProgrammeYearDto(ProgrammeYear programmeYear) {
        ProgrammeYearDto dto = new ProgrammeYearDto();

        dto.setId(programmeYear.getId());
        dto.setAcademicYear(programmeYear.getAcademicYear());
        dto.setIsActive(programmeYear.getIsActive());
        dto.setStartDate(programmeYear.getStartDate());
        dto.setEndDate(programmeYear.getEndDate());
        dto.setSignupOpenDate(programmeYear.getSignupOpenDate());
        dto.setSignupCloseDate(programmeYear.getSignupCloseDate());
        dto.setPreferredAlgorithm(programmeYear.getPreferredAlgorithm());
        dto.setJoinCode(programmeYear.getJoinCode());
        dto.setSurveyUrl(programmeYear.getSurveyUrl());
        dto.setStrictAcademicStage(programmeYear.getStrictAcademicStage());
        dto.setStrictCourseGroup(programmeYear.getStrictCourseGroup());
        dto.setCertificateTemplateUrl(programmeYear.getCertificateTemplateUrl());
        dto.setMatchApprovalType(programmeYear.getMatchApprovalType());
        dto.setApprovalThreshold(programmeYear.getApprovalThreshold());

        if (programmeYear.getProgramme() != null) {
            dto.setProgrammeId(programmeYear.getProgramme().getId());
            dto.setProgrammeName(programmeYear.getProgramme().getName());
        }

        List<MatchingCriteriaDto> matchingCriteria = programmeYear.getMatchingCriteria().stream()
                .map(this::toMatchingCriteriaDto)
                .collect(Collectors.toList());

        dto.setMatchingCriteria(matchingCriteria);

        return dto;
    }

    private MatchingCriteriaDto toMatchingCriteriaDto(ProgrammeMatchingCriteria criterion) {
        MatchingCriteriaDto dto = new MatchingCriteriaDto();
        dto.setCriterionType(criterion.getCriterionType());
        dto.setWeight(criterion.getWeight());
        return dto;
    }

    private String generateJoinCode() {
        String code = UUID.randomUUID().toString().substring(0, 8);
        log.debug("🔐 Generated join code: {}", code);
        return code;
    }
}
