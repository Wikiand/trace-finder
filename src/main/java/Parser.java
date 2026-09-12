import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Parser {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParseResult parse(String line, int lineNumber)
            throws MalformedLineException {

        if (line == null || line.trim().isEmpty()) {
            throw new MalformedLineException(
                    lineNumber,
                    line
            );
        }

        String[] fields = line.split("\\|", -1);

        if (fields.length != 5) {
            throw new MalformedLineException(
                    lineNumber,
                    line
            );
        }

        try {
            LocalDateTime timestamp =
                    LocalDateTime.parse(
                            fields[0].trim(),
                            TIMESTAMP_FORMAT
                    );

            LogEntry entry = new LogEntry(
                    lineNumber,
                    line,
                    timestamp,
                    fields[1].trim(),
                    fields[2].trim(),
                    fields[3].trim(),
                    fields[4].trim()
            );

            return ParseResult.success(entry);

        } catch (DateTimeParseException e) {
            throw new MalformedLineException(
                    lineNumber,
                    line
            );
        }
    }
}