package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.course.*;
import uk.bovykina.matching_guru.service.CourseGroupService;

import java.util.List;

@RestController
@RequestMapping("/course-groups")
@RequiredArgsConstructor
@Slf4j
public class CourseGroupController {

    private final CourseGroupService courseGroupService;

    /**
     * Create a new course group.
     */
    @PostMapping("/create")
    public ResponseEntity<CourseGroupDto> createCourseGroup(@RequestBody CourseGroupCreateDto courseGroupCreateDto) {
        log.info("📌 Creating course group: {}", courseGroupCreateDto.getName());
        return ResponseEntity.ok(courseGroupService.createCourseGroup(courseGroupCreateDto));
    }

    /**
     * Get a course group by ID.
     */
    @GetMapping("/{courseGroupId}")
    public ResponseEntity<CourseGroupDto> getCourseGroupById(@PathVariable Long courseGroupId) {
        log.info("📌 Fetching course group with ID: {}", courseGroupId);
        return ResponseEntity.ok(courseGroupService.getCourseGroupById(courseGroupId));
    }

    /**
     * Get course groups by organisation ID.
     */
    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<List<CourseGroupDto>> getCourseGroupsByOrganisationId(@PathVariable Long organisationId) {
        log.info("📌 Fetching course groups for organisation ID: {}", organisationId);
        return ResponseEntity.ok(courseGroupService.getCourseGroupsByOrganisationId(organisationId));
    }

    /**
     * Update an existing course group.
     */
    @PutMapping("/update/{courseGroupId}")
    public ResponseEntity<CourseGroupDto> updateCourseGroup(@PathVariable Long courseGroupId, @RequestBody CourseGroupUpdateDto courseGroupUpdateDto) {
        log.info("📌 Updating course group ID: {}", courseGroupId);
        return ResponseEntity.ok(courseGroupService.updateCourseGroup(courseGroupId, courseGroupUpdateDto));
    }

    /**
     * Delete a course group by ID.
     */
    @DeleteMapping("/{courseGroupId}")
    public ResponseEntity<String> deleteCourseGroup(@PathVariable Long courseGroupId) {
        log.info("📌 Deleting course group with ID: {}", courseGroupId);
        courseGroupService.deleteCourseGroup(courseGroupId);
        return ResponseEntity.ok("✅ Course group deleted successfully.");
    }
}
