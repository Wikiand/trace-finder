import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReportWriter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void write(
            Path outputPath,
            AnalysisResult result,
            Rulebook rulebook,
            java.util.List<MalformedLine> malformedLines) throws IOException {

        StringBuilder report = new StringBuilder();

        report.append("=== Log Analysis Report ===\n");
        report.append("Generated: ")
                .append(LocalDateTime.now().format(TIMESTAMP_FORMAT))
                .append("\n");
        report.append("Time Window: full file\n");

        // Activity Summary
        report.append("\nActivity Summary\n");
        report.append("================\n");

        for (Map.Entry<String, Integer> rule :
                rulebook.getRules().entrySet()) {

            String level = rule.getKey();
            int count = result.getActivitySummary()
                    .getOrDefault(level, 0);

            report.append(level)
                    .append(": ")
                    .append(count)
                    .append("\n");
        }

        if (rulebook.getRules().isEmpty()) {
            report.append("None found.\n");
        }

        // Flagged Entries
        report.append("\nFlagged Entries\n");
        report.append("===============\n");

        if (result.getFlaggedEntries().isEmpty()) {
            report.append("None found.\n");
        } else {
            for (LogEntry entry : result.getFlaggedEntries()) {
                report.append(entry.getRawContent())
                        .append("\n");
            }
        }

        // Suspicious Activity by IP
        report.append("\nSuspicious Activity by IP\n");
        report.append("=========================\n");

        if (result.getSuspiciousIps().isEmpty()) {
            report.append("None found.\n");
        } else {
            for (Map.Entry<String, Integer> entry :
                    result.getSuspiciousIps().entrySet()) {

                report.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }
        }

        // Unknown Patterns
        report.append("\nUnknown Patterns\n");
        report.append("================\n");

        if (result.getUnknownPatterns().isEmpty()) {
            report.append("None found.\n");
        } else {
            for (LogEntry entry : result.getUnknownPatterns()) {
                report.append("Line ")
                        .append(entry.getLineNumber())
                        .append(": ")
                        .append(entry.getRawContent())
                        .append("\n");
            }
        }

        // Malformed Lines
        report.append("\nMalformed Lines\n");
        report.append("===============\n");

        if (malformedLines.isEmpty()) {
            report.append("None found.\n");
        } else {
            for (MalformedLine line : malformedLines) {
                report.append("Line ")
                        .append(line.getLineNumber())
                        .append(": ")
                        .append(line.getRawContent())
                        .append("\n");
            }
        }

        Files.writeString(outputPath, report.toString());
    }
}
