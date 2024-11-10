package store.vo;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {
    private static final String INVALID_DATE_RANGE_MESSAGE = "시작일은 종료일보다 이전이어야 합니다.";

    public DateRange {
        validate(startDate, endDate);
    }

    private void validate(final LocalDate startDate, final LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(INVALID_DATE_RANGE_MESSAGE);
        }
    }

    public static DateRange of(final LocalDate startDate, final LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }
}
