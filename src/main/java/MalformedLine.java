public class MalformedLine {

    private final int lineNumber;
    private final String rawContent;

    public MalformedLine(int lineNumber, String rawContent) {
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