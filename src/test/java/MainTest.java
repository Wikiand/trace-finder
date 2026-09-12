import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void mainRejectsWrongNumberOfArguments()
            throws TraceFinderArgumentException {

        assertThrows(
                TraceFinderArgumentException.class,
                () -> Main.run(new String[]{"logs.txt"})
        );
    }

    @Test
    void mainRejectsTooManyArguments()
            throws TraceFinderArgumentException {

        assertThrows(
                TraceFinderArgumentException.class,
                () -> Main.run(
                        new String[]{
                                "logs.txt",
                                "rules.csv",
                                "report.txt",
                                "extra.txt"
                        }
                )
        );
    }

    @Test
    void mainRejectsUnreadableTimestamp()
            throws TraceFinderArgumentException {

        assertThrows(
                TraceFinderArgumentException.class,
                () -> Main.run(
                        new String[]{
                                "logs.txt",
                                "rules.csv",
                                "report.txt",
                                "not-a-timestamp",
                                "2024-03-15 03:00:00"
                        }
                )
        );
    }

    @Test
    void mainRejectsReversedTimeWindow()
            throws TraceFinderArgumentException {

        assertThrows(
                TraceFinderArgumentException.class,
                () -> Main.run(
                        new String[]{
                                "logs.txt",
                                "rules.csv",
                                "report.txt",
                                "2024-03-15 04:00:00",
                                "2024-03-15 03:00:00"
                        }
                )
        );
    }

    @Test
    void malformedLinesRemainInReport()
            throws Exception {

        Path logs = Files.createTempFile("logs", ".txt");
        Path rules = Files.createTempFile("rules", ".csv");
        Path report = Files.createTempFile("report", ".txt");

        Files.writeString(
                logs,
                "2024-03-15 02:00:00 | INFO | 192.168.1.10 | /home | view\n" +
                "this is malformed\n"
        );

        Files.writeString(
                rules,
                "level,severity_score\n" +
                "INFO,1\n"
        );

        int exitCode = Main.run(
                new String[]{
                        logs.toString(),
                        rules.toString(),
                        report.toString(),
                        "2024-03-15 03:00:00",
                        "2024-03-15 04:00:00"
                }
        );

        assertEquals(0, exitCode);

        String reportContent = Files.readString(report);

        assertTrue(reportContent.contains("Malformed Lines"));
        assertTrue(reportContent.contains("Line 2: this is malformed"));

        Files.deleteIfExists(logs);
        Files.deleteIfExists(rules);
        Files.deleteIfExists(report);
    }

    @Test
    void missingLogFileProducesFileException() {

        Path rules = Path.of("test-data/rules.csv");
        Path report = Path.of("target/test-report.txt");

        assertThrows(
                TraceFinderFileException.class,
                () -> Main.run(
                        new String[]{
                                "does-not-exist.log",
                                rules.toString(),
                                report.toString()
                        }
                )
        );
    }

    @Test
    void invalidRulebookProducesRulebookException()
            throws Exception {

        Path logs = Files.createTempFile("logs", ".txt");
        Path rules = Files.createTempFile("rules", ".csv");
        Path report = Files.createTempFile("report", ".txt");

        Files.writeString(
                logs,
                "2024-03-15 02:00:00 | INFO | 192.168.1.10 | /home | view\n"
        );

        Files.writeString(
                rules,
                "invalid,header\n"
        );

        assertThrows(
                RulebookException.class,
                () -> Main.run(
                        new String[]{
                                logs.toString(),
                                rules.toString(),
                                report.toString()
                        }
                )
        );

        Files.deleteIfExists(logs);
        Files.deleteIfExists(rules);
        Files.deleteIfExists(report);
    }

    @Test
    void unwritableReportPathProducesFileException()
            throws Exception {

        Path logs = Files.createTempFile("logs", ".txt");
        Path rules = Files.createTempFile("rules", ".csv");
        Path reportDirectory =
                Files.createTempDirectory("report-directory");

        Files.writeString(
                logs,
                "2024-03-15 02:00:00 | INFO | 192.168.1.10 | /home | view\n"
        );

        Files.writeString(
                rules,
                "level,severity_score\n" +
                "INFO,1\n"
        );

        assertThrows(
                TraceFinderFileException.class,
                () -> Main.run(
                        new String[]{
                                logs.toString(),
                                rules.toString(),
                                reportDirectory.toString()
                        }
                )
        );

        Files.deleteIfExists(logs);
        Files.deleteIfExists(rules);
        Files.deleteIfExists(reportDirectory);
    }
}