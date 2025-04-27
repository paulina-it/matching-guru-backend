package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.repository.MatchRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final MatchRepository matchRepository;

    /**
     * Exports match data for a given programme year to a CSV file.
     */
    public ByteArrayInputStream exportMatchesToCsv(Long programmeYearId, int page, int size) {
        Page<Match> matchPage = matchRepository.findByProgrammeYearId(programmeYearId, PageRequest.of(page, size));
        List<Match> matches = matchPage.getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Mentor ID,Mentor Name,Mentor Course,Mentor Academic Stage," +
                "Mentee ID,Mentee Name,Mentee Course,Mentee Academic Stage,Score,Status");

        for (Match match : matches) {
            writer.printf("%d,%s %s,%s,%s,%d,%s %s,%s,%s,%.2f,%s%n",
                    match.getMentor().getId(),
                    match.getMentor().getUser().getFirstName(),
                    match.getMentor().getUser().getLastName(),
                    match.getMentor().getCourse().getName(),
                    match.getMentor().getAcademicStage(),

                    match.getMentee().getId(),
                    match.getMentee().getUser().getFirstName(),
                    match.getMentee().getUser().getLastName(),
                    match.getMentee().getCourse().getName(),
                    match.getMentee().getAcademicStage(),

                    match.getCompatibilityScore(),
                    match.getStatus()
            );
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
