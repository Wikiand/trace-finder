import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LogReaderTest {

    @Test
    void missingLogFilePreservesOriginalCause() {

        Path missingFile =
                Path.of(
                        "does-not-exist-"
                                + System.nanoTime()
                                + ".log"
                );

        LogReader logReader =
                new LogReader(new Parser());

        TraceFinderFileException exception =
                assertThrows(
                        TraceFinderFileException.class,
                        () -> logReader.read(missingFile)
                );

        assertNotNull(exception.getCause());

        assertInstanceOf(
                IOException.class,
                exception.getCause()
        );

        assertInstanceOf(
                NoSuchFileException.class,
                exception.getCause()
        );
    }

    @Test
    void logFileExactlyAtLimitIsAccepted()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "large-log",
                        ".log"
                );

        byte[] content =
                new byte[
                        (int) InputLimits.MAX_LOG_FILE_BYTES
                ];

        Files.write(file, content);

        LogReader reader =
                new LogReader(new Parser());

        assertDoesNotThrow(
                () -> reader.read(file)
        );

        Files.deleteIfExists(file);
    }

    @Test
    void logFileAboveLimitIsRejected()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "oversized-log",
                        ".log"
                );

        byte[] content =
                new byte[
                        (int) InputLimits.MAX_LOG_FILE_BYTES + 1
                ];

        Files.write(file, content);

        LogReader reader =
                new LogReader(new Parser());

        TraceFinderFileException exception =
                assertThrows(
                        TraceFinderFileException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        String.valueOf(
                                InputLimits.MAX_LOG_FILE_BYTES
                        )
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        String.valueOf(
                                InputLimits.MAX_LOG_FILE_BYTES + 1
                        )
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void directoryUsedAsLogPathIsRejected()
            throws Exception {

        Path directory =
                Files.createTempDirectory(
                        "log-directory"
                );

        LogReader reader =
                new LogReader(new Parser());

        TraceFinderFileException exception =
                assertThrows(
                        TraceFinderFileException.class,
                        () -> reader.read(directory)
                );

        assertTrue(
                exception.getMessage().contains(
                        "not a regular file"
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        directory.toString()
                )
        );

        Files.deleteIfExists(directory);
    }

    @Test
    void lineExactlyAtLimitIsNotMarkedMalformed()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "line-at-limit",
                        ".log"
                );

        String prefix =
                "2026-09-15 12:00:00|INFO|127.0.0.1|server|";

        String action =
                "A".repeat(
                        InputLimits.MAX_LOG_LINE_CHARS
                                - prefix.length()
                );

        String line =
                prefix + action;

        assertEquals(
                InputLimits.MAX_LOG_LINE_CHARS,
                line.length()
        );

        Files.writeString(
                file,
                line + "\n",
                StandardCharsets.UTF_8
        );

        LogReader reader =
                new LogReader(new Parser());

        LogReadResult result =
                reader.read(file);

        assertEquals(
                1,
                result.getEntries().size()
        );

        assertTrue(
                result.getMalformedLines().isEmpty()
        );

        Files.deleteIfExists(file);
    }

    @Test
    void lineAboveLimitIsRecordedAsMalformed()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "overlong-line",
                        ".log"
                );

        String prefix =
                "2026-09-15 12:00:00|INFO|127.0.0.1|server|";

        String action =
                "A".repeat(
                        InputLimits.MAX_LOG_LINE_CHARS
                                - prefix.length()
                                + 1
                );

        String line =
                prefix + action;

        assertEquals(
                InputLimits.MAX_LOG_LINE_CHARS + 1,
                line.length()
        );

        Files.writeString(
                file,
                line + "\n",
                StandardCharsets.UTF_8
        );

        LogReader reader =
                new LogReader(new Parser());

        LogReadResult result =
                reader.read(file);

        assertTrue(
                result.getEntries().isEmpty()
        );

        assertEquals(
                1,
                result.getMalformedLines().size()
        );

        MalformedLine malformedLine =
                result.getMalformedLines().get(0);

        assertEquals(
                1,
                malformedLine.getLineNumber()
        );

        assertTrue(
                malformedLine.getRawContent().contains(
                        "exceeded maximum length"
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void multipleOverlongLinesAreAllRecorded()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "multiple-overlong-lines",
                        ".log"
                );

        String oversizedLine =
                "A".repeat(
                        InputLimits.MAX_LOG_LINE_CHARS + 1
                );

        Files.writeString(
                file,
                oversizedLine + "\n"
                        + oversizedLine + "\n",
                StandardCharsets.UTF_8
        );

        LogReader reader =
                new LogReader(new Parser());

        LogReadResult result =
                reader.read(file);

        assertTrue(
                result.getEntries().isEmpty()
        );

        assertEquals(
                2,
                result.getMalformedLines().size()
        );

        assertEquals(
                1,
                result.getMalformedLines()
                        .get(0)
                        .getLineNumber()
        );

        assertEquals(
                2,
                result.getMalformedLines()
                        .get(1)
                        .getLineNumber()
        );

        Files.deleteIfExists(file);
    }

    @Test
    void processingContinuesAfterOverlongLine()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "overlong-then-valid",
                        ".log"
                );

        String oversizedLine =
                "A".repeat(
                        InputLimits.MAX_LOG_LINE_CHARS + 1
                );

        String validLine =
                "2026-09-15 12:01:00"
                        + "|INFO"
                        + "|127.0.0.1"
                        + "|server"
                        + "|login";

        Files.writeString(
                file,
                oversizedLine + "\n"
                        + validLine + "\n",
                StandardCharsets.UTF_8
        );

        LogReader reader =
                new LogReader(new Parser());

        LogReadResult result =
                reader.read(file);

        assertEquals(
                1,
                result.getEntries().size()
        );

        assertEquals(
                validLine,
                result.getEntries()
                        .get(0)
                        .getRawContent()
        );

        assertEquals(
                2,
                result.getEntries()
                        .get(0)
                        .getLineNumber()
        );

        assertEquals(
                1,
                result.getMalformedLines().size()
        );

        assertEquals(
                1,
                result.getMalformedLines()
                        .get(0)
                        .getLineNumber()
        );

        assertTrue(
                result.getMalformedLines()
                        .get(0)
                        .getRawContent()
                        .contains(
                                "exceeded maximum length"
                        )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void multipleDelimitersAreRecordedAsMalformed()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "multiple-delimiters",
                        ".log"
                );

        String malformedLine =
                "2026-09-15 12:00:00"
                        + "|INFO"
                        + "|127.0.0.1"
                        + "|server"
                        + "|login"
                        + "|unexpected";

        String validLine =
                "2026-09-15 12:01:00"
                        + "|INFO"
                        + "|127.0.0.2"
                        + "|server"
                        + "|logout";

        Files.writeString(
                file,
                malformedLine + "\n"
                        + validLine + "\n",
                StandardCharsets.UTF_8
        );

        LogReader reader =
                new LogReader(new Parser());

        LogReadResult result =
                reader.read(file);

        assertEquals(
                1,
                result.getEntries().size()
        );

        assertEquals(
                validLine,
                result.getEntries()
                        .get(0)
                        .getRawContent()
        );

        assertEquals(
                2,
                result.getEntries()
                        .get(0)
                        .getLineNumber()
        );

        assertEquals(
                1,
                result.getMalformedLines().size()
        );

        MalformedLine malformed =
                result.getMalformedLines().get(0);

        assertEquals(
                1,
                malformed.getLineNumber()
        );

        assertEquals(
                malformedLine,
                malformed.getRawContent()
        );

        Files.deleteIfExists(file);
    }

    @Test
    void invalidUtf8IsRejected()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "invalid-utf8",
                        ".log"
                );

        byte[] validPart =
                "2026-09-15 12:00:00|INFO|127.0.0.1|server|test\n"
                        .getBytes(StandardCharsets.UTF_8);

        byte[] invalidPart =
                new byte[]{
                        (byte) 0xC3,
                        (byte) 0x28
                };

        byte[] content =
                new byte[
                        validPart.length
                                + invalidPart.length
                ];

        System.arraycopy(
                validPart,
                0,
                content,
                0,
                validPart.length
        );

        System.arraycopy(
                invalidPart,
                0,
                content,
                validPart.length,
                invalidPart.length
        );

        Files.write(file, content);

        LogReader reader =
                new LogReader(new Parser());

        TraceFinderFileException exception =
                assertThrows(
                        TraceFinderFileException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        "invalid UTF-8"
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void invalidUtf8AfterOverlongLineIsStillRejected()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "invalid-utf8-overlong",
                        ".log"
                );

        String prefix =
                "2026-09-15 12:00:00|INFO|127.0.0.1|server|";

        byte[] prefixBytes =
                prefix.getBytes(StandardCharsets.UTF_8);

        byte[] longContent =
                "A".repeat(
                        InputLimits.MAX_LOG_LINE_CHARS + 100
                ).getBytes(StandardCharsets.UTF_8);

        byte[] invalidBytes =
                new byte[]{
                        (byte) 0xC3,
                        (byte) 0x28
                };

        byte[] newline =
                "\n".getBytes(StandardCharsets.UTF_8);

        byte[] content =
                new byte[
                        prefixBytes.length
                                + longContent.length
                                + invalidBytes.length
                                + newline.length
                ];

        int position = 0;

        System.arraycopy(
                prefixBytes,
                0,
                content,
                position,
                prefixBytes.length
        );

        position += prefixBytes.length;

        System.arraycopy(
                longContent,
                0,
                content,
                position,
                longContent.length
        );

        position += longContent.length;

        System.arraycopy(
                invalidBytes,
                0,
                content,
                position,
                invalidBytes.length
        );

        position += invalidBytes.length;

        System.arraycopy(
                newline,
                0,
                content,
                position,
                newline.length
        );

        Files.write(file, content);

        LogReader reader =
                new LogReader(new Parser());

        TraceFinderFileException exception =
                assertThrows(
                        TraceFinderFileException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        "invalid UTF-8"
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void validUtf8CharactersAreAccepted()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "valid-utf8",
                        ".log"
                );

        String line =
                "2026-09-15 12:00:00|INFO|127.0.0.1|server|"
                        + "café € ";

        Files.writeString(
                file,
                line + "\n",
                StandardCharsets.UTF_8
        );

        LogReader reader =
                new LogReader(new Parser());

        LogReadResult result =
                reader.read(file);

        assertEquals(
                1,
                result.getEntries().size()
        );

        assertTrue(
                result.getMalformedLines().isEmpty()
        );

        assertEquals(
                line,
                result.getEntries()
                        .get(0)
                        .getRawContent()
        );

        Files.deleteIfExists(file);
    }
}