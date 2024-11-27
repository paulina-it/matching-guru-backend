package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.course.*;
import uk.bovykina.matching_guru.service.CourseGroupService;

import java.util.List;

@RestController
@RequestMapping("/course-groups")
@RequiredArgsConstructor
public class CourseGroupController {

    private final CourseGroupService courseGroupService;

    @PostMapping("/create")
    public ResponseEntity<CourseGroupDto> createCourseGroup(@RequestBody CourseGroupCreateDto courseGroupCreateDto) {
        return ResponseEntity.ok(courseGroupService.createCourseGroup(courseGroupCreateDto));
    }

    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<List<CourseGroupDto>> getCourseGroupsByOrganisationId(@PathVariable Long organisationId) {
        List<CourseGroupDto> courseGroups = courseGroupService.getCourseGroupsByOrganisationId(organisationId);
        return ResponseEntity.ok(courseGroups);
    }

    @DeleteMapping("/{courseGroupId}")
    public ResponseEntity<Void> deleteCourseGroup(@PathVariable Long courseGroupId) {
        courseGroupService.deleteCourseGroup(courseGroupId);
        return ResponseEntity.noContent().build();
    }
}
