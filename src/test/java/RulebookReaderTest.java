import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RulebookReaderTest {

    @Test
    void validRulebookIsReadCorrectly() throws Exception {

        Path file =
                Files.createTempFile("rules", ".csv");

        Files.writeString(
                file,
                "level,severity_score\n"
                        + "INFO,1\n"
                        + "WARN,3\n"
                        + "ERROR,5\n"
                        + "ALERT,9\n"
        );

        RulebookReader reader =
                new RulebookReader();

        Rulebook rulebook =
                reader.read(file);

        assertTrue(
                rulebook.containsLevel("INFO")
        );

        assertEquals(
                1,
                rulebook.getSeverityScore("INFO")
        );

        assertTrue(
                rulebook.containsLevel("WARN")
        );

        assertEquals(
                3,
                rulebook.getSeverityScore("WARN")
        );

        assertTrue(
                rulebook.containsLevel("ERROR")
        );

        assertEquals(
                5,
                rulebook.getSeverityScore("ERROR")
        );

        assertTrue(
                rulebook.containsLevel("ALERT")
        );

        assertEquals(
                9,
                rulebook.getSeverityScore("ALERT")
        );

        Files.deleteIfExists(file);
    }

    @Test
    void invalidHeaderIsRejected() throws Exception {

        Path file =
                Files.createTempFile("rules", ".csv");

        Files.writeString(
                file,
                "wrong_header\n"
                        + "INFO,1\n"
        );

        RulebookReader reader =
                new RulebookReader();

        assertThrows(
                Exception.class,
                () -> reader.read(file)
        );

        Files.deleteIfExists(file);
    }

    @Test
    void rulebookExactlyAtLimitIsAccepted() throws Exception {

        Path file =
                Files.createTempFile(
                        "rules-at-limit",
                        ".csv"
                );

        String base =
                "level,severity_score\n"
                        + "INFO,1\n";

        int baseSize =
                base.getBytes(StandardCharsets.UTF_8).length;

        int remaining =
                (int) InputLimits.MAX_RULEBOOK_FILE_BYTES
                        - baseSize;

        StringBuilder content =
                new StringBuilder(base);

        content.append(
                "\n".repeat(remaining)
        );

        Files.writeString(
                file,
                content.toString()
        );

        assertEquals(
                InputLimits.MAX_RULEBOOK_FILE_BYTES,
                Files.size(file)
        );

        RulebookReader reader =
                new RulebookReader();

        assertDoesNotThrow(
                () -> reader.read(file)
        );

        Files.deleteIfExists(file);
    }

    @Test
    void rulebookAboveLimitIsRejected() throws Exception {

        Path file =
                Files.createTempFile(
                        "oversized-rules",
                        ".csv"
                );

        String base =
                "level,severity_score\n"
                        + "INFO,1\n";

        int baseSize =
                base.getBytes(StandardCharsets.UTF_8).length;

        int remaining =
                (int) InputLimits.MAX_RULEBOOK_FILE_BYTES
                        - baseSize;

        StringBuilder content =
                new StringBuilder(base);

        content.append(
                "\n".repeat(remaining + 1)
        );

        Files.writeString(
                file,
                content.toString()
        );

        assertEquals(
                InputLimits.MAX_RULEBOOK_FILE_BYTES + 1,
                Files.size(file)
        );

        RulebookReader reader =
                new RulebookReader();

        TraceFinderFileException exception =
                assertThrows(
                        TraceFinderFileException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        String.valueOf(
                                InputLimits.MAX_RULEBOOK_FILE_BYTES
                        )
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        String.valueOf(
                                InputLimits.MAX_RULEBOOK_FILE_BYTES + 1
                        )
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void directoryUsedAsRulebookPathIsRejected()
            throws Exception {

        Path directory =
                Files.createTempDirectory(
                        "rulebook-directory"
                );

        RulebookReader reader =
                new RulebookReader();

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
    void rulebookLevelsAreCleaned() throws Exception {

        Path file =
                Files.createTempFile(
                        "rules-cleaning",
                        ".csv"
                );

        Files.writeString(
                file,
                "level,severity_score\n"
                        + " \u200BI\u200BNFO\u200B ,1\n"
        );

        RulebookReader reader =
                new RulebookReader();

        Rulebook rulebook =
                reader.read(file);

        assertTrue(
                rulebook.containsLevel("INFO")
        );

        assertEquals(
                1,
                rulebook.getSeverityScore("INFO")
        );

        assertFalse(
                rulebook.containsLevel(
                        " \u200BI\u200BNFO\u200B "
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void duplicateLevelsAfterCleaningAreRejected()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "duplicate-cleaned-levels",
                        ".csv"
                );

        Files.writeString(
                file,
                "level,severity_score\n"
                        + "INFO,1\n"
                        + " \u200BI\u200BNFO\u200B ,3\n"
        );

        RulebookReader reader =
                new RulebookReader();

        RulebookException exception =
                assertThrows(
                        RulebookException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        "duplicate level 'INFO'"
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void negativeSeverityScoreIsRejected()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "negative-score",
                        ".csv"
                );

        Files.writeString(
                file,
                "level,severity_score\n"
                        + "INFO,-1\n"
        );

        RulebookReader reader =
                new RulebookReader();

        RulebookException exception =
                assertThrows(
                        RulebookException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        "non-negative whole number"
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void decimalSeverityScoreIsRejected()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "decimal-score",
                        ".csv"
                );

        Files.writeString(
                file,
                "level,severity_score\n"
                        + "INFO,2.5\n"
        );

        RulebookReader reader =
                new RulebookReader();

        RulebookException exception =
                assertThrows(
                        RulebookException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        "non-negative whole number"
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void emptyLevelIsRejected() throws Exception {

        Path file =
                Files.createTempFile(
                        "empty-level",
                        ".csv"
                );

        Files.writeString(
                file,
                "level,severity_score\n"
                        + ",3\n"
        );

        RulebookReader reader =
                new RulebookReader();

        RulebookException exception =
                assertThrows(
                        RulebookException.class,
                        () -> reader.read(file)
                );

        assertTrue(
                exception.getMessage().contains(
                        "level must not be empty"
                )
        );

        Files.deleteIfExists(file);
    }

    @Test
    void invalidUtf8RulebookIsRejected()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "invalid-utf8-rulebook",
                        ".csv"
                );

        try {
            byte[] validContent =
                    "level,severity_score\n"
                            .getBytes(StandardCharsets.UTF_8);

            byte[] invalidUtf8 =
                    new byte[]{
                            (byte) 0xC3,
                            (byte) 0x28
                    };

            byte[] content =
                    new byte[
                            validContent.length
                                    + invalidUtf8.length
                    ];

            System.arraycopy(
                    validContent,
                    0,
                    content,
                    0,
                    validContent.length
            );

            System.arraycopy(
                    invalidUtf8,
                    0,
                    content,
                    validContent.length,
                    invalidUtf8.length
            );

            Files.write(file, content);

            RulebookReader reader =
                    new RulebookReader();

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

        } finally {
            Files.deleteIfExists(file);
        }
    }
}