import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LogReaderTest {

    @Test
    void logReaderSeparatesValidAndMalformedLines() throws Exception {
        Path file = Files.createTempFile("logs", ".txt");

        Files.writeString(file,
                "2024-03-15 02:14:33 | INFO | 192.168.1.10 | /home | view\n" +
                "this is malformed\n" +
                "2024-03-15 02:15:33 | WARN | 192.168.1.20 | /login | failed\n"
        );

        LogReader reader = new LogReader(new Parser());

        LogReadResult result = reader.read(file);

        assertEquals(2, result.getEntries().size());
        assertEquals(1, result.getMalformedLines().size());

        assertEquals(2, result.getMalformedLines().get(0).getLineNumber());
        assertEquals(
                "this is malformed",
                result.getMalformedLines().get(0).getRawContent()
        );

        Files.deleteIfExists(file);
    }
}