
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analyzer {

    List<LogEntry> filterByTimeWindow(
            List<LogEntry> entries,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end) {

        return entries.stream()
                .filter(entry -> !entry.getTimestamp().isBefore(start))
                .filter(entry -> entry.getTimestamp().isBefore(end))
                .toList();
    }

    public AnalysisResult analyze(
            List<LogEntry> entries,
            Rulebook rulebook,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end) {

        List<LogEntry> filteredEntries =
                filterByTimeWindow(entries, start, end);

        return analyze(filteredEntries, rulebook);
    }

    public AnalysisResult analyze(List<LogEntry> entries, Rulebook rulebook) {

        Map<String, Integer> activitySummary = new HashMap<>();
        List<LogEntry> flaggedEntries = new ArrayList<>();
        Map<String, Integer> suspiciousIps = new HashMap<>();
        List<LogEntry> unknownPatterns = new ArrayList<>();

        // Count every known activity level
        for (LogEntry entry : entries) {

            String level = entry.getLevel();

            if (rulebook.containsLevel(level)) {
                activitySummary.put(
                        level,
                        activitySummary.getOrDefault(level, 0) + 1
                );
            } else {
                unknownPatterns.add(entry);
            }
        }

        // Find flagged entries
        for (LogEntry entry : entries) {

            if (rulebook.containsLevel(entry.getLevel())) {

                int severity = rulebook.getSeverityScore(entry.getLevel());

                if (severity >= 3) {
                    flaggedEntries.add(entry);
                }
            }
        }

        // Count ALL cleanly parsed entries for IPs that have
        // at least one flagged entry
        for (LogEntry flaggedEntry : flaggedEntries) {

            String ip = flaggedEntry.getSourceIp();

            int totalEntriesForIp = 0;

            for (LogEntry entry : entries) {
                if (entry.getSourceIp().equals(ip)) {
                    totalEntriesForIp++;
                }
            }

            suspiciousIps.put(ip, totalEntriesForIp);
        }

        // Sort flagged entries by severity, highest first
        flaggedEntries.sort(
                Comparator.comparingInt(
                        (LogEntry entry) ->
                                rulebook.getSeverityScore(entry.getLevel())
                ).reversed()
        );

        // Sort suspicious IPs by count, highest first
        suspiciousIps = suspiciousIps.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Integer>comparingByValue()
                                .reversed()
                )
                .collect(
                        java.util.LinkedHashMap::new,
                        (map, entry) ->
                                map.put(entry.getKey(), entry.getValue()),
                        Map::putAll
                );

        return new AnalysisResult(
                activitySummary,
                flaggedEntries,
                suspiciousIps,
                unknownPatterns
        );
    }
}

