package uk.bovykina.matching_guru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.bovykina.matching_guru.entity.Programme;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
    List<Programme> findByOrganisationId(Long organisationId);

    Optional<Programme> findById(Long id);

    @Query("SELECT DISTINCT p FROM Programme p " +
            "JOIN p.programmeYears py " +
            "WHERE py.isActive = true AND p.organisation.id = :organisationId")
    List<Programme> findActiveProgrammesByOrganisationId(@Param("organisationId") Long organisationId);

    @Query("""
        SELECT DISTINCT p
        FROM Programme p
        JOIN ProgrammeYear py ON p.id = py.programme.id
        JOIN ParticipantInProgrammeYear participant ON py.id = participant.programmeYear.id
        WHERE participant.user.id = :userId
    """)
    List<Programme> findProgrammesByUserId(@Param("userId") Long userId);

}
