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

    public void printProducts(List<ProductDto> products) {
        StringBuilder result = new StringBuilder();
        result.append(WELCOME_MESSAGE)
                .append(NEW_LINE)
                .append(PRODUCT_LIST_HEADER)
                .append(NEW_LINE)
                .append(NEW_LINE);

        for (ProductDto product : products) {
            result.append(formatProduct(product))
                    .append(NEW_LINE);
        }

        System.out.println(result);
    }

    private String formatProduct(ProductDto product) {
        return String.format(
                PRODUCT_FORMAT,
                product.name(),
                product.price(),
                formatStock(product.quantity()),
                formatPromotion(product.promotion())
        );
    }

    private String formatStock(int quantity) {
        if (quantity <= 0) {
            return OUT_OF_STOCK;
        }

        return String.format(STOCK_FORMAT, quantity);
    }

    private String formatPromotion(String promotion) {
        if (promotion == null) {
            return EMPTY_STRING;
        }

        return SPACE_STRING + promotion;
    }
}
