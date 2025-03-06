package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
