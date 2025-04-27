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
import uk.bovykina.matching_guru.mapper.CourseMapper;
import uk.bovykina.matching_guru.repository.CourseGroupRepository;
import uk.bovykina.matching_guru.repository.CourseRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final ProgrammeRepository programmeRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final OrganisationRepository organisationRepository;
    private final CourseMapper courseMapper;

    /**
     * Creates a new course within a course group.
     */
    public CourseDto createCourse(CourseCreateDto dto) {
        log.info("Creating course: {}", dto.getName());

        CourseGroup group = courseGroupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("CourseGroup not found with ID: " + dto.getGroupId()));

        Course course = new Course();
        course.setName(dto.getName());
        course.setType(dto.getType());
        course.setDuration(dto.getDuration());
        course.setGroup(group);

        return courseMapper.toDto(courseRepository.save(course));
    }

    /**
     * Retrieves a course by ID.
     */
    public CourseDto getCourseById(Long courseId) {
        log.info("Fetching course ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        return courseMapper.toDto(course);
    }

    /**
     * Retrieves all courses in a given group.
     */
    public List<CourseDto> getCoursesByGroupId(Long groupId) {
        log.info("Fetching courses for group ID: {}", groupId);

        return courseRepository.findByGroupId(groupId).stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves eligible courses for a programme based on its configured course groups.
     */
    public List<CourseDto> getEligibleCoursesByProgrammeId(Long programmeId) {
        log.info("Fetching eligible courses for programme ID: {}", programmeId);

        Programme programme = programmeRepository.findById(programmeId)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found with ID: " + programmeId));

        List<Long> courseGroupIds = programme.getEligibleCourseGroups().stream()
                .map(CourseGroup::getId)
                .toList();

        return courseRepository.findByGroupIds(courseGroupIds).stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates course details.
     */
    public CourseDto updateCourse(Long courseId, CourseUpdateDto dto) {
        log.info("Updating course ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        course.setName(dto.getName());
        course.setType(dto.getType());
        course.setDuration(dto.getDuration());

        return courseMapper.toDto(courseRepository.save(course));
    }

    /**
     * Deletes a course by ID.
     */
    public void deleteCourse(Long courseId) {
        log.info("Deleting course ID: {}", courseId);
        courseRepository.deleteById(courseId);
    }

    /**
     * Processes an uploaded course file (CSV or XLSX).
     */
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
                saveCourse(
                        record.get("CourseGroup"),
                        record.get("Type"),
                        record.get("CourseName"),
                        organisationId
                );
            }
        }
    }

    private void processXLSX(MultipartFile file, Long organisationId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                saveCourse(
                        row.getCell(0).getStringCellValue(),
                        row.getCell(1).getStringCellValue(),
                        row.getCell(2).getStringCellValue(),
                        organisationId
                );
            }
        }
    }

    private void saveCourse(String groupName, String type, String courseName, Long organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + organisationId));

        CourseGroup group = courseGroupRepository.findByNameAndOrganisationId(groupName, organisationId)
                .stream().findFirst()
                .orElseGet(() -> {
                    CourseGroup newGroup = new CourseGroup();
                    newGroup.setName(groupName);
                    newGroup.setOrganisation(organisation);
                    return courseGroupRepository.save(newGroup);
                });

        Course course = new Course();
        course.setName(courseName);
        course.setGroup(group);
        course.setType(CourseType.valueOf(type));

        courseRepository.save(course);
    }
}
