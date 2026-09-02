import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RulebookReader {

    public Rulebook read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String header = reader.readLine();

            if (header == null || !header.equals("level,severity_score")) {
                throw new IOException("Invalid rulebook header");
            }

            Rulebook rulebook = new Rulebook();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(",", -1);

                if (fields.length != 2) {
                    throw new IOException("Invalid rulebook row");
                }

                String level = fields[0].trim();
                int severityScore;

                try {
                    severityScore = Integer.parseInt(fields[1].trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid severity score");
                }

                rulebook.addRule(level, severityScore);
            }

            return rulebook;
        }
    }
}