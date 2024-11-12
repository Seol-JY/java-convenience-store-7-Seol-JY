package store.dto;

public record OrderItemDto(
        String name,
        Integer quantity
) implements FileDto {

    public static OrderItemDto of(
            final String rawName,
            final Integer rawQuantity
    ) {
        return new OrderItemDto(
                rawName,
                rawQuantity
        );
    }
}
