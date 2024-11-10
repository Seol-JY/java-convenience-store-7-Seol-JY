package store.model.domain;


public class Product {
    private final String name;
    private final ProductStock normalStock;
    private final ProductStock promotionalStock;
    private final Promotion promotion;

    private Product(final Builder builder) {
        this.name = builder.name;
        this.normalStock = builder.normalStock;
        this.promotionalStock = builder.promotionalStock;
        this.promotion = builder.promotion;
    }

    public static Builder builder(final String name) {
        return new Builder(name);
    }

    public static class Builder {
        private static final String ERROR_NO_STOCK = "상품은 일반 재고와 프로모션 재고 중 적어도 하나는 있어야 합니다.";
        private static final String ERROR_NO_PROMOTION = "프로모션 재고가 있다면 프로모션이 반드시 있어야 합니다.";
        private static final String ERROR_NO_NAME = "상품 이름은 필수입니다.";
        private static final String ERROR_DUPLICATE_PRODUCT = "동일한 상품에 대해 중복된 일반 상품이 존재합니다.";
        private static final String ERROR_DUPLICATE_PROMOTIONAL_PRODUCT = "동일한 상품에 대해 중복된 프로모션 상품이 존재합니다.";

        private final String name;
        private ProductStock normalStock;
        private ProductStock promotionalStock;
        private Promotion promotion;

        private Builder(final String name) {
            validateName(name);
            this.name = name;
        }

        private void validateName(final String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException(ERROR_NO_NAME);
            }
        }

        public Builder normalStock(final ProductStock normalStock) {
            if (this.normalStock != null) {
                throw new IllegalStateException(ERROR_DUPLICATE_PRODUCT);
            }

            this.normalStock = normalStock;
            return this;
        }

        public Builder promotionalStock(final ProductStock promotionalStock) {
            if (this.promotionalStock != null) {
                throw new IllegalStateException(ERROR_DUPLICATE_PROMOTIONAL_PRODUCT);
            }

            this.promotionalStock = promotionalStock;
            return this;
        }

        public Builder promotion(final Promotion promotion) {
            this.promotion = promotion;
            return this;
        }

        public Product build() {
            validateStockExistence();
            validatePromotionExistence();

            return new Product(this);
        }

        private void validateStockExistence() {
            if (normalStock == null && promotionalStock == null) {
                throw new IllegalStateException(ERROR_NO_STOCK);
            }
        }

        private void validatePromotionExistence() {
            if (promotionalStock != null && promotion == null) {
                throw new IllegalStateException(ERROR_NO_PROMOTION);
            }
        }
    }
}
