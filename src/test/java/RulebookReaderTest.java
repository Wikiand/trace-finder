
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RulebookReaderTest {

    @Test
    void validRulebookIsReadCorrectly() throws Exception {
        Path file = Files.createTempFile("rules", ".csv");

        Files.writeString(file,
                "level,severity_score\n" +
                "INFO,1\n" +
                "WARN,3\n" +
                "ERROR,5\n" +
                "ALERT,9\n"
        );

        RulebookReader reader = new RulebookReader();

        Rulebook rulebook = reader.read(file);

        assertTrue(rulebook.containsLevel("INFO"));
        assertEquals(1, rulebook.getSeverityScore("INFO"));

        assertTrue(rulebook.containsLevel("WARN"));
        assertEquals(3, rulebook.getSeverityScore("WARN"));

        assertTrue(rulebook.containsLevel("ERROR"));
        assertEquals(5, rulebook.getSeverityScore("ERROR"));

        assertTrue(rulebook.containsLevel("ALERT"));
        assertEquals(9, rulebook.getSeverityScore("ALERT"));

        Files.deleteIfExists(file);
    }

    @Test
    void invalidHeaderIsRejected() throws Exception {
        Path file = Files.createTempFile("rules", ".csv");

        Files.writeString(file,
                "wrong_header\n" +
                "INFO,1\n"
        );

        RulebookReader reader = new RulebookReader();

        assertThrows(
                Exception.class,
                () -> reader.read(file)
        );

        Files.deleteIfExists(file);
    }
}

