package store.dto;

import java.util.Map;

public record ReceiptDto(
        Map<String, OrderItemInfo> orderedItems,
        Map<String, Integer> promotionalItems,
        PriceInfo priceInfo
) {
    public record OrderItemInfo(
            String name,
            int quantity,
            int price
    ) {
    }

    public record PriceInfo(
            int totalQuantity,
            int totalPrice,
            int promotionDiscount,
            int membershipDiscount
    ) {
        public int calculateFinalPrice() {
            return totalPrice - promotionDiscount - membershipDiscount;
        }
    }
}
