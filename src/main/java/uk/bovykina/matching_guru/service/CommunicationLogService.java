package uk.bovykina.matching_guru.service;

import jakarta.transaction.Transactional;
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
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        CommunicationLog communicationLog = communicationLogMapper.toEntity(dto, match);
        communicationLog = communicationLogRepository.save(communicationLog);
        return communicationLogMapper.toDto(communicationLog);
    }

    public List<CommunicationLogDto> getAllCommunicationLogsForMatch(Long matchId) {
        return communicationLogRepository.findByMatchId(matchId).stream()
                .map(communicationLogMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommunicationLogDto> getAllCommunicationLogs() {
        return communicationLogRepository.findAll().stream()
                .map(communicationLogMapper::toDto)
                .collect(Collectors.toList());
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
