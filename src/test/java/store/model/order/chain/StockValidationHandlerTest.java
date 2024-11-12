package store.model.order.chain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static store.constant.ExceptionMessage.INSUFFICIENT_STOCK;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.dto.OrderItemDto;
import store.dto.ProductDto;
import store.dto.PromotionFileDto;
import store.model.domain.Product;
import store.model.domain.ProductStock;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;
import store.model.order.OrderContext;

class StockValidationHandlerTest {

    private StockValidationHandler handler;
    private LocalDateTime orderDateTime;

    @BeforeEach
    void setUp() {
        handler = new StockValidationHandler();
        orderDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
    }

    @Nested
    class 재고_검증_테스트 {
        @Test
        void 재고가_충분하면_검증을_통과한다() {
            // given
            Product product = createProductWithStock(10);  // 재고 10개
            OrderContext orderContext = createNormalOrderContext(product, 5, orderDateTime);  // 주문 5개

            // when & then
            handler.process(orderContext);  // 예외가 발생하지 않아야 함
        }

        @Test
        void 재고와_주문_수량이_같으면_검증을_통과한다() {
            // given
            Product product = createProductWithStock(10);  // 재고 10개
            OrderContext orderContext = createNormalOrderContext(product, 10, orderDateTime);  // 주문 10개

            // when & then
            handler.process(orderContext);  // 예외가 발생하지 않아야 함
        }

        @Test
        void 재고보다_많은_수량을_주문하면_예외가_발생한다() {
            // given
            Product product = createProductWithStock(10);  // 재고 10개
            OrderContext orderContext = createNormalOrderContext(product, 11, orderDateTime);  // 주문 11개

            // when & then
            assertThatThrownBy(() -> handler.process(orderContext))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(INSUFFICIENT_STOCK.message());
        }

        @Test
        void 프로모션_기간이_아닌_경우_일반_재고만으로_검증한다() {
            // given
            Product product = createProductWithPromotionalStock(
                    5,  // 일반 재고 5개
                    5,  // 프로모션 재고 5개
                    LocalDate.of(2025, 1, 1),  // 미래의 프로모션
                    LocalDate.of(2025, 12, 31)
            );
            OrderContext orderContext = createPromotionalOrderContext(product, 6, orderDateTime);  // 주문 6개

            // when & then
            assertThatThrownBy(() -> handler.process(orderContext))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(INSUFFICIENT_STOCK.message());
        }

        @Test
        void 프로모션_기간_내_총_재고로_검증한다() {
            // given
            Product product = createProductWithPromotionalStock(
                    5,  // 일반 재고 5개
                    5,  // 프로모션 재고 5개
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            OrderContext orderContext = createPromotionalOrderContext(product, 8, orderDateTime);  // 주문 8개

            // when & then
            handler.process(orderContext);  // 예외가 발생하지 않아야 함
        }
    }

    private Product createProductWithStock(int quantity) {
        return Product.builder("상품")
                .normalStock(ProductStock.of(1000, quantity))
                .build();
    }

    private Product createProductWithPromotionalStock(
            int normalQuantity,
            int promotionalQuantity,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String promotionName = "테스트프로모션";
        Promotion promotion = Promotion.from(
                PromotionFileDto.of(
                        promotionName,
                        "2",
                        "1",
                        startDate.toString(),
                        endDate.toString()
                )
        );

        return Product.builder("상품")
                .normalStock(ProductStock.of(1000, normalQuantity))
                .promotionalStock(ProductStock.of(800, promotionalQuantity))
                .promotion(promotion)
                .build();
    }

    private OrderContext createNormalOrderContext(Product product, int quantity, LocalDateTime orderDateTime) {
        List<OrderItemDto> items = List.of(
                OrderItemDto.of(product.getName(), quantity)
        );

        Products products = Products.from(
                List.of(ProductDto.of(
                        product.getName(),
                        String.valueOf(product.getNormalStock().getPrice()),
                        String.valueOf(product.getNormalStock().getQuantity()),
                        "null"
                )),
                Promotions.from(List.of())
        );

        return OrderContext.of(this.orderDateTime, items, products);
    }

    private OrderContext createPromotionalOrderContext(Product product, int quantity, LocalDateTime orderDateTime) {
        String promotionName = "테스트프로모션";
        List<OrderItemDto> items = List.of(
                OrderItemDto.of(product.getName(), quantity)
        );

        Promotion promotion = product.getPromotion();
        Products products = Products.from(
                List.of(
                        ProductDto.of(
                                product.getName(),
                                String.valueOf(product.getNormalStock().getPrice()),
                                String.valueOf(product.getNormalStock().getQuantity()),
                                "null"
                        ),
                        ProductDto.of(
                                product.getName(),
                                String.valueOf(product.getPromotionalStock().getPrice()),
                                String.valueOf(product.getPromotionalStock().getQuantity()),
                                promotionName
                        )
                ),
                Promotions.from(List.of(promotion))
        );

        return OrderContext.of(this.orderDateTime, items, products);
    }
}
