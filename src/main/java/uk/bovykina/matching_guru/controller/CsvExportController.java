package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.service.CsvExportService;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class CsvExportController {

    private final CsvExportService csvExportService;

    @GetMapping("/matches")
    public ResponseEntity<InputStreamResource> exportMatchesToCsv(
            @RequestParam Long programmeYearId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        InputStreamResource file = new InputStreamResource(csvExportService.exportMatchesToCsv(programmeYearId, page, size));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matches_programme_" + programmeYearId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file);
    }
}
