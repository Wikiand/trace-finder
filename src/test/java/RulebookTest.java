
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RulebookTest {

    @Test
    void ruleCanBeAddedAndFound() {
        Rulebook rulebook = new Rulebook();

        rulebook.addRule("WARN", 3);

        assertTrue(rulebook.containsLevel("WARN"));
        assertEquals(3, rulebook.getSeverityScore("WARN"));
    }

    @Test
    void unknownLevelIsNotInRulebook() {
        Rulebook rulebook = new Rulebook();

        rulebook.addRule("INFO", 1);
        rulebook.addRule("WARN", 3);
        rulebook.addRule("ERROR", 5);
        rulebook.addRule("ALERT", 9);

        assertFalse(rulebook.containsLevel("DEBUG"));
    }
}

