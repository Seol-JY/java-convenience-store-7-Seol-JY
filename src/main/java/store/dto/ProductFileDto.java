package store.dto;

import java.io.File;
import store.util.NumberParser;

public record ProductFileDto(
        String name,
        Integer price,
        Integer quantity,
        String promotion
) implements FileDto {

    public static ProductFileDto of(
            final String rawName,
            final String rawPrice,
            final String rawQuantity,
            final String rawPromotion
    ) {
        return new ProductFileDto(
                rawName,
                NumberParser.parse(rawPrice),
                NumberParser.parse(rawQuantity),
                rawPromotion
        );
    }
}
