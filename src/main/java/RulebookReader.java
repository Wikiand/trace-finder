import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RulebookReader {

    public Rulebook read(Path path)
            throws TraceFinderFileException, RulebookException {

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String header = reader.readLine();

            if (header == null || !header.equals("level,severity_score")) {
                throw new RulebookException(
                        "Invalid rulebook '" + path + "': expected header level,severity_score"
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

                String[] fields = line.split(",", -1);

                if (fields.length != 2) {
                    throw new RulebookException(
                            "Invalid rulebook '" + path
                                    + "' at line " + lineNumber
                                    + ": expected 2 columns"
                    );
                }

                String level = fields[0].trim();
                int severityScore;

                try {
                    severityScore = Integer.parseInt(fields[1].trim());
                } catch (NumberFormatException e) {
                    throw new RulebookException(
                            "Invalid rulebook '" + path
                                    + "' at line " + lineNumber
                                    + ": severity score must be an integer",
                            e
                    );
                }

                rulebook.addRule(level, severityScore);
            }

            return rulebook;

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