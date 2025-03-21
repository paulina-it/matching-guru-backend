package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.course.CourseCreateDto;
import uk.bovykina.matching_guru.dto.course.CourseDto;
import uk.bovykina.matching_guru.dto.course.CourseUpdateDto;
import uk.bovykina.matching_guru.entity.Course;
import uk.bovykina.matching_guru.entity.CourseGroup;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.enums.CourseType;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final ProgrammeRepository programmeRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final OrganisationRepository organisationRepository;

    /**
     * Create a new course.
     */
    public CourseDto createCourse(CourseCreateDto courseCreateDto) {
        log.info("🔄 Creating a new course: {}", courseCreateDto.getName());
        CourseGroup group = courseGroupRepository.findById(courseCreateDto.getGroupId())
                .orElseThrow(() -> {
                    log.error("❌ CourseGroup not found: {}", courseCreateDto.getGroupId());
                    return new IllegalArgumentException("CourseGroup not found with ID: " + courseCreateDto.getGroupId());
                });

        Course course = new Course();
        course.setName(courseCreateDto.getName());
        course.setType(courseCreateDto.getType());
        course.setDuration(courseCreateDto.getDuration());
        course.setGroup(group);

        Course savedCourse = courseRepository.save(course);
        log.info("✅ Course created successfully: {}", savedCourse.getId());
        return toCourseDto(savedCourse);
    }

    /**
     * Retrieve a course by ID.
     */
    public CourseDto getCourseById(Long courseId) {
        log.info("🔄 Fetching course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("❌ Course not found: {}", courseId);
                    return new IllegalArgumentException("Course not found with ID: " + courseId);
                });

        log.info("✅ Course found: {}", course.getName());
        return toCourseDto(course);
    }

    /**
     * Get courses by group ID.
     */
    public List<CourseDto> getCoursesByGroupId(Long groupId) {
        log.info("🔄 Fetching courses for group ID: {}", groupId);
        return courseRepository.findByGroupId(groupId).stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get eligible courses for a programme.
     */
    public List<CourseDto> getEligibleCoursesByProgrammeId(Long programmeId) {
        log.info("🔄 Fetching eligible courses for programme ID: {}", programmeId);
        Programme programme = programmeRepository.findById(programmeId)
                .orElseThrow(() -> {
                    log.error("❌ Programme not found: {}", programmeId);
                    return new IllegalArgumentException("Programme not found with ID: " + programmeId);
                });

        List<Long> courseGroupIds = programme.getEligibleCourseGroups().stream()
                .map(CourseGroup::getId)
                .collect(Collectors.toList());

        return courseRepository.findByGroupIds(courseGroupIds).stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing course.
     */
    public CourseDto updateCourse(Long courseId, CourseUpdateDto courseUpdateDto) {
        log.info("🔄 Updating course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("❌ Course not found: {}", courseId);
                    return new IllegalArgumentException("Course not found with ID: " + courseId);
                });

        course.setName(courseUpdateDto.getName());
        course.setType(courseUpdateDto.getType());
        course.setDuration(courseUpdateDto.getDuration());

        Course updatedCourse = courseRepository.save(course);
        log.info("✅ Course updated successfully: {}", updatedCourse.getId());
        return toCourseDto(updatedCourse);
    }

    /**
     * Delete a course by ID.
     */
    public void deleteCourse(Long courseId) {
        log.info("🔄 Deleting course ID: {}", courseId);
        courseRepository.deleteById(courseId);
        log.info("✅ Course deleted: {}", courseId);
    }

    private CourseDto toCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setType(course.getType());
        dto.setDuration(course.getDuration());
        dto.setGroupId(course.getGroup().getId());
        return dto;
    }

    public void processFile(MultipartFile file, Long organisationId) throws IOException {
        try {
            if (file.getOriginalFilename().endsWith(".csv")) {
                processCSV(file, organisationId);
            } else if (file.getOriginalFilename().endsWith(".xlsx")) {
                processXLSX(file, organisationId);
            } else {
                throw new IllegalArgumentException("Unsupported file type");
            }
        } catch (IOException e) {
            log.error("Error processing file: {}", e.getMessage());
            throw new IOException("Error processing file", e);
        }
    }

    private void processCSV(MultipartFile file, Long organisationId) throws IOException {
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                String groupName = record.get("CourseGroup");
                String type = record.get("Type");
                String courseName = record.get("CourseName");

                saveCourse(groupName, type, courseName, organisationId);
            }
        }
    }


    // Process XLSX files
    private void processXLSX(MultipartFile file, Long organisationId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String groupName = row.getCell(0).getStringCellValue();
                String type = row.getCell(1).getStringCellValue();
                String courseName = row.getCell(2).getStringCellValue();

                saveCourse(groupName, type, courseName, organisationId);
            }
        } catch (IOException e) {
            log.error("Error reading XLSX file: {}", e.getMessage());
            throw e;
        }
    }

    private void saveCourse(String groupName, String type, String courseName, Long organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + organisationId));

        List<CourseGroup> groups = courseGroupRepository.findByNameAndOrganisationId(groupName, organisationId);
        CourseGroup group;

        if (groups.isEmpty()) {
            group = new CourseGroup();
            group.setName(groupName);
            group.setOrganisation(organisation);
            group = courseGroupRepository.save(group);
        } else {
            group = groups.get(0);
        }

        Course course = new Course();
        course.setName(courseName);
        course.setGroup(group);
        course.setType(CourseType.valueOf(type));
        courseRepository.save(course);
    }

}
