import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerTest {

    @Test
    void activitySummaryCountsKnownLevels() {
        Rulebook rulebook = new Rulebook();

        rulebook.addRule("INFO", 1);
        rulebook.addRule("WARN", 3);
        rulebook.addRule("ERROR", 5);
        rulebook.addRule("ALERT", 9);

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
                "2024-03-15 02:15:33 | WARN | 192.168.1.20 | /login | failed",
                LocalDateTime.of(2024, 3, 15, 2, 15, 33),
                "WARN",
                "192.168.1.20",
                "/login",
                "failed"
        );

        LogEntry error = new LogEntry(
                3,
                "2024-03-15 02:16:33 | ERROR | 192.168.1.30 | /admin | denied",
                LocalDateTime.of(2024, 3, 15, 2, 16, 33),
                "ERROR",
                "192.168.1.30",
                "/admin",
                "denied"
        );

        Analyzer analyzer = new Analyzer();

        AnalysisResult result = analyzer.analyze(
                Arrays.asList(info, warn, error),
                rulebook
        );

        Map<String, Integer> summary = result.getActivitySummary();

        assertEquals(1, summary.get("INFO"));
        assertEquals(1, summary.get("WARN"));
        assertEquals(1, summary.get("ERROR"));
    }

    @Test
    void severityThreeOrHigherIsFlagged() {
        Rulebook rulebook = new Rulebook();

        rulebook.addRule("INFO", 1);
        rulebook.addRule("WARN", 3);
        rulebook.addRule("ERROR", 5);
        rulebook.addRule("ALERT", 9);

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
                "2024-03-15 02:15:33 | WARN | 192.168.1.20 | /login | failed",
                LocalDateTime.of(2024, 3, 15, 2, 15, 33),
                "WARN",
                "192.168.1.20",
                "/login",
                "failed"
        );

        Analyzer analyzer = new Analyzer();

        AnalysisResult result = analyzer.analyze(
                Arrays.asList(info, warn),
                rulebook
        );

        assertEquals(1, result.getFlaggedEntries().size());
        assertEquals("WARN", result.getFlaggedEntries().get(0).getLevel());
    }

    @Test
    void unknownLevelIsRecordedAsUnknownPattern() {
        Rulebook rulebook = new Rulebook();

        rulebook.addRule("INFO", 1);
        rulebook.addRule("WARN", 3);

        LogEntry unknown = new LogEntry(
                5,
                "2024-03-15 02:14:33 | DEBUG | 192.168.1.10 | /home | view",
                LocalDateTime.of(2024, 3, 15, 2, 14, 33),
                "DEBUG",
                "192.168.1.10",
                "/home",
                "view"
        );

        Analyzer analyzer = new Analyzer();

        AnalysisResult result = analyzer.analyze(
                Arrays.asList(unknown),
                rulebook
        );

        assertEquals(1, result.getUnknownPatterns().size());
        assertEquals(
                "DEBUG",
                result.getUnknownPatterns().get(0).getLevel()
        );
    }

    @Test
    void suspiciousIpCountsAllEntriesFromFlaggedIp() {
        Rulebook rulebook = new Rulebook();

        rulebook.addRule("INFO", 1);
        rulebook.addRule("WARN", 3);
        rulebook.addRule("ERROR", 5);

        LogEntry info1 = new LogEntry(
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

        LogEntry info2 = new LogEntry(
                3,
                "2024-03-15 02:16:33 | INFO | 192.168.1.10 | /home | view",
                LocalDateTime.of(2024, 3, 15, 2, 16, 33),
                "INFO",
                "192.168.1.10",
                "/home",
                "view"
        );

        Analyzer analyzer = new Analyzer();

        AnalysisResult result = analyzer.analyze(
                Arrays.asList(info1, warn, info2),
                rulebook
        );

        assertEquals(
                3,
                result.getSuspiciousIps().get("192.168.1.10")
        );
    }

    @Test
    void timeWindowIncludesStartAndExcludesEnd() {
        Rulebook rulebook = new Rulebook();
        rulebook.addRule("INFO", 1);

        LogEntry atStart = new LogEntry(
                1,
                "2024-03-15 02:00:00 | INFO | 192.168.1.10 | /home | view",
                LocalDateTime.of(2024, 3, 15, 2, 0, 0),
                "INFO",
                "192.168.1.10",
                "/home",
                "view"
        );

        LogEntry inside = new LogEntry(
                2,
                "2024-03-15 02:30:00 | INFO | 192.168.1.20 | /home | view",
                LocalDateTime.of(2024, 3, 15, 2, 30, 0),
                "INFO",
                "192.168.1.20",
                "/home",
                "view"
        );

        LogEntry atEnd = new LogEntry(
                3,
                "2024-03-15 03:00:00 | INFO | 192.168.1.30 | /home | view",
                LocalDateTime.of(2024, 3, 15, 3, 0, 0),
                "INFO",
                "192.168.1.30",
                "/home",
                "view"
        );

        Analyzer analyzer = new Analyzer();

        List<LogEntry> filtered = analyzer.filterByTimeWindow(
                Arrays.asList(atStart, inside, atEnd),
                LocalDateTime.of(2024, 3, 15, 2, 0, 0),
                LocalDateTime.of(2024, 3, 15, 3, 0, 0)
        );

        assertEquals(2, filtered.size());
        assertEquals(1, filtered.get(0).getLineNumber());
        assertEquals(2, filtered.get(1).getLineNumber());
    }

}