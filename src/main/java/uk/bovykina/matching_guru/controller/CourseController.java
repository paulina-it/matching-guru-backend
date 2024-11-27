package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.course.*;
import uk.bovykina.matching_guru.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseCreateDto courseCreateDto) {
        return ResponseEntity.ok(courseService.createCourse(courseCreateDto));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<CourseDto>> getCoursesByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(courseService.getCoursesByGroupId(groupId));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}
