package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.dto.course.CourseGroupCreateDto;
import uk.bovykina.matching_guru.dto.course.CourseGroupDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.enums.CourseType;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseGroupServiceTest {

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @InjectMocks
    private CourseGroupService courseGroupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCourseGroup() {
        // Arrange
        Long organisationId = 1L;
        Organisation organisation = new Organisation();
        organisation.setId(organisationId);

        CourseGroupCreateDto courseGroupCreateDto = new CourseGroupCreateDto();
        courseGroupCreateDto.setName("Test Group");
        courseGroupCreateDto.setOrganisationId(organisationId);

        CourseGroup savedGroup = new CourseGroup();
        savedGroup.setId(1L);
        savedGroup.setName("Test Group");
        savedGroup.setOrganisation(organisation);

        when(organisationRepository.findById(organisationId)).thenReturn(Optional.of(organisation));
        when(courseGroupRepository.save(any(CourseGroup.class))).thenReturn(savedGroup);

        // Act
        CourseGroupDto result = courseGroupService.createCourseGroup(courseGroupCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedGroup.getId(), result.getId());
        assertEquals(savedGroup.getName(), result.getName());
        assertEquals(savedGroup.getOrganisation().getId(), result.getOrganisationId());

        verify(organisationRepository).findById(organisationId);
        verify(courseGroupRepository).save(any(CourseGroup.class));
    }

    @Test
    void testGetCourseGroupsByOrganisationId() {
        // Arrange
        Long organisationId = 1L;

        Organisation organisation = new Organisation();
        organisation.setId(organisationId);

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setId(1L);
        courseGroup.setName("Test Group");
        courseGroup.setOrganisation(organisation);

        Course course = new Course();
        course.setId(1L);
        course.setName("Test Course");
        course.setType(CourseType.UNDERGRAD); // Use CourseType enum
        course.setDuration(3);
        course.setGroup(courseGroup);

        when(courseGroupRepository.findByOrganisationId(organisationId))
                .thenReturn(Collections.singletonList(courseGroup));
        when(courseRepository.findByGroupId(courseGroup.getId()))
                .thenReturn(Collections.singletonList(course));

        // Act
        List<CourseGroupDto> result = courseGroupService.getCourseGroupsByOrganisationId(organisationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(courseGroup.getId(), result.get(0).getId());
        assertEquals(1, result.get(0).getCourses().size());
        assertEquals(course.getId(), result.get(0).getCourses().get(0).getId());
        assertEquals(CourseType.UNDERGRAD, result.get(0).getCourses().get(0).getType());

        verify(courseGroupRepository).findByOrganisationId(organisationId);
        verify(courseRepository).findByGroupId(courseGroup.getId());
    }

    @Test
    void testDeleteCourseGroup() {
        // Arrange
        Long courseGroupId = 1L;

        // Act
        courseGroupService.deleteCourseGroup(courseGroupId);

        // Assert
        verify(courseGroupRepository).deleteById(courseGroupId);
    }

    @Test
    void testCreateCourseGroup_OrganisationNotFound() {
        // Arrange
        Long organisationId = 1L;
        CourseGroupCreateDto courseGroupCreateDto = new CourseGroupCreateDto();
        courseGroupCreateDto.setName("Test Group");
        courseGroupCreateDto.setOrganisationId(organisationId);

        when(organisationRepository.findById(organisationId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseGroupService.createCourseGroup(courseGroupCreateDto)
        );

        assertEquals("Organisation not found with id: " + organisationId, exception.getMessage());
        verify(organisationRepository).findById(organisationId);
        verifyNoInteractions(courseGroupRepository);
    }
}
