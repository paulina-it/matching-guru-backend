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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        programmeService = new ProgrammeService(
                programmeRepository,
                organisationRepository,
                courseGroupRepository,
                participantRepository,
                userRepository
        );
    }

    @Test
    void testGetAllProgrammes() {
        // Arrange
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

        // Act
        var result = programmeService.getAllProgrammes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(programmeRepository).findAll();
    }

    @Test
    void testGetProgrammeById() {
        // Arrange
        Organisation organisation = new Organisation();
        organisation.setId(1L);

        Programme programme = new Programme();
        programme.setId(1L);
        programme.setName("Test Programme");
        programme.setOrganisation(organisation);

        when(programmeRepository.findById(1L)).thenReturn(Optional.of(programme));

        // Act
        var result = programmeService.getProgrammeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Programme", result.getName());
        verify(programmeRepository).findById(1L);
    }

    @Test
    void testGetProgrammesByOrganisation() {
        // Arrange
        Organisation organisation = new Organisation();
        organisation.setId(1L);

        Programme programme1 = new Programme();
        programme1.setId(1L);
        programme1.setName("Programme 1");
        programme1.setOrganisation(organisation);

        when(programmeRepository.findByOrganisationId(1L)).thenReturn(List.of(programme1));

        // Act
        var result = programmeService.getProgrammesByOrganisation(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Programme 1", result.get(0).getName());
        verify(programmeRepository).findByOrganisationId(1L);
    }

    @Test
    void testUpdateProgramme() {
        // Arrange
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

        // Act
        var result = programmeService.updateProgramme(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Programme", result.getName());
        assertEquals("Updated Description", result.getDescription());
        verify(programmeRepository).save(existingProgramme);
    }
}
