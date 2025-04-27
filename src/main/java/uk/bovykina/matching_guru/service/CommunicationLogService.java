package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.coms.CommunicationLogCreateDto;
import uk.bovykina.matching_guru.dto.coms.CommunicationLogDto;
import uk.bovykina.matching_guru.entity.CommunicationLog;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.mapper.CommunicationLogMapper;
import uk.bovykina.matching_guru.repository.CommunicationLogRepository;
import uk.bovykina.matching_guru.repository.MatchRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommunicationLogService {

    private final CommunicationLogRepository communicationLogRepository;
    private final MatchRepository matchRepository;
    private final CommunicationLogMapper communicationLogMapper;

    /**
     * Creates and saves a communication log for a match.
     */
    public CommunicationLogDto createCommunicationLog(CommunicationLogCreateDto dto) {
        log.info("Creating communication log for Match ID: {}", dto.getMatchId());

        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> {
                    log.error("Match not found with ID: {}", dto.getMatchId());
                    return new IllegalArgumentException("Match not found");
                });

        CommunicationLog entity = communicationLogMapper.toEntity(dto, match);
        entity = communicationLogRepository.save(entity);

        return communicationLogMapper.toDto(entity);
    }

    /**
     * Retrieves all communication logs linked to a specific match.
     */
    public List<CommunicationLogDto> getAllCommunicationLogsForMatch(Long matchId) {
        log.info("Fetching communication logs for Match ID: {}", matchId);

        return communicationLogRepository.findByMatchId(matchId).stream()
                .map(communicationLogMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all communication logs in the system.
     */
    public List<CommunicationLogDto> getAllCommunicationLogs() {
        log.info("Fetching all communication logs");

        return communicationLogRepository.findAll().stream()
                .map(communicationLogMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing communication log by ID.
     */
    public CommunicationLogDto updateCommunicationLog(Long id, CommunicationLogCreateDto dto) {
        CommunicationLog existing = communicationLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Log not found"));

        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        existing.setMatch(match);
        existing.setType(dto.getType());
        existing.setStatus(dto.getStatus());
        existing.setTimestamp(dto.getTimestamp());

        existing = communicationLogRepository.save(existing);
        return communicationLogMapper.toDto(existing);
    }

    /**
     * Deletes a communication log by ID.
     */
    public void deleteCommunicationLog(Long id) {
        CommunicationLog log = communicationLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Log not found"));
        communicationLogRepository.delete(log);
    }
}
