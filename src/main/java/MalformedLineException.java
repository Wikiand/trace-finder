public class MalformedLineException extends Exception {

    private final int lineNumber;
    private final String rawContent;

    public MalformedLineException(int lineNumber, String rawContent) {
        super("Malformed log line at line " + lineNumber);
        this.lineNumber = lineNumber;
        this.rawContent = rawContent;
    }

    public MalformedLineException(
            int lineNumber,
            String rawContent,
            Throwable cause) {

        super(
                "Malformed log line at line " + lineNumber,
                cause
        );

        this.lineNumber = lineNumber;
        this.rawContent = rawContent;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getRawContent() {
        return rawContent;
    }
}
