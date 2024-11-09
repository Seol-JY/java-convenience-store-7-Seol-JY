package store.model.doamin;

import java.time.LocalDate;
import store.dto.PromotionFileDto;

public class Promotion {
    private static final int MINIMUM_QUANTITY = 1;
    private static final String INVALID_BUY_QUANTITY_MESSAGE = "구매 수량은 1개 이상이어야 합니다.";
    private static final String INVALID_GET_QUANTITY_MESSAGE = "증정 수량은 1개 이상이어야 합니다.";
    private static final String INVALID_DATE_RANGE_MESSAGE = "시작일은 종료일보다 이전이어야 합니다.";

    private final String name;
    private final Integer buy;
    private final Integer get;
    private final LocalDate startDate;
    private final LocalDate endDate;

    private Promotion(
            final String name,
            final Integer buy,
            final Integer get,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        validateBuyQuantity(buy);
        validateGetQuantity(get);
        validateDateRange(startDate, endDate);

        this.name = name;
        this.buy = buy;
        this.get = get;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private void validateBuyQuantity(Integer buy) {
        if (buy == null || buy < MINIMUM_QUANTITY) {
            throw new IllegalArgumentException(INVALID_BUY_QUANTITY_MESSAGE);
        }
    }

    private void validateGetQuantity(Integer get) {
        if (get == null || get < MINIMUM_QUANTITY) {
            throw new IllegalArgumentException(INVALID_GET_QUANTITY_MESSAGE);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(INVALID_DATE_RANGE_MESSAGE);
        }
    }

    public static Promotion from(PromotionFileDto dto) {
        return new Promotion(
                dto.name(),
                dto.buy(),
                dto.get(),
                dto.startDate(),
                dto.endDate()
        );
    }

    public String getName() {
        return name;
    }
}
