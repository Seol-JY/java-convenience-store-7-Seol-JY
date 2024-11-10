package store.constant;

public enum ExceptionMessage {
    WRONG_INPUT("잘못된 입력입니다. 다시 입력해 주세요.");

    private final String message;

    ExceptionMessage(final String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    public String format(final Object... args) {
        return String.format(message, args);
    }
}
