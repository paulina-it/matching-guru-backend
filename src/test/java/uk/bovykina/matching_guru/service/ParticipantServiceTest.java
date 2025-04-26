package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantCreateDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantResponseDto;
import uk.bovykina.matching_guru.dto.participant.ParticipantUpdateDto;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.ParticipantRole;
import uk.bovykina.matching_guru.repository.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProgrammeYearRepository programmeYearRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private EndSurveyResponseRepository endSurveyResponseRepository;

    @InjectMocks
    private ParticipantService participantService;

    private User user;
    private ProgrammeYear programmeYear;
    private Programme programme;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        programme = new Programme();
        programme.setId(1L);

        programmeYear = new ProgrammeYear();
        programmeYear.setId(10L);
        programmeYear.setAcademicYear("2024/25");
        programmeYear.setProgramme(programme);
        Course mockCourse = new Course();
        mockCourse.setId(1L);
        mockCourse.setName("Mock Course");
        user = new User();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setCourse(mockCourse);
    }

    @Test
    void createParticipant_shouldSetWasMatchedLastYearFalseIfPreviouslyUnmatched() {
        ParticipantCreateDto createDto = new ParticipantCreateDto();
        createDto.setUserId(user.getId());
        createDto.setProgrammeYearId(programmeYear.getId());
        createDto.setRole(ParticipantRole.MENTEE);

        ProgrammeYear pastYear = new ProgrammeYear();
        pastYear.setId(999L);
        pastYear.setProgramme(programmeYear.getProgramme());

        ParticipantInProgrammeYear previousUnmatched = new ParticipantInProgrammeYear();
        previousUnmatched.setUser(user);
        previousUnmatched.setProgrammeYear(pastYear);
        previousUnmatched.setIsMatched(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(programmeYearRepository.findById(programmeYear.getId())).thenReturn(Optional.of(programmeYear));
        when(participantRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(previousUnmatched)); // << MOCK HERE
        when(participantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ParticipantResponseDto result = participantService.createParticipant(createDto);

        assertFalse(result.getWasMatchedLastYear());
    }

    @Test
    void createParticipant_shouldSetWasMatchedLastYearTrueIfNoPreviousUnmatched() {
        ParticipantCreateDto createDto = new ParticipantCreateDto();
        createDto.setUserId(user.getId());
        createDto.setProgrammeYearId(programmeYear.getId());
        createDto.setRole(ParticipantRole.MENTEE);

        ParticipantInProgrammeYear previousMatched = new ParticipantInProgrammeYear();
        previousMatched.setUser(user);
        ProgrammeYear prevYear = new ProgrammeYear();
        prevYear.setId(5L);
        prevYear.setProgramme(programme);
        previousMatched.setProgrammeYear(prevYear);
        previousMatched.setIsMatched(true);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(programmeYearRepository.findById(programmeYear.getId())).thenReturn(Optional.of(programmeYear));
        when(participantRepository.findByUserId(user.getId())).thenReturn(Optional.of(previousMatched));
        when(participantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ParticipantResponseDto result = participantService.createParticipant(createDto);

        assertTrue(result.getWasMatchedLastYear());
    }

    @Test
    void createParticipant_shouldThrowIfUserNotFound() {
        ParticipantCreateDto createDto = new ParticipantCreateDto();
        createDto.setUserId(999L);
        createDto.setProgrammeYearId(programmeYear.getId());

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> participantService.createParticipant(createDto));
    }

    @Test
    void createParticipant_shouldThrowIfProgrammeYearNotFound() {
        ParticipantCreateDto createDto = new ParticipantCreateDto();
        createDto.setUserId(user.getId());
        createDto.setProgrammeYearId(999L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(programmeYearRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> participantService.createParticipant(createDto));
    }

    @Test
    void getParticipant_shouldReturnDtoIfExists() {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setId(1L);
        participant.setUser(user);
        participant.setProgrammeYear(programmeYear);
        participant.setRole(ParticipantRole.MENTEE);
        participant.setIsMatched(false);

        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(endSurveyResponseRepository.existsByParticipantInProgramme(participant)).thenReturn(false);

        ParticipantResponseDto result = participantService.getParticipant(1L);

        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getUserName());
    }

    @Test
    void getParticipant_shouldThrowIfNotFound() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> participantService.getParticipant(1L));
    }

    @Test
    void updateParticipant_shouldUpdateFields() {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setId(1L);
        participant.setUser(user);
        participant.setProgrammeYear(programmeYear);

        ParticipantUpdateDto updateDto = new ParticipantUpdateDto();
        updateDto.setMotivation("Grow skills");
        updateDto.setWasMatchedLastYear(true);

        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(participantRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(endSurveyResponseRepository.existsByParticipantInProgramme(participant)).thenReturn(false);

        ParticipantResponseDto result = participantService.updateParticipant(1L, updateDto);

        assertEquals("Grow skills", result.getMotivation());
        assertTrue(result.getWasMatchedLastYear());
    }

    @Test
    void getParticipantInfoByUserIdAndProgrammeYearId_shouldReturnMatchesIfExist() {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setId(1L);
        participant.setUser(user);
        participant.setProgrammeYear(programmeYear);

        Match match = new Match();
        match.setId(10L);

        when(participantRepository.findByUserIdAndProgrammeYearId(user.getId(), programmeYear.getId()))
                .thenReturn(Optional.of(participant));
        when(matchRepository.findByParticipantId(1L)).thenReturn(List.of(match));
        when(matchService.getDetailedMatchById(10L)).thenReturn(new DetailedMatchResponseDto());

        Object result = participantService.getParticipantInfoByUserIdAndProgrammeYearId(user.getId(), programmeYear.getId());

        assertTrue(result instanceof List);
        assertEquals(1, ((List<?>) result).size());
    }

    @Test
    void getParticipantInfoByUserIdAndProgrammeYearId_shouldReturnDtoIfNoMatches() {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setId(1L);
        participant.setUser(user);
        participant.setProgrammeYear(programmeYear);

        when(participantRepository.findByUserIdAndProgrammeYearId(user.getId(), programmeYear.getId()))
                .thenReturn(Optional.of(participant));
        when(matchRepository.findByParticipantId(1L)).thenReturn(Collections.emptyList());
        when(endSurveyResponseRepository.existsByParticipantInProgramme(participant)).thenReturn(false);

        Object result = participantService.getParticipantInfoByUserIdAndProgrammeYearId(user.getId(), programmeYear.getId());

        assertTrue(result instanceof ParticipantResponseDto);
        assertEquals(1L, ((ParticipantResponseDto) result).getUserId());
    }

}
