package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.course.*;
import uk.bovykina.matching_guru.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    /**
     * Create a new course (manual).
     */
    @PostMapping("/create")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseCreateDto courseCreateDto) {
        log.info("📌 Creating course: {}", courseCreateDto.getName());
        return ResponseEntity.ok(courseService.createCourse(courseCreateDto));
    }

    /**
     * Create a new course via file upload.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCourses(@RequestParam("file") MultipartFile file) {
        try {
            courseService.processFile(file);
            return ResponseEntity.ok("File uploaded and processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file.");
        }
    }

    /**
     * Retrieve a course by ID.
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long courseId) {
        log.info("📌 Fetching course with ID: {}", courseId);
        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }

    /**
     * Get courses by group ID.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<CourseDto>> getCoursesByGroupId(@PathVariable Long groupId) {
        log.info("📌 Fetching courses for group ID: {}", groupId);
        return ResponseEntity.ok(courseService.getCoursesByGroupId(groupId));
    }

    /**
     * Get eligible courses for a programme ID.
     */
    @GetMapping("/programme/{programmeId}/eligible")
    public ResponseEntity<List<CourseDto>> getEligibleCoursesByProgrammeId(@PathVariable Long programmeId) {
        log.info("📌 Fetching eligible courses for programme ID: {}", programmeId);
        return ResponseEntity.ok(courseService.getEligibleCoursesByProgrammeId(programmeId));
    }

    /**
     * Update an existing course.
     */
    @PutMapping("/update/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long courseId, @RequestBody CourseUpdateDto courseUpdateDto) {
        log.info("📌 Updating course ID: {}", courseId);
        return ResponseEntity.ok(courseService.updateCourse(courseId, courseUpdateDto));
    }

    /**
     * Delete a course by ID.
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long courseId) {
        log.info("📌 Deleting course with ID: {}", courseId);
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok("✅ Course deleted successfully.");
    }
}
