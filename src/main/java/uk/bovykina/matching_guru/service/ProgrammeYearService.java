package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;
import uk.bovykina.matching_guru.mapper.ProgrammeYearMapper;
import uk.bovykina.matching_guru.repository.*;
import uk.bovykina.matching_guru.util.CloudinaryService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProgrammeYearService {

    private final ProgrammeYearRepository programmeYearRepository;
    private final ProgrammeRepository programmeRepository;
    private final ProgrammeMatchingCriteriaRepository matchingCriteriaRepository;
    private final ParticipantRepository participantRepository;
    private final MatchRepository matchRepository;
    private final CloudinaryService cloudinaryService;
    private final ProgrammeYearMapper programmeYearMapper;

    /**
     * Creates a new programme year and saves matching criteria if provided.
     */
    public ProgrammeYearResponseDto createProgrammeYear(ProgrammeYearCreateDto dto) {
        log.info("Creating ProgrammeYear for Programme ID {}", dto.getProgrammeId());

        Programme programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        ProgrammeYear py = programmeYearMapper.toEntity(dto, programme, generateJoinCode());

        if (dto.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
            Integer threshold = dto.getApprovalThreshold();
            if (threshold == null || threshold < 0 || threshold > 100) {
                log.warn("Invalid approval threshold: {}", threshold);
                throw new IllegalArgumentException("Approval threshold must be between 0 and 100.");
            }
            py.setApprovalThreshold(threshold);
        }

        ProgrammeYear saved = programmeYearRepository.save(py);

        if (dto.getMatchingCriteria() != null) {
            saveMatchingCriteria(saved, dto.getMatchingCriteria());
        }

        log.info("ProgrammeYear created with ID {}", saved.getId());
        return programmeYearMapper.toResponseDto(saved,
                participantRepository.countByProgrammeYearId(saved.getId()),
                participantRepository.countMatchedInProgrammeYear(saved.getId()),
                matchRepository.existsByProgrammeYearId(saved.getId()),
                matchingCriteriaRepository.findByProgrammeYearId(saved.getId()));
    }

    /**
     * Retrieves a ProgrammeYear entity by ID or throws an exception.
     */
    public ProgrammeYear getById(Long id) {
        log.debug("Fetching ProgrammeYear by ID {}", id);
        return programmeYearRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ProgrammeYear not found"));
    }

    /**
     * Retrieves a programme year response DTO by ID.
     */
    public ProgrammeYearResponseDto getProgrammeYear(Long id) {
        log.info("Retrieving ProgrammeYear details for ID {}", id);
        ProgrammeYear py = getById(id);
        return programmeYearMapper.toResponseDto(py,
                participantRepository.countByProgrammeYearId(id),
                participantRepository.countMatchedInProgrammeYear(id),
                matchRepository.existsByProgrammeYearId(id),
                matchingCriteriaRepository.findByProgrammeYearId(id));
    }

    /**
     * Retrieves all programme years in the system.
     */
    public List<ProgrammeYearResponseDto> getAllProgrammeYears() {
        log.info("Fetching all ProgrammeYears");
        return programmeYearRepository.findAll().stream()
                .map(py -> programmeYearMapper.toResponseDto(py,
                        participantRepository.countByProgrammeYearId(py.getId()),
                        participantRepository.countMatchedInProgrammeYear(py.getId()),
                        matchRepository.existsByProgrammeYearId(py.getId()),
                        matchingCriteriaRepository.findByProgrammeYearId(py.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all programme years for a given programme.
     */
    public List<ProgrammeYearResponseDto> getAllProgrammeYearsByProgrammeId(Long programmeId) {
        log.info("Fetching ProgrammeYears for Programme ID {}", programmeId);
        return programmeYearRepository.findProgrammeYearByProgrammeId(programmeId).stream()
                .map(py -> programmeYearMapper.toResponseDto(py,
                        participantRepository.countByProgrammeYearId(py.getId()),
                        participantRepository.countMatchedInProgrammeYear(py.getId()),
                        matchRepository.existsByProgrammeYearId(py.getId()),
                        matchingCriteriaRepository.findByProgrammeYearId(py.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves matching criteria for a given programme year.
     */
    public List<MatchingCriteriaDto> getMatchingCriteriaByProgrammeYear(Long programmeYearId) {
        log.info("Fetching matching criteria for ProgrammeYear ID {}", programmeYearId);
        return matchingCriteriaRepository.findByProgrammeYearId(programmeYearId).stream()
                .map(programmeYearMapper::toMatchingCriteriaDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing programme year and replaces its matching criteria if provided.
     */
    public ProgrammeYearResponseDto updateProgrammeYear(Long id, ProgrammeYearUpdateDto updateDto) {
        log.info("Updating ProgrammeYear ID {}", id);
        ProgrammeYear py = getById(id);
        programmeYearMapper.updateEntityFromDto(py, updateDto);

        if (updateDto.getMatchApprovalType() == MatchApprovalType.THRESHOLD) {
            Integer threshold = updateDto.getApprovalThreshold();
            if (threshold == null || threshold < 0 || threshold > 100) {
                log.warn("Invalid threshold during update: {}", threshold);
                throw new IllegalArgumentException("Approval threshold must be between 0 and 100.");
            }
            py.setApprovalThreshold(threshold);
        } else {
            py.setApprovalThreshold(null);
        }

        ProgrammeYear updated = programmeYearRepository.save(py);

        if (updateDto.getMatchingCriteria() != null) {
            saveMatchingCriteria(updated, updateDto.getMatchingCriteria());
        }

        log.info("ProgrammeYear updated: ID {}", updated.getId());
        return programmeYearMapper.toResponseDto(updated,
                participantRepository.countByProgrammeYearId(updated.getId()),
                participantRepository.countMatchedInProgrammeYear(updated.getId()),
                matchRepository.existsByProgrammeYearId(updated.getId()),
                matchingCriteriaRepository.findByProgrammeYearId(updated.getId()));
    }

    /**
     * Deletes a programme year by its ID.
     */
    public void deleteProgrammeYear(Long id) {
        log.warn("Deleting ProgrammeYear with ID {}", id);
        ProgrammeYear py = getById(id);
        programmeYearRepository.delete(py);
        log.info("ProgrammeYear deleted: ID {}", id);
    }

    /**
     * Uploads a certificate template to Cloudinary.
     */
    public String uploadCertificateTemplate(MultipartFile file) throws IOException {
        log.info("Uploading certificate template");
        return cloudinaryService.uploadImage(file, "certificate_templates");
    }

    /**
     * Retrieves the most recently created programme year for a given programme.
     */
    public Optional<ProgrammeYearDto> findLatestByProgrammeId(Long programmeId) {
        log.info("Fetching latest ProgrammeYear for Programme ID {}", programmeId);
        return programmeYearRepository
                .findFirstByProgrammeIdOrderByCreatedAtDesc(programmeId)
                .map(programmeYearMapper::toDto);
    }

    /**
     * Saves matching criteria after validating total weight.
     */
    private void saveMatchingCriteria(ProgrammeYear py, List<MatchingCriteriaDto> dtos) {
        log.debug("Saving matching criteria for ProgrammeYear ID {}", py.getId());
        int totalWeight = dtos.stream().mapToInt(MatchingCriteriaDto::getWeight).sum();
        if (totalWeight != 100) {
            log.error("Invalid total weight: {}", totalWeight);
            throw new IllegalArgumentException("Total weight of all criteria must be 100%");
        }
        matchingCriteriaRepository.deleteByProgrammeYearId(py.getId());
        List<ProgrammeMatchingCriteria> entities = dtos.stream()
                .map(dto -> programmeYearMapper.toEntity(dto, py))
                .collect(Collectors.toList());
        matchingCriteriaRepository.saveAll(entities);
    }

    /**
     * Generates a random 8-character join code.
     */
    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
