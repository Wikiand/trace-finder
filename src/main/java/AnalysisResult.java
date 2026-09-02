import java.util.List;
import java.util.Map;

public class AnalysisResult {

    private final Map<String, Integer> activitySummary;
    private final List<LogEntry> flaggedEntries;
    private final Map<String, Integer> suspiciousIps;
    private final List<LogEntry> unknownPatterns;

    public AnalysisResult(
            Map<String, Integer> activitySummary,
            List<LogEntry> flaggedEntries,
            Map<String, Integer> suspiciousIps,
            List<LogEntry> unknownPatterns) {

        this.activitySummary = activitySummary;
        this.flaggedEntries = flaggedEntries;
        this.suspiciousIps = suspiciousIps;
        this.unknownPatterns = unknownPatterns;
    }

    public Map<String, Integer> getActivitySummary() {
        return activitySummary;
    }

    public List<LogEntry> getFlaggedEntries() {
        return flaggedEntries;
    }

    public Map<String, Integer> getSuspiciousIps() {
        return suspiciousIps;
    }

    public List<LogEntry> getUnknownPatterns() {
        return unknownPatterns;
    }
}