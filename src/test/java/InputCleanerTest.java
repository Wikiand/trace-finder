
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InputCleanerTest {

    @Test
    void removesLeadingAndTrailingWhitespace() {

        assertEquals(
                "INFO",
                InputCleaner.clean("  INFO  ")
        );
    }

    @Test
    void removesZeroWidthSpaceInsideValue() {

        assertEquals(
                "INFO",
                InputCleaner.clean("I\u200BNFO")
        );
    }

    @Test
    void removesMultipleHiddenCharacters() {

        assertEquals(
                "ERROR",
                InputCleaner.clean(
                        "\u200B E\u200BR\u200BR\u200BO\u200BR\u200B "
                )
        );
    }

    @Test
    void nullRemainsNull() {

        assertNull(
                InputCleaner.clean(null)
        );
    }

    @Test
    void ordinaryCharactersArePreserved() {

        assertEquals(
                "User login failed",
                InputCleaner.clean(
                        " User login failed "
                )
        );
    }
}

