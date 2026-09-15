import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LogReader {

    private final Parser parser;

    public LogReader(Parser parser) {
        this.parser = parser;
    }

    public LogReadResult read(Path path)
            throws TraceFinderFileException {

        if (Files.exists(path) && !Files.isRegularFile(path)) {
            throw new TraceFinderFileException(
                    "Log path '" + path
                            + "' is not a regular file",
                    null
            );
        }

        long fileSize;

        try {
            fileSize = Files.size(path);
        } catch (IOException e) {
            throw new TraceFinderFileException(
                    "Could not access log file '" + path
                            + "': " + e.getMessage(),
                    e
            );
        }

        if (fileSize > InputLimits.MAX_LOG_FILE_BYTES) {
            throw new TraceFinderFileException(
                    "Log file '" + path
                            + "' exceeds the limit of "
                            + InputLimits.MAX_LOG_FILE_BYTES
                            + " bytes; actual size is "
                            + fileSize
                            + " bytes",
                    null
            );
        }

        List<LogEntry> entries = new ArrayList<>();
        List<MalformedLine> malformedLines = new ArrayList<>();

        try (
                InputStreamReader inputStreamReader =
                        new InputStreamReader(
                                Files.newInputStream(path),
                                StandardCharsets.UTF_8
                                        .newDecoder()
                                        .onMalformedInput(
                                                CodingErrorAction.REPORT
                                        )
                                        .onUnmappableCharacter(
                                                CodingErrorAction.REPORT
                                        )
                        );

                BufferedReader reader =
                        new BufferedReader(inputStreamReader)
        ) {

            LimitedLineReader lineReader =
                    new LimitedLineReader(
                            reader,
                            InputLimits.MAX_LOG_LINE_CHARS
                    );

            int lineNumber = 0;

            LimitedLineReader.LineReadResult result;

            while ((result = lineReader.readLine()) != null) {

                lineNumber++;

                if (result.exceededLimit()) {

                    malformedLines.add(
                            new MalformedLine(
                                    lineNumber,
                                    "[line exceeded maximum length of "
                                            + InputLimits.MAX_LOG_LINE_CHARS
                                            + " characters]"
                            )
                    );

                    continue;
                }

                try {

                    ParseResult parseResult =
                            parser.parse(
                                    result.getContent(),
                                    lineNumber
                            );

                    if (parseResult.isValid()) {
                        entries.add(parseResult.getEntry());
                    }

                } catch (MalformedLineException e) {

                    malformedLines.add(
                            new MalformedLine(
                                    e.getLineNumber(),
                                    e.getRawContent()
                            )
                    );
                }
            }

        } catch (CharacterCodingException e) {

            throw new TraceFinderFileException(
                    "Log file '" + path
                            + "' contains invalid UTF-8",
                    e
            );

        } catch (IOException e) {

            throw new TraceFinderFileException(
                    "Could not read log file '" + path
                            + "': " + e.getMessage(),
                    e
            );
        }

        return new LogReadResult(
                entries,
                malformedLines
        );
    }
}