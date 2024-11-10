package store.model.domain;

import java.util.List;
import java.util.Optional;

public class Promotions {
    private static final String DUPLICATE_PROMOTION_NAME_MESSAGE = "중복된 이름의 프로모션이 존재합니다.";

    private final List<Promotion> values;

    private Promotions(final List<Promotion> values) {
        validateDuplicatePromotionNames(values);
        this.values = values;
    }

    private void validateDuplicatePromotionNames(final List<Promotion> values) {
        long uniqueNameCount = values.stream()
                .map(Promotion::getName)
                .distinct()
                .count();

        if (uniqueNameCount != values.size()) {
            throw new IllegalArgumentException(DUPLICATE_PROMOTION_NAME_MESSAGE);
        }
    }

    public static Promotions from(final List<Promotion> values) {
        return new Promotions(values);
    }

    public Optional<Promotion> findByName(final String promotionName) {
        return values.stream()
                .filter(promotion -> promotion.getName().equals(promotionName))
                .findFirst();
    }
}
