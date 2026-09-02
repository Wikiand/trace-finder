import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ReportWriterTest {

    @Test
    void reportContainsAllFiveSections() throws Exception {
        Rulebook rulebook = new Rulebook();

        rulebook.addRule("INFO", 1);
        rulebook.addRule("WARN", 3);

        LogEntry info = new LogEntry(
                1,
                "2024-03-15 02:14:33 | INFO | 192.168.1.10 | /home | view",
                LocalDateTime.of(2024, 3, 15, 2, 14, 33),
                "INFO",
                "192.168.1.10",
                "/home",
                "view"
        );

        LogEntry warn = new LogEntry(
                2,
                "2024-03-15 02:15:33 | WARN | 192.168.1.10 | /login | failed",
                LocalDateTime.of(2024, 3, 15, 2, 15, 33),
                "WARN",
                "192.168.1.10",
                "/login",
                "failed"
        );

        Analyzer analyzer = new Analyzer();

        AnalysisResult result = analyzer.analyze(
                Arrays.asList(info, warn),
                rulebook
        );

        Path output = Files.createTempFile("report", ".txt");

        ReportWriter writer = new ReportWriter();

        writer.write(
                output,
                result,
                rulebook,
                Arrays.asList()
        );

        String report = Files.readString(output);

        assertTrue(report.contains("Activity Summary"));
        assertTrue(report.contains("Flagged Entries"));
        assertTrue(report.contains("Suspicious Activity by IP"));
        assertTrue(report.contains("Unknown Patterns"));
        assertTrue(report.contains("Malformed Lines"));

        assertTrue(report.contains(
                "2024-03-15 02:15:33 | WARN | 192.168.1.10 | /login | failed"
        ));

        assertTrue(report.contains("192.168.1.10: 2"));

        Files.deleteIfExists(output);
    }
}