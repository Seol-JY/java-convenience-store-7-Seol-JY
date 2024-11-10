package store.dto;

import store.util.NumberParser;

public record ProductDto(
        String name,
        Integer price,
        Integer quantity,
        String promotion
) implements FileDto {

    private static final String NULL_PROMOTION = "null";

    public static ProductDto of(
            final String rawName,
            final String rawPrice,
            final String rawQuantity,
            final String rawPromotion
    ) {
        String promotion = rawPromotion;
        if (NULL_PROMOTION.equals(rawPromotion)) {
            promotion = null;
        }

        return new ProductDto(
                rawName,
                NumberParser.parse(rawPrice),
                NumberParser.parse(rawQuantity),
                promotion
        );
    }
}
