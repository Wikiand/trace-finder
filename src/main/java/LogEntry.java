import java.time.LocalDateTime;

public class LogEntry {

    private final int lineNumber;
    private final String rawContent;
    private final LocalDateTime timestamp;
    private final String level;
    private final String sourceIp;
    private final String target;
    private final String action;

    public LogEntry(
            int lineNumber,
            String rawContent,
            LocalDateTime timestamp,
            String level,
            String sourceIp,
            String target,
            String action) {

        this.lineNumber = lineNumber;
        this.rawContent = rawContent;
        this.timestamp = timestamp;
        this.level = level;
        this.sourceIp = sourceIp;
        this.target = target;
        this.action = action;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getRawContent() {
        return rawContent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getTarget() {
        return target;
    }

    public String getAction() {
        return action;
    }
}