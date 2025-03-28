package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.dto.coms.*;
import uk.bovykina.matching_guru.entity.CommunicationLog;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.repository.CommunicationLogRepository;
import uk.bovykina.matching_guru.repository.MatchRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CommunicationLogService {

    private final CommunicationLogRepository communicationLogRepository;
    private final MatchRepository matchRepository;

    @Autowired
    public CommunicationLogService(CommunicationLogRepository communicationLogRepository,
                                   MatchRepository matchRepository) {
        this.communicationLogRepository = communicationLogRepository;
        this.matchRepository = matchRepository;
    }

    public CommunicationLogDto createCommunicationLog(CommunicationLogCreateDto dto) {
        log.info("📝 Creating communication log for Match ID: {}", dto.getMatchId());

        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> {
                    log.error("❌ Match not found with ID: {}", dto.getMatchId());
                    return new IllegalArgumentException("Match not found");
                });

        CommunicationLog communicationLog = communicationLogMapper.toEntity(dto, match);
        communicationLog = communicationLogRepository.save(communicationLog);

        log.info("✅ Communication log created with ID: {} for Match ID: {}", communicationLog.getId(), dto.getMatchId());
        return communicationLogMapper.toDto(communicationLog);
    }

    public List<CommunicationLogDto> getAllCommunicationLogsForMatch(Long matchId) {
        log.info("📂 Retrieving communication logs for Match ID: {}", matchId);

        List<CommunicationLogDto> logs = communicationLogRepository.findByMatchId(matchId).stream()
                .map(communicationLogMapper::toDto)
                .collect(Collectors.toList());

        log.info("📄 Found {} communication logs for Match ID: {}", logs.size(), matchId);
        return logs;
    }

    public List<CommunicationLogDto> getAllCommunicationLogs() {
        log.info("📂 Retrieving all communication logs");

        List<CommunicationLogDto> logs = communicationLogRepository.findAll().stream()
                .map(communicationLogMapper::toDto)
                .collect(Collectors.toList());

        log.info("📄 Retrieved total {} communication logs", logs.size());
        return logs;
    }

    public CommunicationLogDto updateCommunicationLog(Long id, CommunicationLogCreateDto dto) {
        CommunicationLog log = communicationLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Log not found"));

        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        log.setMatch(match);
        log.setType(dto.getType());
        log.setStatus(dto.getStatus());
        log.setTimestamp(dto.getTimestamp());

        log = communicationLogRepository.save(log);
        return communicationLogMapper.toDto(log);
    }

    public void deleteCommunicationLog(Long id) {
        CommunicationLog log = communicationLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Log not found"));
        communicationLogRepository.delete(log);
    }

    private final CommunicationLogMapper communicationLogMapper = new CommunicationLogMapper();

    private static class CommunicationLogMapper {

        public CommunicationLogDto toDto(CommunicationLog log) {
            CommunicationLogDto dto = new CommunicationLogDto();
            dto.setId(log.getId());
            dto.setMatchId(log.getMatch().getId());
            dto.setType(log.getType());
            dto.setTimestamp(log.getTimestamp());
            dto.setStatus(log.getStatus());
            return dto;
        }

        public CommunicationLog toEntity(CommunicationLogCreateDto dto, Match match) {
            CommunicationLog log = new CommunicationLog();
            log.setMatch(match);
            log.setType(dto.getType());
            log.setTimestamp(dto.getTimestamp());
            log.setStatus(dto.getStatus());
            return log;
        }
    }
}
