public class Rule {

    private final String level;
    private final int severityScore;

    public Rule(String level, int severityScore) {
        this.level = level;
        this.severityScore = severityScore;
    }

    public String getLevel() {
        return level;
    }

    public int getSeverityScore() {
        return severityScore;
    }
}