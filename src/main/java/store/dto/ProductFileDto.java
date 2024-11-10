package store.dto;

import store.util.NumberParser;

public record ProductFileDto(
        String name,
        Integer price,
        Integer quantity,
        String promotion
) implements FileDto {

    private static final String NULL_PROMOTION = "null";

    public static ProductFileDto of(
            final String rawName,
            final String rawPrice,
            final String rawQuantity,
            final String rawPromotion
    ) {
        String promotion = rawPromotion;
        if (NULL_PROMOTION.equals(rawPromotion)) {
            promotion = null;
        }

        return new ProductFileDto(
                rawName,
                NumberParser.parse(rawPrice),
                NumberParser.parse(rawQuantity),
                promotion
        );
    }
}
