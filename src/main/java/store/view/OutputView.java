package store.view;

import java.util.List;
import store.dto.ProductDto;

public class OutputView {
    private static final String WELCOME_MESSAGE = "안녕하세요. W편의점입니다.";
    private static final String PRODUCT_LIST_HEADER = "현재 보유하고 있는 상품입니다.";
    private static final String PRODUCT_FORMAT = "- %s %,d원 %s%s";
    private static final String OUT_OF_STOCK = "재고 없음";
    private static final String STOCK_FORMAT = "%,d개";
    private static final String EMPTY_STRING = "";
    private static final String SPACE_STRING = " ";
    private static final String NEW_LINE = "\n";
    private static final String ERROR_PREFIX = "[ERROR] ";

    public void printProducts(final List<ProductDto> products) {
        StringBuilder result = new StringBuilder();
        result.append(WELCOME_MESSAGE).append(NEW_LINE).append(PRODUCT_LIST_HEADER).append(NEW_LINE).append(NEW_LINE);

        for (ProductDto product : products) {
            result.append(formatProduct(product))
                    .append(NEW_LINE);

            if (product.promotion() != null && !containsNullPromotionWithSameName(products, product)) {
                result.append(formatOutOfStockProduct(product)).append(NEW_LINE);
            }
        }

        System.out.println(result);
    }

    private boolean containsNullPromotionWithSameName(final List<ProductDto> products, final ProductDto targetProduct) {
        return products.stream().anyMatch(product ->
                product != targetProduct
                        && product.name().equals(targetProduct.name())
                        && product.promotion() == null
        );
    }

    private String formatProduct(final ProductDto product) {
        return String.format(
                PRODUCT_FORMAT,
                product.name(),
                product.price(),
                formatStock(product.quantity()),
                formatPromotion(product.promotion())
        );
    }

    private String formatOutOfStockProduct(final ProductDto product) {
        return String.format(
                PRODUCT_FORMAT,
                product.name(),
                product.price(),
                OUT_OF_STOCK,
                EMPTY_STRING
        );
    }

    private String formatStock(final int quantity) {
        if (quantity <= 0) {
            return OUT_OF_STOCK;
        }

        return String.format(STOCK_FORMAT, quantity);
    }

    private String formatPromotion(final String promotion) {
        if (promotion == null) {
            return EMPTY_STRING;
        }

        return SPACE_STRING + promotion;
    }

    public void printError(final String message) {
        System.out.println(ERROR_PREFIX.concat(message));
    }
}
