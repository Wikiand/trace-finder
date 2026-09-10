import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        int exitCode = run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int run(String[] args) {

        if (args.length != 3 && args.length != 5) {
            System.err.println(
                    "Usage: java Main logs.txt rules.csv report.txt [start timestamp] [end timestamp]"
            );
            return 1;
        }

        Path logPath = Path.of(args[0]);
        Path rulebookPath = Path.of(args[1]);
        Path reportPath = Path.of(args[2]);

        try {
            java.time.LocalDateTime start = null;
            java.time.LocalDateTime end = null;

            if (args.length == 5) {
                java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"
                        );

                try {
                    start = java.time.LocalDateTime.parse(args[3], formatter);
                    end = java.time.LocalDateTime.parse(args[4], formatter);
                } catch (java.time.format.DateTimeParseException e) {
                    System.err.println(
                            "Error: timestamps must use format yyyy-MM-dd HH:mm:ss"
                    );
                    return 1;
                }

                if (start.isAfter(end)) {
                    System.err.println(
                            "Error: start timestamp must not be after end timestamp"
                    );
                    return 1;
                }
            }
            RulebookReader rulebookReader = new RulebookReader();
            Rulebook rulebook = rulebookReader.read(rulebookPath);

            LogReader logReader = new LogReader(new Parser());
            LogReadResult logResult = logReader.read(logPath);

            Analyzer analyzer = new Analyzer();
            AnalysisResult analysisResult;

            if (args.length == 5) {
                analysisResult = analyzer.analyze(
                        logResult.getEntries(),
                        rulebook,
                        start,
                        end
                );
            } else {
                analysisResult = analyzer.analyze(
                        logResult.getEntries(),
                        rulebook
                );
            }

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