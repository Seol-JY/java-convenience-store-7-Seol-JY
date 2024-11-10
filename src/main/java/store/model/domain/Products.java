package store.model.domain;

import store.dto.ProductFileDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Products {
    private static final String NOT_FOUND_PROMOTION_MESSAGE = "존재하지 않는 프로모션입니다: %s";

    private final List<Product> values;

    private Products(final List<Product> values) {
        this.values = values;
    }

    public static Products from(List<ProductFileDto> productDtos, Promotions promotions) {
        Map<String, List<ProductFileDto>> productsByName = productDtos.stream()
                .collect(Collectors.groupingBy(ProductFileDto::name));

        List<Product> products = productsByName.entrySet().stream()
                .map(entry -> createProduct(entry.getKey(), entry.getValue(), promotions))
                .toList();

        return new Products(products);
    }

    private static Product createProduct(
            String productName,
            List<ProductFileDto> dtos,
            Promotions promotions
    ) {
        Product.Builder builder = Product.builder(productName);

        for (ProductFileDto dto : dtos) {
            addStockToBuilder(builder, dto, promotions);
        }

        return builder.build();
    }

    private static void addStockToBuilder(
            Product.Builder builder,
            ProductFileDto dto,
            Promotions promotions
    ) {
        ProductStock stock = ProductStock.of(dto.price(), dto.quantity());

        if (dto.promotion() == null) {
            builder.normalStock(stock);
            return;
        }

        Promotion promotion = findPromotion(dto.promotion(), promotions);
        builder.promotionalStock(stock)
                .promotion(promotion);
    }

    private static Promotion findPromotion(String promotionName, Promotions promotions) {
        return promotions.findByName(promotionName)
                .orElseThrow(
                        () -> new IllegalArgumentException(String.format(NOT_FOUND_PROMOTION_MESSAGE, promotionName))
                );
    }
}
