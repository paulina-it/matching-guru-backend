package uk.bovykina.matching_guru.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.bovykina.matching_guru.algorithms.CompatibilityService;
import uk.bovykina.matching_guru.algorithms.GaleShapleyService;
import uk.bovykina.matching_guru.algorithms.helpers.MatchSaver;
import uk.bovykina.matching_guru.algorithms.helpers.MentorshipValidator;
import uk.bovykina.matching_guru.algorithms.helpers.MatchingCriteriaProvider;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GaleShapleyTest {

    private ParticipantRepository participantRepository;
    private CompatibilityService compatibilityService;
    private MentorshipValidator mentorshipValidator;
    private MatchingCriteriaProvider criteriaProvider;
    private MatchSaver matchSaver;
    private ProgrammeYearService programmeYearService;

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

        galeShapleyService = new GaleShapleyService(
                participantRepository,
                compatibilityService,
                mentorshipValidator,
                criteriaProvider,
                matchSaver,
                programmeYearService
        );

        Programme programme = new Programme();
        programme.setId(1L);
        programme.setName("Test Programme");

        programmeYear = new ProgrammeYear();
        programmeYear.setId(1L);
        programmeYear.setProgramme(programme);

        when(programmeYearService.getById(1L)).thenReturn(programmeYear);
        when(criteriaProvider.loadWeights(1L)).thenReturn(Map.of());
    }

    @Test
    void shouldPrioritiseUnmatchedMenteesInGaleShapley() {
        ParticipantInProgrammeYear mentor = createParticipant(100L, ParticipantRole.MENTOR, false);
        mentor.setMenteesNumber(1);

        ParticipantInProgrammeYear matchedMentee = createParticipant(201L, ParticipantRole.MENTEE, true);
        ParticipantInProgrammeYear unmatchedMentee = createParticipant(202L, ParticipantRole.MENTEE, false);

        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTOR)).thenReturn(List.of(mentor));
        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTEE)).thenReturn(List.of(matchedMentee, unmatchedMentee));
        when(mentorshipValidator.isCompatible(any(), any(), anyBoolean())).thenReturn(true);
        when(compatibilityService.calculate(any(), any(), anyMap())).thenReturn(0.9);

        ArgumentCaptor<Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>>> matchCaptor = ArgumentCaptor.forClass(Map.class);

        galeShapleyService.matchParticipants(1L, true);

        verify(matchSaver).saveMatches(eq(1L), eq(programmeYear), matchCaptor.capture(), anyMap());

        Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>> result = matchCaptor.getValue();

        assertThat(result).containsKey(mentor);
        assertThat(result.get(mentor)).hasSize(1);
        assertThat(result.get(mentor).get(0).getId()).isEqualTo(unmatchedMentee.getId());
    }

    @Test
    void shouldSkipIfNoMentorsOrMentees() {
        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTOR)).thenReturn(List.of());
        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTEE)).thenReturn(List.of());

        galeShapleyService.matchParticipants(1L, true);

        verifyNoInteractions(matchSaver);
    }

    @Test
    void shouldSkipMentorWithNoCapacity() {
        ParticipantInProgrammeYear mentor = createParticipant(101L, ParticipantRole.MENTOR, false);
        mentor.setMenteesNumber(0);

        ParticipantInProgrammeYear mentee = createParticipant(201L, ParticipantRole.MENTEE, false);

        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTOR)).thenReturn(List.of(mentor));
        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTEE)).thenReturn(List.of(mentee));
        when(mentorshipValidator.isCompatible(any(), any(), anyBoolean())).thenReturn(true);
        when(compatibilityService.calculate(any(), any(), anyMap())).thenReturn(0.9);

        ArgumentCaptor<Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>>> matchCaptor = ArgumentCaptor.forClass(Map.class);

        galeShapleyService.matchParticipants(1L, true);

        verify(matchSaver).saveMatches(eq(1L), eq(programmeYear), matchCaptor.capture(), anyMap());
        assertThat(matchCaptor.getValue()).doesNotContainKey(mentor);
    }

    @Test
    void shouldNotMatchIncompatibleParticipants() {
        ParticipantInProgrammeYear mentor = createParticipant(101L, ParticipantRole.MENTOR, false);
        mentor.setMenteesNumber(1);

        ParticipantInProgrammeYear mentee = createParticipant(201L, ParticipantRole.MENTEE, false);

        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTOR)).thenReturn(List.of(mentor));
        when(participantRepository.findByProgrammeYearIdAndRole(1L, ParticipantRole.MENTEE)).thenReturn(List.of(mentee));
        when(mentorshipValidator.isCompatible(any(), any(), anyBoolean())).thenReturn(false);

        ArgumentCaptor<Map<ParticipantInProgrammeYear, List<ParticipantInProgrammeYear>>> matchCaptor = ArgumentCaptor.forClass(Map.class);

        galeShapleyService.matchParticipants(1L, true);

        verify(matchSaver).saveMatches(eq(1L), eq(programmeYear), matchCaptor.capture(), anyMap());
        assertThat(matchCaptor.getValue()).isEmpty();
    }

    private ParticipantInProgrammeYear createParticipant(Long id, ParticipantRole role, boolean wasMatchedLastYear) {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setId(id);
        participant.setRole(role);
        participant.setWasMatchedLastYear(wasMatchedLastYear);

        User user = new User();
        user.setId(id);
        participant.setUser(user);

        return participant;
    }
}
