import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    void validLineIsParsedIntoLogEntry() throws MalformedLineException {
        Parser parser = new Parser();

        String line =
                "2024-03-15 02:14:33 | WARN | 203.0.113.42 | /login | failed";

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
    void malformedLineThrowsExceptionWithLineNumberAndContent() {
        Parser parser = new Parser();

        String line =
                "2024-03-15 02:14:33 | WARN | 203.0.113.42";

        MalformedLineException exception =
                assertThrows(
                        MalformedLineException.class,
                        () -> parser.parse(line, 5)
                );

        assertEquals(5, exception.getLineNumber());
        assertEquals(line, exception.getRawContent());
    }

    @Test
    void emptyLineThrowsExceptionWithLineNumberAndContent() {
        Parser parser = new Parser();

        String line = "";

        MalformedLineException exception =
                assertThrows(
                        MalformedLineException.class,
                        () -> parser.parse(line, 3)
                );

        assertEquals(3, exception.getLineNumber());
        assertEquals(line, exception.getRawContent());
    }

    @Test
    void invalidTimestampThrowsExceptionWithLineNumberAndContent() {
        Parser parser = new Parser();

        String line =
                "not-a-date | WARN | 203.0.113.42 | /login | failed";

        MalformedLineException exception =
                assertThrows(
                        MalformedLineException.class,
                        () -> parser.parse(line, 7)
                );

        assertEquals(7, exception.getLineNumber());
        assertEquals(line, exception.getRawContent());
    }
}