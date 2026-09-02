import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void mainRejectsWrongNumberOfArguments() {
        int exitCode = Main.run(new String[]{"logs.txt"});

        assertEquals(1, exitCode);
    }

    @Test
    void mainRejectsTooManyArguments() {
        int exitCode = Main.run(
                new String[]{"logs.txt", "rules.csv", "report.txt", "extra.txt"}
        );

        assertEquals(1, exitCode);
    }
}