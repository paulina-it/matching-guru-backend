package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.bovykina.matching_guru.dto.programme.ProgrammeCreateDto;
import uk.bovykina.matching_guru.dto.programme.ProgrammeUpdateDto;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.mapper.ProgrammeMapper;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;
import uk.bovykina.matching_guru.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgrammeServiceTest {

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
        programmeService = new ProgrammeService(
                programmeRepository,
                organisationRepository,
                courseGroupRepository,
                participantRepository,
                userRepository,
                programmeMapper
        );
    }

    @Test
    void testGetAllProgrammes() {
        Organisation organisation = new Organisation();
        organisation.setId(1L);

        Programme programme1 = new Programme();
        programme1.setId(1L);
        programme1.setName("Programme 1");
        programme1.setOrganisation(organisation);

        Programme programme2 = new Programme();
        programme2.setId(2L);
        programme2.setName("Programme 2");
        programme2.setOrganisation(organisation);

        when(programmeRepository.findAll()).thenReturn(List.of(programme1, programme2));

        var result = programmeService.getAllProgrammes();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(programmeRepository).findAll();
    }

    @Test
    void testGetProgrammeById() {
        Organisation organisation = new Organisation();
        organisation.setId(1L);

        Programme programme = new Programme();
        programme.setId(1L);
        programme.setName("Test Programme");
        programme.setOrganisation(organisation);

        when(programmeRepository.findById(1L)).thenReturn(Optional.of(programme));

        var result = programmeService.getProgrammeById(1L);

        assertNotNull(result);
        assertEquals("Test Programme", result.getName());
        verify(programmeRepository).findById(1L);
    }

    @Test
    void testGetProgrammesByOrganisation() {
        Organisation organisation = new Organisation();
        organisation.setId(1L);

        Programme programme1 = new Programme();
        programme1.setId(1L);
        programme1.setName("Programme 1");
        programme1.setOrganisation(organisation);

        when(programmeRepository.findByOrganisationId(1L)).thenReturn(List.of(programme1));

        var result = programmeService.getProgrammesByOrganisation(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Programme 1", result.get(0).getName());
        verify(programmeRepository).findByOrganisationId(1L);
    }

    @Test
    void testUpdateProgramme() {
        Organisation organisation = new Organisation();
        organisation.setId(1L);

        Programme existingProgramme = new Programme();
        existingProgramme.setId(1L);
        existingProgramme.setName("Old Programme");
        existingProgramme.setDescription("Old Description");
        existingProgramme.setOrganisation(organisation);

        ProgrammeUpdateDto updateDto = new ProgrammeUpdateDto();
        updateDto.setName("Updated Programme");
        updateDto.setDescription("Updated Description");

        when(programmeRepository.findById(1L)).thenReturn(Optional.of(existingProgramme));
        when(programmeRepository.save(existingProgramme)).thenReturn(existingProgramme);

        var result = programmeService.updateProgramme(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Programme", result.getName());
        assertEquals("Updated Description", result.getDescription());
        verify(programmeRepository).save(existingProgramme);
    }

    @Test
    void testCreateProgramme_shouldReturnSavedDto() {
        ProgrammeCreateDto createDto = new ProgrammeCreateDto();
        createDto.setName("New Prog");
        createDto.setDescription("New Desc");
        createDto.setOrganisationId(1L);
        createDto.setCourseGroupIds(Set.of(10L));

        Organisation organisation = new Organisation();
        organisation.setName("Test Org");
        organisation.setId(1L);

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setName("Test Group");
        courseGroup.setId(10L);

        Programme saved = new Programme();
        saved.setId(1L);
        saved.setName("New Prog");
        saved.setDescription("New Desc");
        saved.setOrganisation(organisation);
        saved.setEligibleCourseGroups(Set.of(courseGroup));

        when(organisationRepository.findById(1L)).thenReturn(Optional.of(organisation));
        when(courseGroupRepository.findAllById(Set.of(10L))).thenReturn(List.of(courseGroup));
        when(programmeRepository.save(any())).thenReturn(saved);
        when(participantRepository.countDistinctParticipantsByProgrammeId(anyLong())).thenReturn(0);

        var result = programmeService.createProgramme(createDto);

        assertNotNull(result);
        assertEquals("New Prog", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void testDeleteProgramme_shouldDeleteIfExists() {
        when(programmeRepository.existsById(1L)).thenReturn(true);

        programmeService.deleteProgramme(1L);

        verify(programmeRepository).deleteById(1L);
    }

    @Test
    void testDeleteProgramme_shouldThrowIfNotFound() {
        when(programmeRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> programmeService.deleteProgramme(99L));
    }

    @Test
    void testGetActiveProgrammesByOrganisation_shouldReturnList() {
        Organisation organisation = new Organisation();
        organisation.setId(1L);

        Programme programme = new Programme();
        programme.setId(1L);
        programme.setName("Active Prog");
        programme.setDescription("Active Desc");
        programme.setOrganisation(organisation);
        programme.setEligibleCourseGroups(Set.of());

        when(programmeRepository.findActiveProgrammesByOrganisationId(1L)).thenReturn(List.of(programme));
        when(participantRepository.countDistinctParticipantsByProgrammeId(1L)).thenReturn(5);

        var result = programmeService.getActiveProgrammesByOrganisation(1L);

        assertEquals(1, result.size());
        assertEquals("Active Prog", result.get(0).getName());
    }

    @Test
    void testGetProgrammesByUserId_shouldReturnList() {
        var user = new uk.bovykina.matching_guru.entity.User();
        user.setId(1L);

        Programme programme = new Programme();
        programme.setId(1L);
        programme.setName("User's Programme");
        programme.setOrganisation(new Organisation());
        programme.setEligibleCourseGroups(Set.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(programmeRepository.findProgrammesByUserId(1L)).thenReturn(List.of(programme));
        when(participantRepository.countDistinctParticipantsByProgrammeId(1L)).thenReturn(3);

        var result = programmeService.getProgrammesByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("User's Programme", result.get(0).getName());
    }
}
