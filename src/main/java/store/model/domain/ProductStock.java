package store.model.domain;

public class ProductStock {
    private static final int MINIMUM_AMOUNT = 0;
    private static final String INVALID_PRICE_MESSAGE = "가격은 0 이상의 정수여야 합니다.";
    private static final String INVALID_QUANTITY_MESSAGE = "수량은 0 이상의 정수여야 합니다.";
    private static final String INSUFFICIENT_STOCK_MESSAGE = "재고가 부족합니다.";

    private final Integer price;
    private Integer quantity;

    private ProductStock(final Integer price, final Integer quantity) {
        validate(price, quantity);
        this.price = price;
        this.quantity = quantity;
    }

    private void validate(final Integer price, final Integer quantity) {
        validatePrice(price);
        validateQuantity(quantity);
    }

    private void validatePrice(final Integer price) {
        if (price == null || price < MINIMUM_AMOUNT) {
            throw new IllegalArgumentException(INVALID_PRICE_MESSAGE);
        }
    }

    private void validateQuantity(final Integer quantity) {
        if (quantity == null || quantity < MINIMUM_AMOUNT) {
            throw new IllegalArgumentException(INVALID_QUANTITY_MESSAGE);
        }
    }

    public static ProductStock of(final Integer price, final Integer quantity) {
        return new ProductStock(price, quantity);
    }

    public void reduceQuantity(final int quantity) {
        if (quantity > this.quantity) {
            throw new IllegalStateException(INSUFFICIENT_STOCK_MESSAGE);
        }

        this.quantity -= quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

}
