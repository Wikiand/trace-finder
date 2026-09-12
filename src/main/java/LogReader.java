import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LogReader {

    private final Parser parser;

    public LogReader(Parser parser) {
        this.parser = parser;
    }

    public LogReadResult read(Path path) throws TraceFinderFileException {

        List<LogEntry> entries = new ArrayList<>();
        List<MalformedLine> malformedLines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                ParseResult result = parser.parse(line, lineNumber);

                if (result.isValid()) {
                    entries.add(result.getEntry());
                } else {
                    malformedLines.add(result.getMalformedLine());
                }
            }

        } catch (IOException e) {
            throw new TraceFinderFileException(
                    "Could not read log file '" + path + "': " + e.getMessage(),
                    e
            );
        }

        return new LogReadResult(entries, malformedLines);
    }
}