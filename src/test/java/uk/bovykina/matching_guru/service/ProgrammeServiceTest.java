package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import uk.bovykina.matching_guru.dto.programme.ProgrammeCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeUpdateDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.mapper.ProgrammeMapper;
import uk.bovykina.matching_guru.repository.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgrammeServiceTest {

    @InjectMocks
    private ProgrammeService programmeService;

    @Mock
    private ProgrammeRepository programmeRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProgrammeMapper programmeMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProgramme_shouldReturnDto() {
        ProgrammeCreateDto dto = new ProgrammeCreateDto();
        dto.setName("New Prog");
        dto.setDescription("Desc");
        dto.setOrganisationId(1L);
        dto.setCourseGroupIds(Set.of(100L));

        Organisation org = new Organisation();
        org.setId(1L);

        CourseGroup group = new CourseGroup();
        group.setId(100L);

        Programme saved = new Programme();
        saved.setId(10L);
        saved.setName("New Prog");
        saved.setDescription("Desc");
        saved.setOrganisation(org);
        saved.setEligibleCourseGroups(Set.of(group));

        ProgrammeDto expected = new ProgrammeDto();
        expected.setId(10L);
        expected.setName("New Prog");

        when(organisationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(courseGroupRepository.findAllById(dto.getCourseGroupIds())).thenReturn(List.of(group));
        when(programmeRepository.save(any())).thenReturn(saved);
        when(participantRepository.countDistinctParticipantsByProgrammeId(10L)).thenReturn(0);
        when(programmeMapper.toDto(saved, 0)).thenReturn(expected);

        ProgrammeDto result = programmeService.createProgramme(dto);

        assertEquals(10L, result.getId());
        verify(programmeMapper).toDto(saved, 0);
    }

    @Test
    void updateProgramme_shouldUpdateFieldsAndReturnDto() {
        ProgrammeUpdateDto dto = new ProgrammeUpdateDto();
        dto.setName("Updated Name");
        dto.setDescription("Updated Desc");
        dto.setContactEmail("new@example.com");
        dto.setCourseGroupIds(Set.of(10L));

        Programme existing = new Programme();
        existing.setId(1L);
        existing.setName("Old");
        existing.setDescription("Old");
        existing.setOrganisation(new Organisation());

        CourseGroup group = new CourseGroup();
        group.setId(10L);

        ProgrammeDto expected = new ProgrammeDto();
        expected.setId(1L);
        expected.setName("Updated Name");

        when(programmeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseGroupRepository.findAllById(Set.of(10L))).thenReturn(List.of(group));
        when(programmeRepository.save(any())).thenReturn(existing);
        when(participantRepository.countDistinctParticipantsByProgrammeId(1L)).thenReturn(0);
        when(programmeMapper.toDto(existing, 0)).thenReturn(expected);

        ProgrammeDto result = programmeService.updateProgramme(1L, dto);

        assertEquals("Updated Name", result.getName());
    }

    @Test
    void deleteProgramme_shouldDeleteIfExists() {
        when(programmeRepository.existsById(1L)).thenReturn(true);
        programmeService.deleteProgramme(1L);
        verify(programmeRepository).deleteById(1L);
    }

    @Test
    void getProgrammeById_shouldReturnDto() {
        Programme programme = new Programme();
        programme.setId(5L);
        programme.setName("My Programme");

        ProgrammeDto dto = new ProgrammeDto();
        dto.setId(5L);
        dto.setName("My Programme");

        when(programmeRepository.findById(5L)).thenReturn(Optional.of(programme));
        when(participantRepository.countDistinctParticipantsByProgrammeId(5L)).thenReturn(2);
        when(programmeMapper.toDto(programme, 2)).thenReturn(dto);

        ProgrammeDto result = programmeService.getProgrammeById(5L);

        assertEquals(5L, result.getId());
        verify(programmeMapper).toDto(programme, 2);
    }

    @Test
    void getAllProgrammes_shouldReturnList() {
        Programme p1 = new Programme();
        p1.setId(1L);
        Programme p2 = new Programme();
        p2.setId(2L);

        ProgrammeDto d1 = new ProgrammeDto();
        d1.setId(1L);
        ProgrammeDto d2 = new ProgrammeDto();
        d2.setId(2L);

        when(programmeRepository.findAll()).thenReturn(List.of(p1, p2));
        when(participantRepository.countDistinctParticipantsByProgrammeId(1L)).thenReturn(1);
        when(participantRepository.countDistinctParticipantsByProgrammeId(2L)).thenReturn(2);
        when(programmeMapper.toDto(p1, 1)).thenReturn(d1);
        when(programmeMapper.toDto(p2, 2)).thenReturn(d2);

        var result = programmeService.getAllProgrammes();

        assertEquals(2, result.size());
    }

    @Test
    void getProgrammesByOrganisation_shouldReturnList() {
        Organisation org = new Organisation();
        org.setId(1L);

        Programme prog = new Programme();
        prog.setId(5L);
        prog.setOrganisation(org);

        ProgrammeDto dto = new ProgrammeDto();
        dto.setId(5L);

        when(programmeRepository.findByOrganisationId(1L)).thenReturn(List.of(prog));
        when(participantRepository.countDistinctParticipantsByProgrammeId(5L)).thenReturn(4);
        when(programmeMapper.toDto(prog, 4)).thenReturn(dto);

        var result = programmeService.getProgrammesByOrganisation(1L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }

    @Test
    void getActiveProgrammesByOrganisation_shouldReturnList() {
        Programme p = new Programme();
        p.setId(10L);

        ProgrammeDto d = new ProgrammeDto();
        d.setId(10L);

        when(programmeRepository.findActiveProgrammesByOrganisationId(1L)).thenReturn(List.of(p));
        when(participantRepository.countDistinctParticipantsByProgrammeId(10L)).thenReturn(1);
        when(programmeMapper.toDto(p, 1)).thenReturn(d);

        var result = programmeService.getActiveProgrammesByOrganisation(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getProgrammesByUserId_shouldReturnList() {
        User user = new User();
        user.setId(2L);

        Programme prog = new Programme();
        prog.setId(5L);

        ProgrammeDto dto = new ProgrammeDto();
        dto.setId(5L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(programmeRepository.findProgrammesByUserId(2L)).thenReturn(List.of(prog));
        when(participantRepository.countDistinctParticipantsByProgrammeId(5L)).thenReturn(3);
        when(programmeMapper.toDto(prog, 3)).thenReturn(dto);

        var result = programmeService.getProgrammesByUserId(2L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }
}
