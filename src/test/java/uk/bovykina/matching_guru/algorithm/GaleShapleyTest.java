package uk.bovykina.matching_guru.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.bovykina.matching_guru.algorithms.CompatibilityService;
import uk.bovykina.matching_guru.algorithms.GaleShapleyService;
import uk.bovykina.matching_guru.algorithms.helpers.MatchSaver;
import uk.bovykina.matching_guru.algorithms.helpers.MentorshipValidator;
import uk.bovykina.matching_guru.algorithms.helpers.MatchingCriteriaProvider;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GaleShapleyTest {

    private ParticipantRepository participantRepository;
    private CompatibilityService compatibilityService;
    private MentorshipValidator mentorshipValidator;
    private MatchingCriteriaProvider criteriaProvider;
    private MatchSaver matchSaver;
    private ProgrammeYearService programmeYearService;
    private MatchRepository matchRepository;
    private GaleShapleyService galeShapleyService;
    private ProgrammeYear programmeYear;

    @BeforeEach
    void setUp() {
        participantRepository = mock(ParticipantRepository.class);
        compatibilityService = mock(CompatibilityService.class);
        mentorshipValidator = mock(MentorshipValidator.class);
        criteriaProvider = mock(MatchingCriteriaProvider.class);
        matchSaver = mock(MatchSaver.class);
        programmeYearService = mock(ProgrammeYearService.class);
        matchRepository = mock(MatchRepository.class);

        galeShapleyService = new GaleShapleyService(
                participantRepository,
                compatibilityService,
                mentorshipValidator,
                criteriaProvider,
                matchSaver,
                programmeYearService,
                matchRepository
        );

        Programme programme = new Programme();
        programme.setId(1L);
        programme.setName("Test Programme");

        programmeYear = new ProgrammeYear();
        programmeYear.setId(1L);
        programmeYear.setProgramme(programme);

        when(programmeYearService.getById(1L)).thenReturn(programmeYear);
        when(criteriaProvider.loadWeights(1L)).thenReturn(Map.of());

        when(compatibilityService.calculate(any(), any(), anyMap())).thenReturn(0.9);
        when(mentorshipValidator.isCompatible(any(), any(), anyBoolean())).thenReturn(true);
    }

    @Test
    void shouldPrioritiseUnmatchedMenteesInGaleShapley() {
        ParticipantInProgrammeYear mentor = createParticipant(100L, ParticipantRole.MENTOR, false);
        mentor.setMenteesNumber(1);

        ParticipantInProgrammeYear matchedMentee = createParticipant(201L, ParticipantRole.MENTEE, true);
        ParticipantInProgrammeYear unmatchedMentee = createParticipant(202L, ParticipantRole.MENTEE, false);

        CourseGroup group = new CourseGroup();
        group.setId(1L);

        Course mentorCourse = new Course();
        mentorCourse.setId(100L);
        mentorCourse.setGroup(group);

        Course menteeCourse = new Course();
        menteeCourse.setId(200L);
        menteeCourse.setGroup(group);

        mentor.getUser().setCourse(mentorCourse);
        unmatchedMentee.getUser().setCourse(menteeCourse);
        matchedMentee.getUser().setCourse(menteeCourse);

        setupMockParticipants(List.of(mentor), List.of(matchedMentee, unmatchedMentee));

        when(mentorshipValidator.isCompatibleWithHistoryCheck(any(), any(), anyBoolean(), anyBoolean()))
                .thenReturn(true);
        when(compatibilityService.calculate(any(), any(), anyMap()))
                .thenReturn(0.9);

        ArgumentCaptor<Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>>> matchCaptor = ArgumentCaptor.forClass(Map.class);

        galeShapleyService.matchParticipants(1L, true);

        verify(matchSaver).saveMatches(eq(1L), eq(programmeYear), matchCaptor.capture(), anyMap());

        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> result = matchCaptor.getValue();
        assertThat(result).isNotEmpty(); // <-- fixed
        assertThat(result.get(mentor)).hasSize(1);
        assertThat(result.get(mentor).get(0).getId()).isEqualTo(unmatchedMentee.getId());
    }


    private ParticipantInProgrammeYear createParticipant(Long id, ParticipantRole role, boolean wasMatchedLastYear) {
        ParticipantInProgrammeYear p = new ParticipantInProgrammeYear();
        p.setId(id);
        p.setRole(role);
        p.setWasMatchedLastYear(wasMatchedLastYear);
        User u = new User();
        u.setId(id);
        p.setUser(u);
        return p;
    }

    private void setupMockParticipants(List<ParticipantInProgrammeYear> mentors, List<ParticipantInProgrammeYear> mentees) {
        List<ParticipantInProgrammeYear> all = new ArrayList<>();
        all.addAll(mentors);
        all.addAll(mentees);
        when(participantRepository.findByProgrammeYearId(1L)).thenReturn(all);
    }
}
