import java.io.BufferedReader;
import java.io.IOException;

public class LimitedLineReader {

    private final BufferedReader reader;
    private final int maxLineCharacters;

    private int pendingCharacter = -1;

    public LimitedLineReader(
            BufferedReader reader,
            int maxLineCharacters) {

        this.reader = reader;
        this.maxLineCharacters = maxLineCharacters;
    }

    public LineReadResult readLine() throws IOException {

        StringBuilder line =
                new StringBuilder(
                        Math.min(maxLineCharacters, 1024)
                );

        boolean exceeded = false;
        boolean sawCharacter = false;

        while (true) {

            int value = readCharacter();

            if (value == -1) {

                if (!sawCharacter) {
                    return null;
                }

                return new LineReadResult(
                        line.toString(),
                        exceeded
                );
            }

            sawCharacter = true;

            char character = (char) value;

            if (character == '\n') {
                return new LineReadResult(
                        line.toString(),
                        exceeded
                );
            }

            if (character == '\r') {

                int next = readCharacter();

                if (next != '\n' && next != -1) {
                    pendingCharacter = next;
                }

                return new LineReadResult(
                        line.toString(),
                        exceeded
                );
            }

            if (line.length() < maxLineCharacters) {
                line.append(character);
            } else {
                exceeded = true;
            }
        }
    }

    private int readCharacter() throws IOException {

        if (pendingCharacter != -1) {

            int character = pendingCharacter;
            pendingCharacter = -1;

            return character;
        }

        return reader.read();
    }

    public static final class LineReadResult {

        private final String content;
        private final boolean exceededLimit;

        public LineReadResult(
                String content,
                boolean exceededLimit) {

            this.content = content;
            this.exceededLimit = exceededLimit;
        }

        public String getContent() {
            return content;
        }

        public boolean exceededLimit() {
            return exceededLimit;
        }
    }
}