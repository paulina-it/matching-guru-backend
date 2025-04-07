package uk.bovykina.matching_guru.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.bovykina.matching_guru.algorithms.BraceService;
import uk.bovykina.matching_guru.algorithms.helpers.*;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.service.ProgrammeYearService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BraceTest {

    private ParticipantLoader participantLoader;
    private CompatibilityMatrixBuilder matrixBuilder;
    private MatchAssigner matchAssigner;
    private MatchSaver matchSaver;
    private ProgrammeYearService programmeYearService;
    private BraceService braceService;
    private ProgrammeYear testProgrammeYear;

    @BeforeEach
    void setup() {
        participantLoader = mock(ParticipantLoader.class);
        matrixBuilder = mock(CompatibilityMatrixBuilder.class);
        matchAssigner = mock(MatchAssigner.class);
        matchSaver = mock(MatchSaver.class);
        programmeYearService = mock(ProgrammeYearService.class);

        braceService = new BraceService(participantLoader, matrixBuilder, matchAssigner, matchSaver, programmeYearService);

        Programme programme = new Programme();
        programme.setId(1L);
        programme.setName("Test Programme");

        testProgrammeYear = new ProgrammeYear();
        testProgrammeYear.setId(100L);
        testProgrammeYear.setProgramme(programme);
        testProgrammeYear.setStrictAcademicStage(false);
        testProgrammeYear.setStrictCourseGroup(false);

        when(programmeYearService.getById(100L)).thenReturn(testProgrammeYear);
    }

    @Test
    void shouldPrioritisePreviouslyUnmatchedMentees() {
        ParticipantInProgrammeYear unmatchedMentee = new ParticipantInProgrammeYear();
        unmatchedMentee.setId(1L);
        unmatchedMentee.setRole(ParticipantRole.MENTEE);
        unmatchedMentee.setWasMatchedLastYear(false);

        ParticipantInProgrammeYear matchedMentee = new ParticipantInProgrammeYear();
        matchedMentee.setId(2L);
        matchedMentee.setRole(ParticipantRole.MENTEE);
        matchedMentee.setWasMatchedLastYear(true);

        ParticipantInProgrammeYear mentor = new ParticipantInProgrammeYear();
        mentor.setId(3L);
        mentor.setRole(ParticipantRole.MENTOR);
        mentor.setMenteesNumber(1);

        when(participantLoader.loadMentors(100L)).thenReturn(List.of(mentor));
        when(participantLoader.loadMentees(100L)).thenReturn(Arrays.asList(matchedMentee, unmatchedMentee));

        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibility = new HashMap<>();
        compatibility.put(mentor, new HashMap<>());
        compatibility.get(mentor).put(unmatchedMentee, 0.8);
        compatibility.get(mentor).put(matchedMentee, 0.8);

        when(matrixBuilder.build(any(), any(), eq(100L), eq(false), eq(false))).thenReturn(compatibility);

        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> assigned = new HashMap<>();
        assigned.put(mentor, unmatchedMentee);
        when(matchAssigner.assignMatches(any(), any(), any(), eq(testProgrammeYear))).thenReturn(assigned);

        braceService.match(100L);

        verify(matchSaver).save(eq(100L), eq(assigned), eq(compatibility));
        assertThat(assigned.get(mentor)).isEqualTo(unmatchedMentee);
    }

    @Test
    void shouldSkipMatchingWhenNoMentorsOrMentees() {
        when(participantLoader.loadMentors(100L)).thenReturn(Collections.emptyList());
        when(participantLoader.loadMentees(100L)).thenReturn(Collections.emptyList());

        braceService.match(100L);

        verifyNoInteractions(matrixBuilder, matchAssigner, matchSaver);
    }

    @Test
    void shouldHandleAllUnmatchedParticipants() {
        ParticipantInProgrammeYear mentee1 = new ParticipantInProgrammeYear();
        mentee1.setId(10L);
        mentee1.setWasMatchedLastYear(false);
        mentee1.setRole(ParticipantRole.MENTEE);

        ParticipantInProgrammeYear mentee2 = new ParticipantInProgrammeYear();
        mentee2.setId(11L);
        mentee2.setWasMatchedLastYear(false);
        mentee2.setRole(ParticipantRole.MENTEE);

        ParticipantInProgrammeYear mentor = new ParticipantInProgrammeYear();
        mentor.setId(20L);
        mentor.setMenteesNumber(1);
        mentor.setWasMatchedLastYear(false);
        mentor.setRole(ParticipantRole.MENTOR);

        when(participantLoader.loadMentors(100L)).thenReturn(List.of(mentor));
        when(participantLoader.loadMentees(100L)).thenReturn(List.of(mentee1, mentee2));

        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibility = new HashMap<>();
        compatibility.put(mentor, Map.of(mentee1, 0.7, mentee2, 0.6));

        when(matrixBuilder.build(any(), any(), eq(100L), anyBoolean(), anyBoolean())).thenReturn(compatibility);

        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> assigned = new HashMap<>();
        assigned.put(mentor, mentee1);
        when(matchAssigner.assignMatches(any(), any(), any(), eq(testProgrammeYear))).thenReturn(assigned);

        braceService.match(100L);

        verify(matchSaver).save(eq(100L), eq(assigned), eq(compatibility));
        assertThat(assigned.get(mentor)).isEqualTo(mentee1);
    }

    @Test
    void shouldRespectStrictStageAndGroupFlags() {
        testProgrammeYear.setStrictAcademicStage(true);
        testProgrammeYear.setStrictCourseGroup(true);

        ParticipantInProgrammeYear mentor = new ParticipantInProgrammeYear();
        mentor.setId(1L);
        mentor.setRole(ParticipantRole.MENTOR);
        mentor.setMenteesNumber(1);

        ParticipantInProgrammeYear mentee = new ParticipantInProgrammeYear();
        mentee.setId(2L);
        mentee.setRole(ParticipantRole.MENTEE);

        when(participantLoader.loadMentors(100L)).thenReturn(List.of(mentor));
        when(participantLoader.loadMentees(100L)).thenReturn(List.of(mentee));

        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibility = new HashMap<>();
        compatibility.put(mentor, Map.of(mentee, 1.0));

        when(matrixBuilder.build(any(), any(), eq(100L), eq(true), eq(true))).thenReturn(compatibility);

        Map<ParticipantInProgrammeYear, ParticipantInProgrammeYear> assigned = new HashMap<>();
        assigned.put(mentor, mentee);
        when(matchAssigner.assignMatches(any(), any(), any(), eq(testProgrammeYear))).thenReturn(assigned);

        braceService.match(100L);

        verify(matrixBuilder).build(any(), any(), eq(100L), eq(true), eq(true));
        verify(matchSaver).save(eq(100L), eq(assigned), eq(compatibility));
    }

    @Test
    void shouldNotAssignIfMentorCapacityIsZero() {
        ParticipantInProgrammeYear mentor = new ParticipantInProgrammeYear();
        mentor.setId(1L);
        mentor.setRole(ParticipantRole.MENTOR);
        mentor.setMenteesNumber(0);

        ParticipantInProgrammeYear mentee = new ParticipantInProgrammeYear();
        mentee.setId(2L);
        mentee.setRole(ParticipantRole.MENTEE);

        when(participantLoader.loadMentors(100L)).thenReturn(List.of(mentor));
        when(participantLoader.loadMentees(100L)).thenReturn(List.of(mentee));

        Map<ParticipantInProgrammeYear, Map<ParticipantInProgrammeYear, Double>> compatibility = new HashMap<>();
        compatibility.put(mentor, Map.of(mentee, 1.0));

        when(matrixBuilder.build(any(), any(), eq(100L), anyBoolean(), anyBoolean())).thenReturn(compatibility);
        when(matchAssigner.assignMatches(any(), any(), any(), eq(testProgrammeYear))).thenReturn(Collections.emptyMap());

        braceService.match(100L);

        verify(matchSaver).save(eq(100L), eq(Collections.emptyMap()), eq(compatibility));
    }
}
