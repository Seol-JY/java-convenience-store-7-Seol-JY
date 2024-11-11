package store.view;

import java.util.List;
import java.util.Map;
import store.dto.ProductDto;
import store.dto.ReceiptDto;

public class OutputView {
    // 기존 상수들 유지
    private static final String WELCOME_MESSAGE = "안녕하세요. W편의점입니다.";
    private static final String PRODUCT_LIST_HEADER = "현재 보유하고 있는 상품입니다.";
    private static final String PRODUCT_FORMAT = "- %s %,d원 %s%s";
    private static final String OUT_OF_STOCK = "재고 없음";
    private static final String STOCK_FORMAT = "%,d개";
    private static final String EMPTY_STRING = "";
    private static final String SPACE_STRING = " ";
    private static final String NEW_LINE = "\n";
    private static final String ERROR_PREFIX = "[ERROR] ";

    private static final String RECEIPT_HEADER = "==============W 편의점================";
    private static final String RECEIPT_DIVIDER = "====================================";
    private static final String PROMOTION_DIVIDER = "=============증      정===============";
    private static final String TOTAL_PRICE = "총구매액";
    private static final String PROMOTION_DISCOUNT = "행사할인";
    private static final String MEMBERSHIP_DISCOUNT = "멤버십할인";
    private static final String FINAL_PRICE = "내실돈";
    private static final String NO_PROMOTION = "없음";

    private static final int NAME_COLUMN_WIDTH = 5;
    private static final int QUANTITY_COLUMN_WIDTH = 13;
    private static final int PRICE_COLUMN_WIDTH = 15;

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

    // 영수증 출력 관련 메서드들 수정
    public void printReceipt(final ReceiptDto receipt) {
        StringBuilder builder = new StringBuilder();
        appendHeader(builder);
        appendOrderedItems(builder, receipt.orderedItems());
        appendPromotionalItems(builder, receipt.promotionalItems());
        appendPriceInformation(builder, receipt.priceInfo());

        System.out.println(builder);
    }

    private void appendHeader(final StringBuilder builder) {
        builder.append(RECEIPT_HEADER).append(NEW_LINE);
        builder.append(formatRow("상품명", "수량", "금액")).append(NEW_LINE);
    }

    private void appendOrderedItems(
            final StringBuilder builder,
            final Map<String, ReceiptDto.OrderItemInfo> orderedItems
    ) {
        orderedItems.values().forEach(item ->
                builder.append(formatRow(
                        item.name(),
                        String.valueOf(item.quantity()),
                        String.format("%,d", item.price())
                )).append(NEW_LINE)
        );
    }

    private void appendPromotionalItems(
            final StringBuilder builder,
            final Map<String, Integer> promotionalItems
    ) {
        builder.append(PROMOTION_DIVIDER).append(NEW_LINE);

        if (promotionalItems.isEmpty()) {
            builder.append(formatRow(NO_PROMOTION, "", "")).append(NEW_LINE);
            return;
        }

        promotionalItems.forEach((name, quantity) ->
                builder.append(formatRow(name, String.valueOf(quantity), ""))
                        .append(NEW_LINE)
        );
    }

    private void appendPriceInformation(
            final StringBuilder builder,
            final ReceiptDto.PriceInfo priceInfo
    ) {
        builder.append(RECEIPT_DIVIDER).append(NEW_LINE);
        builder.append(formatRow(
                TOTAL_PRICE, String.valueOf(priceInfo.totalQuantity()), String.format("%,d", priceInfo.totalPrice())
        )).append(NEW_LINE);

        builder.append(formatRow(
                PROMOTION_DISCOUNT, "", String.format("-%,d", priceInfo.promotionDiscount())
        )).append(NEW_LINE);

        builder.append(formatRow(
                MEMBERSHIP_DISCOUNT, "", String.format("-%,d", priceInfo.membershipDiscount())
        )).append(NEW_LINE);

        builder.append(formatRow(FINAL_PRICE, "", String.format("%,d", priceInfo.calculateFinalPrice())
        )).append(NEW_LINE);
    }

    private String formatRow(String name, String quantity, String price) {
        return String.format("%-" + NAME_COLUMN_WIDTH + "s%"
                        + QUANTITY_COLUMN_WIDTH + "s%"
                        + PRICE_COLUMN_WIDTH + "s",
                name, quantity, price);
    }
}
