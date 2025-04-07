package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.bovykina.matching_guru.dto.match.DetailedMatchResponseDto;
import uk.bovykina.matching_guru.dto.match.MatchCreateDto;
import uk.bovykina.matching_guru.dto.match.MatchResponseDto;
import uk.bovykina.matching_guru.dto.match.MatchStatusUpdateDto;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.mapper.MatchMapper;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchService matchService;

    private ParticipantInProgrammeYear mentor;
    private ParticipantInProgrammeYear mentee;
    private ProgrammeYear programmeYear;
    private Match match;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        programmeYear = new ProgrammeYear();
        programmeYear.setId(1L);

        mentor = new ParticipantInProgrammeYear();
        mentor.setId(1L);
        mentor.setProgrammeYear(programmeYear);

        mentee = new ParticipantInProgrammeYear();
        mentee.setId(2L);
        mentee.setProgrammeYear(programmeYear);

        match = new Match();
        match.setId(100L);
        match.setMentor(mentor);
        match.setMentee(mentee);
        match.setProgrammeYear(programmeYear);
        match.setStatus(MatchStatus.PENDING);
        match.setCompatibilityScore(0.85);
    }

    @Test
    void createMatch_shouldReturnDtoIfSuccessful() {
        MatchCreateDto dto = new MatchCreateDto(1L, 1L, 2L, 0.85, MatchStatus.PENDING);
        MatchResponseDto responseDto = new MatchResponseDto(100L, 1L, 1L, "Mentor", "Year 2", "CS", 0.85,
                2L, "Mentee", "Year 1", "IT", MatchStatus.PENDING);

        when(participantRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(participantRepository.findById(2L)).thenReturn(Optional.of(mentee));
        when(matchRepository.existsByMentorIdAndMenteeId(1L, 2L)).thenReturn(false);
        when(matchRepository.save(any())).thenReturn(match);
        when(matchMapper.toResponseDto(match)).thenReturn(responseDto);

        MatchResponseDto result = matchService.createMatch(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void createMatch_shouldReturnNullIfExists() {
        MatchCreateDto dto = new MatchCreateDto(1L, 1L, 2L, 0.85, MatchStatus.PENDING);

        when(participantRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(participantRepository.findById(2L)).thenReturn(Optional.of(mentee));
        when(matchRepository.existsByMentorIdAndMenteeId(1L, 2L)).thenReturn(true);

        MatchResponseDto result = matchService.createMatch(dto);

        assertNull(result);
    }

    @Test
    void updateMatchStatus_shouldUpdateStatus() {
        MatchStatusUpdateDto updateDto = new MatchStatusUpdateDto();
        updateDto.setStatus(MatchStatus.APPROVED);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        match.setStatus(MatchStatus.APPROVED);
        when(matchRepository.save(any())).thenReturn(match);

        MatchResponseDto expectedDto = new MatchResponseDto(100L, 1L, 1L, "Mentor", "Year 2", "CS", 0.85,
                2L, "Mentee", "Year 1", "IT", MatchStatus.APPROVED);
        when(matchMapper.toResponseDto(match)).thenReturn(expectedDto);

        MatchResponseDto result = matchService.updateMatchStatus(100L, updateDto);

        assertEquals(MatchStatus.APPROVED, result.getStatus());
    }

    @Test
    void getMatchById_shouldReturnDto() {
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        MatchResponseDto responseDto = new MatchResponseDto(100L, 1L, 1L, "Mentor", "Year 2", "CS", 0.85,
                2L, "Mentee", "Year 1", "IT", MatchStatus.PENDING);
        when(matchMapper.toResponseDto(match)).thenReturn(responseDto);

        MatchResponseDto result = matchService.getMatchById(100L);

        assertEquals(100L, result.getId());
        assertEquals(0.85, result.getCompatibilityScore());
    }

    @Test
    void getDetailedMatchById_shouldReturnDto() {
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        DetailedMatchResponseDto detailedDto = new DetailedMatchResponseDto();
        when(matchMapper.toDetailedResponseDto(match)).thenReturn(detailedDto);

        DetailedMatchResponseDto result = matchService.getDetailedMatchById(100L);

        assertNotNull(result);
    }

    @Test
    void getDetailedMatchByParticipantId_shouldReturnCorrectMatch() {
        when(matchRepository.findByMentorIdOrMenteeId(1L)).thenReturn(List.of(match));
        DetailedMatchResponseDto dto = new DetailedMatchResponseDto();
        when(matchMapper.toDetailedResponseDto(match)).thenReturn(dto);

        DetailedMatchResponseDto result = matchService.getDetailedMatchByParticipantId(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getAllMatches_shouldReturnAll() {
        when(matchRepository.findAll()).thenReturn(List.of(match));
        MatchResponseDto responseDto = new MatchResponseDto(100L, 1L, 1L, "Mentor", "Year 2", "CS", 0.85,
                2L, "Mentee", "Year 1", "IT", MatchStatus.PENDING);
        when(matchMapper.toResponseDto(match)).thenReturn(responseDto);

        List<MatchResponseDto> result = matchService.getAllMatches();

        assertEquals(1, result.size());
    }
}