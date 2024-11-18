package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<ParticipantInProgrammeYear, Long> {
    List<ParticipantInProgrammeYear> findByUserId(Long userId);
    List<ParticipantInProgrammeYear> findByProgrammeYearId(Long programmeYearId);
    List<ParticipantInProgrammeYear> findByRole(ParticipantRole role);
}
