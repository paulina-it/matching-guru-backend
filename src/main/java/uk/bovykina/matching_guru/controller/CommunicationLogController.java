package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.dto.coms.CommunicationLogCreateDto;
import uk.bovykina.matching_guru.dto.coms.CommunicationLogDto;
import uk.bovykina.matching_guru.service.CommunicationLogService;

import java.util.List;

@RestController
@RequestMapping("/communication-logs")
@RequiredArgsConstructor
@Slf4j
public class CommunicationLogController {

    private final CommunicationLogService communicationLogService;

    @PostMapping
    public ResponseEntity<CommunicationLogDto> createLog(@RequestBody CommunicationLogCreateDto createDto) {
        log.info("📨 Request to create communication log for match ID: {}", createDto.getMatchId());
        CommunicationLogDto createdLog = communicationLogService.createCommunicationLog(createDto);
        return ResponseEntity.ok(createdLog);
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<CommunicationLogDto>> getLogsByMatchId(@PathVariable Long matchId) {
        log.info("🔍 Request to fetch communication logs for match ID: {}", matchId);
        List<CommunicationLogDto> logs = communicationLogService.getAllCommunicationLogsForMatch(matchId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping
    public ResponseEntity<List<CommunicationLogDto>> getAllLogs() {
        log.info("📚 Request to fetch all communication logs");
        List<CommunicationLogDto> logs = communicationLogService.getAllCommunicationLogs();
        return ResponseEntity.ok(logs);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommunicationLogDto> updateLog(
            @PathVariable Long id,
            @RequestBody CommunicationLogCreateDto updateDto) {
        log.info("✏️ Request to update communication log ID: {}", id);
        CommunicationLogDto updatedLog = communicationLogService.updateCommunicationLog(id, updateDto);
        return ResponseEntity.ok(updatedLog);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        log.info("🗑️ Request to delete communication log ID: {}", id);
        communicationLogService.deleteCommunicationLog(id);
        return ResponseEntity.noContent().build();
    }

}
