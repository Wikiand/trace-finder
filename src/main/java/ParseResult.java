public class ParseResult {

    private final LogEntry entry;
    private final MalformedLine malformedLine;

    private ParseResult(LogEntry entry, MalformedLine malformedLine) {
        this.entry = entry;
        this.malformedLine = malformedLine;
    }

    public static ParseResult success(LogEntry entry) {
        return new ParseResult(entry, null);
    }

    public static ParseResult malformed(MalformedLine malformedLine) {
        return new ParseResult(null, malformedLine);
    }

    public boolean isValid() {
        return entry != null;
    }

    public LogEntry getEntry() {
        return entry;
    }

    public MalformedLine getMalformedLine() {
        return malformedLine;
    }
}