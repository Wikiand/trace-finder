public final class InputCleaner {

    private InputCleaner() {
    }

    public static String clean(String value) {

        if (value == null) {
            return null;
        }

        return value
                .replaceAll("[\\u0000-\\u001F\\u007F\\u200B-\\u200D\\u2060\\uFEFF]", "")
                .trim();
    }
}