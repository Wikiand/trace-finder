
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    void validLineIsParsedIntoLogEntry() {
        Parser parser = new Parser();

        String line = "2024-03-15 02:14:33 | WARN | 203.0.113.42 | /login | failed";

        ParseResult result = parser.parse(line, 1);

        assertTrue(result.isValid());

        LogEntry entry = result.getEntry();

        assertNotNull(entry);
        assertEquals("WARN", entry.getLevel());
        assertEquals("203.0.113.42", entry.getSourceIp());
        assertEquals("/login", entry.getTarget());
        assertEquals("failed", entry.getAction());
    }

    @Test
    void malformedLineIsRecorded() {
        Parser parser = new Parser();

        String line = "2024-03-15 02:14:33 | WARN | 203.0.113.42";

        ParseResult result = parser.parse(line, 5);

        assertFalse(result.isValid());

        MalformedLine malformed = result.getMalformedLine();

        assertNotNull(malformed);
        assertEquals(5, malformed.getLineNumber());
        assertEquals(line, malformed.getRawContent());
    }

    @Test
    void emptyLineIsMalformed() {
        Parser parser = new Parser();

        String line = "";

        ParseResult result = parser.parse(line, 3);

        assertFalse(result.isValid());

        MalformedLine malformed = result.getMalformedLine();

        assertNotNull(malformed);
        assertEquals(3, malformed.getLineNumber());
        assertEquals(line, malformed.getRawContent());
    }

    @Test
    void invalidTimestampIsMalformed() {
        Parser parser = new Parser();

        String line = "not-a-date | WARN | 203.0.113.42 | /login | failed";

        ParseResult result = parser.parse(line, 7);

        assertFalse(result.isValid());

        MalformedLine malformed = result.getMalformedLine();

        assertNotNull(malformed);
        assertEquals(7, malformed.getLineNumber());
        assertEquals(line, malformed.getRawContent());
    }
}

