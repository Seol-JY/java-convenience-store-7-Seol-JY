package store.model.domain;

import java.time.LocalDate;
import store.dto.PromotionFileDto;
import store.vo.DateRange;

public class Promotion {
    private static final int MINIMUM_QUANTITY = 1;
    private static final String INVALID_BUY_QUANTITY_MESSAGE = "구매 수량은 1개 이상이어야 합니다.";
    private static final String INVALID_GET_QUANTITY_MESSAGE = "증정 수량은 1개 이상이어야 합니다.";

    private final String name;
    private final Integer buy;
    private final Integer get;
    private final DateRange dateRange;

    private Promotion(
            final String name,
            final Integer buy,
            final Integer get,
            final DateRange dateRange
    ) {
        validateBuyQuantity(buy);
        validateGetQuantity(get);

        this.name = name;
        this.buy = buy;
        this.get = get;
        this.dateRange = dateRange;
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

    public static Promotion from(PromotionFileDto dto) {
        return new Promotion(
                dto.name(),
                dto.buy(),
                dto.get(),
                DateRange.of(dto.startDate(), dto.endDate())
        );
    }

    public String getName() {
        return name;
    }
}
