package store.dto;

public record StockReduceResultDto(
        int normalQuantity,
        int promotionalQuantity,
        int freeQuantity
) {
    public static StockReduceResultDto of(
            final int normalQuantity,
            final int promotionalQuantity,
            final int freeQuantity
    ) {
        return new StockReduceResultDto(normalQuantity, promotionalQuantity, freeQuantity);
    }

    public int getTotalQuantity() {
        return normalQuantity + promotionalQuantity;
    }
}