package uk.bovykina.matching_guru.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import uk.bovykina.matching_guru.dto.match.*;
import uk.bovykina.matching_guru.entity.*;
import uk.bovykina.matching_guru.entity.enums.MatchStatus;
import uk.bovykina.matching_guru.entity.enums.UserRole;
import uk.bovykina.matching_guru.mapper.MatchMapper;
import uk.bovykina.matching_guru.repository.MatchRepository;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private UserRepository userRepository;
    @Mock private MatchMapper matchMapper;

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
        mentor.setIsMatched(true);

        mentee = new ParticipantInProgrammeYear();
        mentee.setId(2L);
        mentee.setProgrammeYear(programmeYear);
        mentee.setIsMatched(true);

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
        MatchResponseDto responseDto = new MatchResponseDto();
        responseDto.setId(100L);
        responseDto.setStatus(MatchStatus.APPROVED);

        when(participantRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(participantRepository.findById(2L)).thenReturn(Optional.of(mentee));
        when(matchRepository.existsByMentorIdAndMenteeId(1L, 2L)).thenReturn(false);
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(100L);
            m.setStatus(MatchStatus.APPROVED);
            return m;
        });
        when(matchMapper.toResponseDto(any(Match.class))).thenReturn(responseDto);

        MatchResponseDto result = matchService.createMatch(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(MatchStatus.APPROVED, result.getStatus());
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
        updateDto.setEditedByUserId(10L);

        User editor = new User();
        editor.setId(10L);
        editor.setRole(UserRole.ADMIN);

        MatchResponseDto expectedDto = new MatchResponseDto();
        expectedDto.setStatus(MatchStatus.APPROVED);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(userRepository.findById(10L)).thenReturn(Optional.of(editor));
        when(matchRepository.save(any())).thenReturn(match);
        when(matchMapper.toResponseDto(any())).thenReturn(expectedDto);

        MatchResponseDto result = matchService.updateMatchStatus(100L, updateDto);

        assertEquals(MatchStatus.APPROVED, result.getStatus());
    }

    @Test
    void getMatchById_shouldReturnDto() {
        MatchResponseDto dto = new MatchResponseDto();
        dto.setId(100L);
        dto.setCompatibilityScore(0.85);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(matchMapper.toResponseDto(match)).thenReturn(dto);

        MatchResponseDto result = matchService.getMatchById(100L);

        assertEquals(100L, result.getId());
        assertEquals(0.85, result.getCompatibilityScore());
    }

    @Test
    void getDetailedMatchById_shouldReturnDto() {
        DetailedMatchResponseDto detailedDto = new DetailedMatchResponseDto();
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(matchMapper.toDetailedResponseDto(match)).thenReturn(detailedDto);

        DetailedMatchResponseDto result = matchService.getDetailedMatchById(100L);

        assertNotNull(result);
    }

    @Test
    void getDetailedMatchByParticipantId_shouldReturnCorrectMatch() {
        DetailedMatchResponseDto detailedDto = new DetailedMatchResponseDto();

        when(matchRepository.findByParticipantAndProgrammeYear(1L, 1L)).thenReturn(Optional.of(match));
        when(matchMapper.toDetailedResponseDto(match)).thenReturn(detailedDto);

        DetailedMatchResponseDto result = matchService.getDetailedMatchByParticipantId(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void processParticipantDecision_shouldHandleRejection() {
        MatchDecisionDto dto = new MatchDecisionDto();
        dto.setMatchId(100L);
        dto.setDecision(MatchStatus.REJECTED);
        dto.setUserId(10L);
        dto.setRejectionReason("Not a good fit");

        User rejectingUser = new User();
        rejectingUser.setId(10L);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(userRepository.getReferenceById(10L)).thenReturn(rejectingUser);
        when(matchRepository.save(any())).thenReturn(match);

        matchService.processParticipantDecision(dto);

        assertEquals(MatchStatus.REJECTED, match.getStatus());
        assertFalse(mentor.getIsMatched());
        assertFalse(mentee.getIsMatched());

        verify(participantRepository).saveAll(List.of(mentor, mentee));
    }

    @Test
    void processParticipantDecision_shouldHandleAcceptance() {
        MatchDecisionDto dto = new MatchDecisionDto();
        dto.setMatchId(100L);
        dto.setDecision(MatchStatus.ACCEPTED);
        dto.setUserId(10L);

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        matchService.processParticipantDecision(dto);
        assertEquals(MatchStatus.ACCEPTED_BY_ONE_PARTY, match.getStatus());

        match.setStatus(MatchStatus.ACCEPTED_BY_ONE_PARTY);
        matchService.processParticipantDecision(dto);
        assertEquals(MatchStatus.ACCEPTED_BY_BOTH, match.getStatus());

        verify(matchRepository, times(2)).save(match);
    }
}
