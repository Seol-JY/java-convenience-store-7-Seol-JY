package store.model.order.chain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.dto.OrderItemDto;
import store.dto.ProductDto;
import store.dto.PromotionFileDto;
import store.dto.ReceiptDto;
import store.dto.StockReduceResultDto;
import store.model.domain.Product;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;
import store.model.order.OrderContext;

class PurchaseHandlerTest {

    private static final int NORMAL_PRICE = 1000;

    @Nested
    class 영수증_생성_테스트 {

        @Test
        void 일반_상품_주문시_영수증이_정상_생성된다() {
            // given
            OrderContext orderContext = createNormalOrderContext("상품A", 3);
            attachStockReduceResult(orderContext, false, 3, 0, 0);
            PurchaseHandler handler = new PurchaseHandler();

            // when
            handler.process(orderContext);

            // then
            ReceiptDto receipt = orderContext.getReceipt();
            assertSoftly(softly -> {
                softly.assertThat(receipt.orderedItems().get("상품A").quantity()).isEqualTo(3);
                softly.assertThat(receipt.orderedItems().get("상품A").price()).isEqualTo(3000);
                softly.assertThat(receipt.promotionalItems()).isEmpty();
                softly.assertThat(receipt.priceInfo().totalQuantity()).isEqualTo(3);
                softly.assertThat(receipt.priceInfo().totalPrice()).isEqualTo(3000);
                softly.assertThat(receipt.priceInfo().promotionDiscount()).isZero();
                softly.assertThat(receipt.priceInfo().membershipDiscount()).isZero();
            });
        }

        @ParameterizedTest(name = "주문수량[{0}] 프로모션사용[{1}] 일반사용[{2}] 증정[{3}] -> 총금액[{4}] 할인[{5}]")
        @CsvSource({
                // order, pUsed, nUsed, free, total, discount
                "6,     6,     0,     2,    6000,  2000",    // 프로모션만 사용 (2+1 두세트)
                "6,     3,     3,     1,    6000,  1000",    // 프로모션 1세트 + 일반 3개
                "6,     0,     6,     0,    6000,  0",       // 전체 일반 사용
                "3,     3,     0,     1,    3000,  1000",    // 정확히 1세트
                "5,     3,     2,     1,    5000,  1000"     // 1세트 + 일반 2개
        })
        void 프로모션_상품_주문시_영수증의_금액이_정확히_계산된다(
                int orderQuantity,
                int promotionalUsed,
                int normalUsed,
                int freeItems,
                int expectedTotal,
                int expectedDiscount
        ) {
            // given
            OrderContext orderContext = createPromotionalOrderContext("상품A", 2, 1, orderQuantity);
            attachStockReduceResult(orderContext, true, normalUsed, promotionalUsed, freeItems);
            PurchaseHandler handler = new PurchaseHandler();

            // when
            handler.process(orderContext);

            // then
            ReceiptDto receipt = orderContext.getReceipt();
            assertSoftly(softly -> {
                softly.assertThat(receipt.orderedItems().get("상품A").quantity()).isEqualTo(orderQuantity);
                softly.assertThat(receipt.orderedItems().get("상품A").price()).isEqualTo(expectedTotal);
                if (freeItems > 0) {
                    softly.assertThat(receipt.promotionalItems()).containsEntry("상품A", freeItems);
                } else {
                    softly.assertThat(receipt.promotionalItems()).isEmpty();
                }
                softly.assertThat(receipt.priceInfo().totalQuantity()).isEqualTo(orderQuantity);
                softly.assertThat(receipt.priceInfo().totalPrice()).isEqualTo(expectedTotal);
                softly.assertThat(receipt.priceInfo().promotionDiscount()).isEqualTo(expectedDiscount);
            });
        }

        @Test
        void 멤버십_할인이_적용된다() {
            // given
            OrderContext orderContext = createPromotionalOrderContext("상품A", 2, 1, 6);
            attachStockReduceResult(orderContext, true, 3, 3, 1);
            applyMembershipDiscount(orderContext);
            PurchaseHandler handler = new PurchaseHandler();

            // when
            handler.process(orderContext);

            // then
            ReceiptDto receipt = orderContext.getReceipt();
            assertSoftly(softly -> {
                softly.assertThat(receipt.priceInfo().totalPrice()).isEqualTo(6000);
                softly.assertThat(receipt.priceInfo().promotionDiscount()).isEqualTo(1000);
                softly.assertThat(receipt.priceInfo().membershipDiscount()).isEqualTo(900); // 3000 * 0.3
            });
        }
    }

    private OrderContext createNormalOrderContext(String productName, int orderQuantity) {
        ProductDto normalProductDto = ProductDto.of(
                productName,
                String.valueOf(NORMAL_PRICE),
                "100",
                null
        );

        Products products = Products.from(
                List.of(normalProductDto),
                Promotions.from(List.of())
        );

        List<OrderItemDto> orderItems = List.of(
                new OrderItemDto(productName, orderQuantity)
        );

        return OrderContext.of(LocalDateTime.now(), orderItems, products);
    }

    private OrderContext createPromotionalOrderContext(
            String productName,
            int buy,
            int get,
            int orderQuantity
    ) {
        PromotionFileDto promotionDto = new PromotionFileDto(
                productName + "_프로모션",
                buy,
                get,
                LocalDateTime.now().toLocalDate(),
                LocalDateTime.now().plusDays(7).toLocalDate()
        );
        Promotion promotion = Promotion.from(promotionDto);
        Promotions promotions = Promotions.from(List.of(promotion));

        ProductDto promotionalProductDto = ProductDto.of(
                productName,
                "0",
                "10",
                promotionDto.name()
        );

        ProductDto normalProductDto = ProductDto.of(
                productName,
                String.valueOf(NORMAL_PRICE),
                "100",
                null
        );

        Products products = Products.from(
                List.of(normalProductDto, promotionalProductDto),
                promotions
        );

        List<OrderItemDto> orderItems = List.of(
                new OrderItemDto(productName, orderQuantity)
        );

        return OrderContext.of(LocalDateTime.now(), orderItems, products);
    }

    private void attachStockReduceResult(
            OrderContext orderContext,
            boolean hasPromotion,
            int normalQuantity,
            int promotionalQuantity,
            int freeQuantity
    ) {
        Product product = orderContext.getOrderItems().keySet().iterator().next();
        StockReduceResultDto result = StockReduceResultDto.of(
                normalQuantity,
                promotionalQuantity,
                freeQuantity
        );

        Map<Product, StockReduceResultDto> results = new HashMap<>();
        results.put(product, result);
        orderContext.attachStockReduceResults(results);
    }

    private void applyMembershipDiscount(OrderContext orderContext) {
        orderContext.setMembershipDiscountSupplier(price -> (int) (price * 0.3));
    }
}
