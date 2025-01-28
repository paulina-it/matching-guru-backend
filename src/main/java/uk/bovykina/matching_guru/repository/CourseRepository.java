package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.bovykina.matching_guru.entity.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByGroupId(Long groupId);
    @Query("SELECT c FROM Course c WHERE c.group.id IN :groupIds")
    List<Course> findByGroupIds(@Param("groupIds") List<Long> groupIds);
}
