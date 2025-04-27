package uk.bovykina.matching_guru.mapper;

import org.springframework.stereotype.Component;
import uk.bovykina.matching_guru.dto.coms.CommunicationLogCreateDto;
import uk.bovykina.matching_guru.dto.coms.CommunicationLogDto;
import uk.bovykina.matching_guru.entity.CommunicationLog;
import uk.bovykina.matching_guru.entity.Match;

@Component
public class CommunicationLogMapper {

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