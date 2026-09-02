import java.util.LinkedHashMap;
import java.util.Map;

public class Rulebook {

    private final Map<String, Integer> rules = new LinkedHashMap<>();

    public void addRule(String level, int severityScore) {
        rules.put(level, severityScore);
    }

    public boolean containsLevel(String level) {
        return rules.containsKey(level);
    }

    public int getSeverityScore(String level) {
        return rules.get(level);
    }

    public Map<String, Integer> getRules() {
        return rules;
    }
}