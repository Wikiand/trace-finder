import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LogReaderTest {

    @Test
    void missingLogFilePreservesOriginalCause() {

        Path missingFile =
                Path.of("does-not-exist-" + System.nanoTime() + ".log");

        LogReader logReader = new LogReader(new Parser());

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
}