package store.model.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import store.dto.ProductDto;

public class Products {
    private static final String NOT_FOUND_PROMOTION_MESSAGE = "존재하지 않는 프로모션입니다: %s";
    private static final String MESSAGE_PRODUCT_NOT_MESSAGE = "상품을 찾을 수 없습니다: ";
    private static final String INVALID_PROMOTION_STOCK_MESSAGE = "유효하지 않은 프로모션 재고입니다.";

    private final List<Product> values;

    private Products(final List<Product> values) {
        this.values = values;
    }

    public static Products from(final List<ProductDto> productDtos, final Promotions promotions) {
        Map<String, List<ProductDto>> productsByName = productDtos.stream()
                .collect(Collectors.groupingBy(ProductDto::name));

        List<Product> products = productsByName.entrySet().stream()
                .map(entry -> createProduct(entry.getKey(), entry.getValue(), promotions))
                .toList();

        return new Products(products);
    }

    private static Product createProduct(
            final String productName,
            final List<ProductDto> dtos,
            final Promotions promotions
    ) {
        Product.Builder builder = Product.builder(productName);

        for (ProductDto dto : dtos) {
            addStockToBuilder(builder, dto, promotions);
        }

        return builder.build();
    }

    private static void addStockToBuilder(
            final Product.Builder builder,
            final ProductDto dto,
            final Promotions promotions
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

    private static Promotion findPromotion(final String promotionName, final Promotions promotions) {
        return promotions.findByName(promotionName)
                .orElseThrow(
                        () -> new IllegalArgumentException(String.format(NOT_FOUND_PROMOTION_MESSAGE, promotionName))
                );
    }

    public List<ProductDto> updateDtoQuantities(final List<ProductDto> dtos) {
        return dtos.stream()
                .map(this::createUpdatedDto)
                .toList();
    }

    private ProductDto createUpdatedDto(final ProductDto dto) {
        Product product = findProductByName(dto.name());
        int actualQuantity = getActualQuantity(product, dto.promotion());

        return ProductDto.of(
                dto.name(),
                String.valueOf(dto.price()),
                String.valueOf(actualQuantity),
                dto.promotion()
        );
    }

    private Product findProductByName(final String name) {
        return values.stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MESSAGE_PRODUCT_NOT_MESSAGE + name));
    }


    private int getActualQuantity(final Product product, final String promotionName) {
        if (promotionName == null) {
            return product.getNormalStock().getQuantity();
        }

        if (product.getPromotion() == null || !product.getPromotion().getName().equals(promotionName)) {
            throw new IllegalStateException(INVALID_PROMOTION_STOCK_MESSAGE);
        }

        return product.getPromotionalStock().getQuantity();
    }
}
