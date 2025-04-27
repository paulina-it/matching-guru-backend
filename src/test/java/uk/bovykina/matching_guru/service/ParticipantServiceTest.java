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
import uk.bovykina.matching_guru.mapper.ParticipantMapper;
import uk.bovykina.matching_guru.repository.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParticipantServiceTest {

    @Mock private ParticipantRepository participantRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProgrammeYearRepository programmeYearRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private MatchService matchService;
    @Mock private ParticipantMapper participantMapper;

    @InjectMocks private ParticipantService participantService;

    private User user;
    private Programme programme;
    private ProgrammeYear programmeYear;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        programme = new Programme();
        programme.setId(1L);

        programmeYear = new ProgrammeYear();
        programmeYear.setId(10L);
        programmeYear.setProgramme(programme);
        programmeYear.setAcademicYear("2024/25");

        user = new User();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
    }

    @Test
    void createParticipant_shouldSetWasMatchedLastYearFalseIfPreviouslyUnmatched() {
        ParticipantCreateDto dto = new ParticipantCreateDto();
        dto.setUserId(1L);
        dto.setProgrammeYearId(10L);
        dto.setRole(ParticipantRole.MENTEE);

        ProgrammeYear pastYear = new ProgrammeYear();
        pastYear.setId(5L);
        pastYear.setProgramme(programme);

        ParticipantInProgrammeYear previous = new ParticipantInProgrammeYear();
        previous.setProgrammeYear(pastYear);
        previous.setIsMatched(false);

        ParticipantInProgrammeYear created = new ParticipantInProgrammeYear();
        created.setId(100L);
        created.setWasMatchedLastYear(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(programmeYearRepository.findById(10L)).thenReturn(Optional.of(programmeYear));
        when(participantRepository.findAllByUserId(1L)).thenReturn(List.of(previous));
        when(participantMapper.toEntity(dto, user, programmeYear)).thenReturn(created);
        when(participantRepository.save(any())).thenReturn(created);
        when(participantMapper.toDto(created)).thenReturn(new ParticipantResponseDto() {{
            setWasMatchedLastYear(false);
        }});

        ParticipantResponseDto result = participantService.createParticipant(dto);
        assertFalse(result.getWasMatchedLastYear());
    }


    @Test
    void createParticipant_shouldThrowIfUserNotFound() {
        ParticipantCreateDto dto = new ParticipantCreateDto();
        dto.setUserId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> participantService.createParticipant(dto));
    }

    @Test
    void getParticipant_shouldReturnDto() {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setId(1L);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(participantMapper.toDto(participant)).thenReturn(new ParticipantResponseDto() {{ setId(1L); }});

        ParticipantResponseDto result = participantService.getParticipant(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getParticipant_shouldThrowIfNotFound() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> participantService.getParticipant(1L));
    }

    @Test
    void updateParticipant_shouldApplyChanges() {
        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setId(1L);

        ParticipantUpdateDto updateDto = new ParticipantUpdateDto();
        updateDto.setMotivation("Grow");

        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        doAnswer(inv -> {
            participant.setMotivation("Grow");
            return null;
        }).when(participantMapper).updateEntity(participant, updateDto);
        when(participantRepository.save(participant)).thenReturn(participant);
        when(participantMapper.toDto(participant)).thenReturn(new ParticipantResponseDto() {{
            setMotivation("Grow");
        }});

        ParticipantResponseDto result = participantService.updateParticipant(1L, updateDto);
        assertEquals("Grow", result.getMotivation());
    }

    @Test
    void getParticipantInfoByUserId_shouldReturnMatches() {
        ParticipantInProgrammeYear p = new ParticipantInProgrammeYear();
        p.setId(1L);

        Match match = new Match();
        match.setId(10L);

        when(participantRepository.findByUserId(1L)).thenReturn(Optional.of(p));
        when(matchRepository.findByParticipantId(1L)).thenReturn(List.of(match));
        when(matchService.getDetailedMatchById(10L)).thenReturn(new DetailedMatchResponseDto());

        Object result = participantService.getParticipantInfoByUserId(1L);
        assertTrue(result instanceof List);
        assertEquals(1, ((List<?>) result).size());
    }

    @Test
    void getParticipantInfoByUserId_shouldReturnDtoIfNoMatches() {
        ParticipantInProgrammeYear p = new ParticipantInProgrammeYear();
        p.setId(1L);

        when(participantRepository.findByUserId(1L)).thenReturn(Optional.of(p));
        when(matchRepository.findByParticipantId(1L)).thenReturn(List.of());
        when(participantMapper.toDto(p)).thenReturn(new ParticipantResponseDto() {{ setUserId(1L); }});

        Object result = participantService.getParticipantInfoByUserId(1L);
        assertTrue(result instanceof ParticipantResponseDto);
        assertEquals(1L, ((ParticipantResponseDto) result).getUserId());
    }
}
