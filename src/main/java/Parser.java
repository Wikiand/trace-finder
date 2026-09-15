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

        String timestampField =
                InputCleaner.clean(fields[0]);

        String level =
                InputCleaner.clean(fields[1]);

        String sourceIp =
                InputCleaner.clean(fields[2]);

        String target =
                InputCleaner.clean(fields[3]);

        String action =
                InputCleaner.clean(fields[4]);

        try {
            LocalDateTime timestamp =
                    LocalDateTime.parse(
                            timestampField,
                            TIMESTAMP_FORMAT
                    );

            LogEntry entry = new LogEntry(
                    lineNumber,
                    line,
                    timestamp,
                    level,
                    sourceIp,
                    target,
                    action
            );

            return ParseResult.success(entry);

        } catch (DateTimeParseException e) {
            throw new MalformedLineException(
                    lineNumber,
                    line,
                    e
            );
        }
    }
}