package store.util;

import static store.constant.ExceptionMessage.WRONG_INPUT;

public class YesNoParser {
    private static final String YES = "Y";
    private static final String NO = "N";

    public static Boolean parse(final String input) {
        validateNull(input);

        String trimmedInput = input.strip();
        validateEmpty(trimmedInput);
        validateCorrectInput(trimmedInput);

        return parseToBoolean(trimmedInput);
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

    private static void validateCorrectInput(final String input) {
        if (!input.equals(YES) && !input.equals(NO)) {
            throw new IllegalArgumentException(WRONG_INPUT.message());
        }
    }

    private static Boolean parseToBoolean(final String input) {
        return input.equals(YES);
    }
}
