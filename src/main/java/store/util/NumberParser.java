package store.util;

import static store.constant.ExceptionMessage.WRONG_INPUT;

public class NumberParser {
    private static final int MAX_DIGITS_TO_PREVENT_OVERFLOW = 10;

    public static Integer parse(final String input) {
        validateNull(input);

        String trimmedInput = input.strip();
        validateEmpty(trimmedInput);
        validateOverflow(trimmedInput);

        return parseToInt(trimmedInput);
    }

    private static void validateNull(final String input) {
        if (input == null) {
            throw new IllegalArgumentException(WRONG_INPUT.message());
        }
    }

    private static void validateEmpty(final String input) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException(WRONG_INPUT.message());
        }
    }

    private static void validateOverflow(final String input) {
        if (input.length() >= MAX_DIGITS_TO_PREVENT_OVERFLOW) {
            throw new IllegalArgumentException(WRONG_INPUT.message());
        }
    }

    private static Integer parseToInt(final String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_INPUT.message());
        }
    }
}
