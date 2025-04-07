package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.bovykina.matching_guru.dto.programme.MatchingCriteriaDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeYearResponseDto;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.AlgorithmType;
import uk.bovykina.matching_guru.entity.enums.CriterionType;
import uk.bovykina.matching_guru.entity.enums.MatchApprovalType;
import uk.bovykina.matching_guru.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgrammeYearServiceTest {

    @Mock
    private ProgrammeYearRepository programmeYearRepository;

    @Mock
    private ProgrammeRepository programmeRepository;

    @Mock
    private ProgrammeMatchingCriteriaRepository matchingCriteriaRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private ProgrammeYearService programmeYearService;

    private Programme programme;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        programme = new Programme();
        programme.setId(1L);
        programme.setName("Test Programme");
        programme.setDescription("Test Description");
        programme.setContactEmail("test@example.com");
    }

    @Test
    void createProgrammeYear_shouldReturnDto() {
        ProgrammeYearCreateDto createDto = new ProgrammeYearCreateDto();
        createDto.setProgrammeId(1L);
        createDto.setAcademicYear("2024/25");
        createDto.setPreferredAlgorithm(AlgorithmType.BRACE);
        createDto.setMatchApprovalType(MatchApprovalType.MANUAL);
        createDto.setStrictAcademicStage(false);
        createDto.setStrictCourseGroup(false);

        MatchingCriteriaDto c1 = new MatchingCriteriaDto();
        c1.setCriterionType(CriterionType.FIELD);
        c1.setWeight(60);

        MatchingCriteriaDto c2 = new MatchingCriteriaDto();
        c2.setCriterionType(CriterionType.SKILLS);
        c2.setWeight(40);

        createDto.setMatchingCriteria(List.of(c1, c2));

        ProgrammeYear saved = new ProgrammeYear();
        saved.setId(10L);
        saved.setProgramme(programme);
        saved.setAcademicYear("2024/25");

        when(programmeRepository.findById(1L)).thenReturn(Optional.of(programme));
        when(programmeYearRepository.save(any())).thenReturn(saved);

        ProgrammeYearResponseDto result = programmeYearService.createProgrammeYear(createDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("2024/25", result.getAcademicYear());
    }

    @Test
    void createProgrammeYear_shouldThrowIfProgrammeNotFound() {
        ProgrammeYearCreateDto createDto = new ProgrammeYearCreateDto();
        createDto.setProgrammeId(99L);

        when(programmeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> programmeYearService.createProgrammeYear(createDto));
    }

    @Test
    void getById_shouldReturnEntity() {
        ProgrammeYear programmeYear = new ProgrammeYear();
        programmeYear.setId(10L);

        when(programmeYearRepository.findById(10L)).thenReturn(Optional.of(programmeYear));

        ProgrammeYear result = programmeYearService.getById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        when(programmeYearRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> programmeYearService.getById(99L));
    }
}