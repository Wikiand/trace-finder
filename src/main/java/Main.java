import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        int exitCode = run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int run(String[] args) {

        if (args.length != 3) {
            System.err.println(
                    "Usage: java Main logs.txt rules.csv report.txt"
            );
            return 1;
        }

        Path logPath = Path.of(args[0]);
        Path rulebookPath = Path.of(args[1]);
        Path reportPath = Path.of(args[2]);

        try {
            RulebookReader rulebookReader = new RulebookReader();
            Rulebook rulebook = rulebookReader.read(rulebookPath);

            LogReader logReader = new LogReader(new Parser());
            LogReadResult logResult = logReader.read(logPath);

            Analyzer analyzer = new Analyzer();
            AnalysisResult analysisResult =
                    analyzer.analyze(logResult.getEntries(), rulebook);

            ReportWriter reportWriter = new ReportWriter();

            reportWriter.write(
                    reportPath,
                    analysisResult,
                    rulebook,
                    logResult.getMalformedLines()
            );

            return 0;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}