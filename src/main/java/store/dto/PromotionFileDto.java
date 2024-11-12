package store.dto;

import java.time.LocalDate;
import store.util.NumberParser;

public record PromotionFileDto(
        String name,
        Integer buy,
        Integer get,
        LocalDate startDate,
        LocalDate endDate
) implements FileDto {

    public static PromotionFileDto of(
            final String rawName,
            final String rawBuy,
            final String rawGet,
            final String rawStartDate,
            final String rawEndDate
    ) {
        return new PromotionFileDto(
                rawName,
                NumberParser.parse(rawBuy),
                NumberParser.parse(rawGet),
                LocalDate.parse(rawStartDate),
                LocalDate.parse(rawEndDate)
        );
    }
}
