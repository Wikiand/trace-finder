import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RulebookReader {

    public Rulebook read(Path path)
            throws TraceFinderFileException, RulebookException {

        if (Files.exists(path) && !Files.isRegularFile(path)) {
            throw new TraceFinderFileException(
                    "Rulebook path '" + path
                            + "' is not a regular file",
                    null
            );
        }

        long fileSize;

        try {
            fileSize = Files.size(path);
        } catch (IOException e) {
            throw new TraceFinderFileException(
                    "Could not access rulebook file '"
                            + path
                            + "': "
                            + e.getMessage(),
                    e
            );
        }

        if (fileSize > InputLimits.MAX_RULEBOOK_FILE_BYTES) {
            throw new TraceFinderFileException(
                    "Rulebook file '" + path
                            + "' exceeds the limit of "
                            + InputLimits.MAX_RULEBOOK_FILE_BYTES
                            + " bytes; actual size is "
                            + fileSize
                            + " bytes",
                    null
            );
        }

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

            String header = reader.readLine();

            if (header == null
                    || !header.equals("level,severity_score")) {

                throw new RulebookException(
                        "Invalid rulebook '" + path
                                + "': expected header level,severity_score"
                );
            }

            Rulebook rulebook = new Rulebook();

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields =
                        line.split(",", -1);

                if (fields.length != 2) {
                    throw new RulebookException(
                            "Invalid rulebook '" + path
                                    + "' at line "
                                    + lineNumber
                                    + ": expected 2 columns"
                    );
                }

                String level =
                        InputCleaner.clean(fields[0]);

                if (level == null || level.isEmpty()) {
                    throw new RulebookException(
                            "Invalid rulebook '" + path
                                    + "' at line "
                                    + lineNumber
                                    + ": level must not be empty"
                    );
                }

                if (rulebook.containsLevel(level)) {
                    throw new RulebookException(
                            "Invalid rulebook '" + path
                                    + "' at line "
                                    + lineNumber
                                    + ": duplicate level '"
                                    + level
                                    + "'"
                    );
                }

                String scoreText =
                        InputCleaner.clean(fields[1]);

                int severityScore;

                try {
                    severityScore =
                            Integer.parseInt(scoreText);

                } catch (NumberFormatException e) {

                    throw new RulebookException(
                            "Invalid rulebook '" + path
                                    + "' at line "
                                    + lineNumber
                                    + ": severity score must be a non-negative whole number",
                            e
                    );
                }

                if (severityScore < 0) {
                    throw new RulebookException(
                            "Invalid rulebook '" + path
                                    + "' at line "
                                    + lineNumber
                                    + ": severity score must be a non-negative whole number"
                    );
                }

                rulebook.addRule(
                        level,
                        severityScore
                );
            }

            return rulebook;

        } catch (CharacterCodingException e) {

            throw new TraceFinderFileException(
                    "Rulebook file '" + path
                            + "' contains invalid UTF-8",
                    e
            );

        } catch (IOException e) {

            throw new TraceFinderFileException(
                    "Could not read rulebook file '"
                            + path
                            + "': "
                            + e.getMessage(),
                    e
            );
        }
    }
}