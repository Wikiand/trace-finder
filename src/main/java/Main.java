import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {

    public static void main(String[] args) {
        int exitCode;

        try {
            exitCode = run(args);
        } catch (TraceFinderArgumentException
                 | TraceFinderFileException
                 | RulebookException e) {

            System.err.println("Error: " + e.getMessage());
            exitCode = 1;
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int run(String[] args)
            throws TraceFinderArgumentException,
                   TraceFinderFileException,
                   RulebookException {

        if (args.length != 3 && args.length != 5) {
            throw new TraceFinderArgumentException(
                    "Usage: java Main logs.txt rules.csv report.txt "
                            + "[start timestamp] [end timestamp]"
            );
        }

        Path logPath = Path.of(args[0]);
        Path rulebookPath = Path.of(args[1]);
        Path reportPath = Path.of(args[2]);

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (args.length == 5) {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            try {
                start = LocalDateTime.parse(args[3], formatter);
                end = LocalDateTime.parse(args[4], formatter);
            } catch (DateTimeParseException e) {
                throw new TraceFinderArgumentException(
                        "Timestamps must use format yyyy-MM-dd HH:mm:ss",
                        e
                );
            }

            if (start.isAfter(end)) {
                throw new TraceFinderArgumentException(
                        "Start timestamp must not be after end timestamp"
                );
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

        try {
            reportWriter.write(
                    reportPath,
                    analysisResult,
                    rulebook,
                    logResult.getMalformedLines(),
                    start,
                    end
            );
        } catch (java.io.IOException e) {
            throw new TraceFinderFileException(
                    "Could not write report file '"
                            + reportPath
                            + "': "
                            + e.getMessage(),
                    e
            );
        }

        return 0;
    }
}