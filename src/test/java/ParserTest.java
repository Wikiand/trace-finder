import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private final Parser parser = new Parser();

    @Test
    void parsesValidLogLine() throws Exception {

        String line =
                "2024-03-15 02:00:00"
                        + "|INFO"
                        + "|192.168.1.10"
                        + "|/home"
                        + "|view";

        ParseResult result =
                parser.parse(line, 1);

        assertTrue(result.isValid());

        LogEntry entry = result.getEntry();

        assertEquals(
                1,
                entry.getLineNumber()
        );

        assertEquals(
                LocalDateTime.of(2024, 3, 15, 2, 0),
                entry.getTimestamp()
        );

        assertEquals(
                "INFO",
                entry.getLevel()
        );

        assertEquals(
                "192.168.1.10",
                entry.getSourceIp()
        );

        assertEquals(
                "/home",
                entry.getTarget()
        );

        assertEquals(
                "view",
                entry.getAction()
        );
    }

    @Test
    void preservesOriginalLine() throws Exception {

        String line =
                "2024-03-15 02:00:00"
                        + "|INFO"
                        + "|192.168.1.10"
                        + "|/home"
                        + "|view";

        ParseResult result =
                parser.parse(line, 7);

        assertTrue(result.isValid());

        LogEntry entry = result.getEntry();

        assertEquals(
                line,
                entry.getRawContent()
        );

        assertEquals(
                7,
                entry.getLineNumber()
        );
    }

    @Test
    void trimsWhitespaceFromFields() throws Exception {

        String line =
                " 2024-03-15 02:00:00 "
                        + "| INFO "
                        + "| 192.168.1.10 "
                        + "| /home "
                        + "| view ";

        ParseResult result =
                parser.parse(line, 1);

        assertTrue(result.isValid());

        LogEntry entry = result.getEntry();

        assertEquals(
                "INFO",
                entry.getLevel()
        );

        assertEquals(
                "192.168.1.10",
                entry.getSourceIp()
        );

        assertEquals(
                "/home",
                entry.getTarget()
        );

        assertEquals(
                "view",
                entry.getAction()
        );
    }

    @Test
    void hiddenCharactersAreRemovedFromFields()
            throws Exception {

        String line =
                "2024-03-15 02:00:00"
                        + "|I\u200BNFO"
                        + "| 192.168.1.10\u200B "
                        + "| /home "
                        + "| view";

        ParseResult result =
                parser.parse(line, 1);

        assertTrue(result.isValid());

        LogEntry entry = result.getEntry();

        assertEquals(
                "INFO",
                entry.getLevel()
        );

        assertEquals(
                "192.168.1.10",
                entry.getSourceIp()
        );

        assertEquals(
                "/home",
                entry.getTarget()
        );

        assertEquals(
                "view",
                entry.getAction()
        );
    }

    @Test
    void originalLineIsPreservedAfterCleaning()
            throws Exception {

        String line =
                "2024-03-15 02:00:00"
                        + "|I\u200BNFO"
                        + "| 192.168.1.10 "
                        + "| /home "
                        + "| view";

        ParseResult result =
                parser.parse(line, 1);

        assertTrue(result.isValid());

        LogEntry entry =
                result.getEntry();

        assertEquals(
                line,
                entry.getRawContent()
        );
    }

    @Test
    void rejectsNullLine() {

        assertThrows(
                MalformedLineException.class,
                () -> parser.parse(null, 1)
        );
    }

    @Test
    void rejectsEmptyLine() {

        assertThrows(
                MalformedLineException.class,
                () -> parser.parse("", 1)
        );
    }

    @Test
    void rejectsWhitespaceOnlyLine() {

        assertThrows(
                MalformedLineException.class,
                () -> parser.parse("   ", 1)
        );
    }

    @Test
    void rejectsLineWithTooFewFields() {

        String line =
                "2024-03-15 02:00:00"
                        + "|INFO"
                        + "|192.168.1.10";

        assertThrows(
                MalformedLineException.class,
                () -> parser.parse(line, 4)
        );
    }

    @Test
    void rejectsLineWithTooManyFields() {

        String line =
                "2024-03-15 02:00:00"
                        + "|INFO"
                        + "|192.168.1.10"
                        + "|/home"
                        + "|view"
                        + "|extra";

        assertThrows(
                MalformedLineException.class,
                () -> parser.parse(line, 4)
        );
    }

    @Test
    void rejectsEmbeddedPipeBecauseItCreatesExtraField() {

        String line =
                "2024-03-15 02:00:00"
                        + "|INFO"
                        + "|192.168.1.10"
                        + "|/home"
                        + "|view|unexpected";

        MalformedLineException exception =
                assertThrows(
                        MalformedLineException.class,
                        () -> parser.parse(line, 12)
                );

        assertEquals(
                12,
                exception.getLineNumber()
        );

        assertEquals(
                line,
                exception.getRawContent()
        );
    }

    @Test
    void rejectsInvalidTimestamp() {

        String line =
                "not-a-timestamp"
                        + "|INFO"
                        + "|192.168.1.10"
                        + "|/home"
                        + "|view";

        MalformedLineException exception =
                assertThrows(
                        MalformedLineException.class,
                        () -> parser.parse(line, 5)
                );

        assertEquals(
                5,
                exception.getLineNumber()
        );

        assertEquals(
                line,
                exception.getRawContent()
        );
    }

    @Test
    void rejectsMalformedTimestampDate() {

        String line =
                "2024-99-99 99:99:99"
                        + "|INFO"
                        + "|192.168.1.10"
                        + "|/home"
                        + "|view";

        assertThrows(
                MalformedLineException.class,
                () -> parser.parse(line, 2)
        );
    }

    @Test
    void malformedLinePreservesOriginalContent() {

        String line =
                "this is not a valid log line";

        MalformedLineException exception =
                assertThrows(
                        MalformedLineException.class,
                        () -> parser.parse(line, 25)
                );

        assertEquals(
                25,
                exception.getLineNumber()
        );

        assertEquals(
                line,
                exception.getRawContent()
        );
    }
}