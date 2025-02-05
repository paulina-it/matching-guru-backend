package uk.bovykina.matching_guru.algorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.ProgrammeMatchingCriteriaRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
//@SpringBootTest
class GaleShapleyServiceTest {

    @InjectMocks
    private GaleShapleyService matchingService;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ProgrammeMatchingCriteriaRepository criteriaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
//    @Test
//    void testMatchParticipants() {
//        // Arrange
//        Long programmeYearId = 7L;
//
//        // Мокируем вызовы репозиториев
//        when(participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTOR))
//                .thenReturn(mentors);
//        when(participantRepository.findByProgrammeYearIdAndRole(programmeYearId, ParticipantRole.MENTEE))
//                .thenReturn(mentees);
//
//        when(criteriaRepository.findByProgrammeYearId(programmeYearId))
//                .thenReturn(List.of());  // Можно добавить вес критериев, если нужно
//
//        // Act
//        matchingService.matchParticipants(programmeYearId, true);
//
//        // Assert
//        // Проверяем, что количество сохраненных матчей соответствует числу участников
//        verify(matchRepository, times(5)).save(any(Match.class));
//
//        // Проверяем, что все менторы и менти были помечены как "matched"
//        for (ParticipantInProgrammeYear mentor : mentors) {
//            assertTrue(mentor.getIsMatched(), "Mentor should be matched");
//        }
//        for (ParticipantInProgrammeYear mentee : mentees) {
//            assertTrue(mentee.getIsMatched(), "Mentee should be matched");
//        }
//    }
    @Transactional
    @Test
    void testMatchParticipantsWithRealData() {
        // Arrange: Assume the participants have been inserted already
        Long programmeYearId = 7L;

        // Act: Run the matching algorithm
        matchingService.matchParticipants(programmeYearId, true);

        // Assert: Verify that the match was saved and both mentor and mentee were matched
        ParticipantInProgrammeYear mentor = participantRepository.findByRoleAndIsMatched(ParticipantRole.MENTOR, true);
        ParticipantInProgrammeYear mentee = participantRepository.findByRoleAndIsMatched(ParticipantRole.MENTEE, true);

        assertTrue(mentor.getIsMatched(), "Mentor should be matched");
        assertTrue(mentee.getIsMatched(), "Mentee should be matched");

        // Optionally, verify that the match was saved in the repository
        verify(matchRepository).save(any(Match.class));
    }

}
