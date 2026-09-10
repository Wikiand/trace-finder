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

    @Test
    void mainRejectsUnreadableTimestamp() {
        int exitCode = Main.run(
                new String[]{
                        "logs.txt",
                        "rules.csv",
                        "report.txt",
                        "not-a-timestamp",
                        "2024-03-15 03:00:00"
                }
        );

        assertEquals(1, exitCode);
    }

    @Test
    void mainRejectsReversedTimeWindow() {
        int exitCode = Main.run(
                new String[]{
                        "logs.txt",
                        "rules.csv",
                        "report.txt",
                        "2024-03-15 04:00:00",
                        "2024-03-15 03:00:00"
                }
        );

        assertEquals(1, exitCode);
    }

}