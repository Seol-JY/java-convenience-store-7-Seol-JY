package store.model.order.chain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.dto.OrderItemDto;
import store.dto.ProductDto;
import store.dto.PromotionFileDto;
import store.dto.StockReduceResultDto;
import store.model.domain.Product;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;
import store.model.order.OrderContext;

class InventoryReduceHandlerTest {

    @Nested
    class 일반_상품_재고_감소_테스트 {

        @Test
        void 일반_상품_주문시_일반_재고에서만_차감된다() {
            // given
            OrderContext orderContext = createNormalOrderContext("상품A", 100, 5);
            InventoryReduceHandler handler = new InventoryReduceHandler();

            // when
            handler.process(orderContext);

            // then
            Map<Product, StockReduceResultDto> results = orderContext.getStockReduceResults();
            StockReduceResultDto result = results.values().iterator().next();

            assertSoftly(softly -> {
                softly.assertThat(result.normalQuantity()).isEqualTo(5);  // 일반 재고에서 5개 차감
                softly.assertThat(result.promotionalQuantity()).isZero();  // 프로모션 재고 차감 없음
                softly.assertThat(result.freeQuantity()).isZero();  // 증정 수량 없음
                softly.assertThat(result.getTotalQuantity()).isEqualTo(5);  // 총 차감 수량
            });
        }
    }

    @Nested
    class 프로모션_상품_재고_감소_테스트 {

        @ParameterizedTest(name = "주문[{0}개] 프로모션재고[{1}개] 일반재고[{2}개] → 프로모션차감[{3}개] 일반차감[{4}개] 증정[{5}개]")
        @CsvSource({
                // order, pStock, nStock, pUsed, nUsed, free
                "6,     6,      100,    6,     0,     2",    // 프로모션 재고로만 처리 (2+1 두세트)
                "6,     3,      100,    3,     3,     1",    // 프로모션 1세트 + 일반 3개
                "6,     0,      100,    0,     6,     0",    // 전체 일반 재고 사용
                "3,     9,      100,    3,     0,     1",    // 정확히 1세트
                "5,     9,      100,    5,     0,     1",    // 1세트 + 일반 2개
                "1,     9,      100,    1,     0,     0",    // 최소 주문 - 세트 미달
                "2,     9,      100,    2,     0,     0",    // 세트 미달 케이스
                "9,     3,      100,    3,     6,     1",    // 프로모션 재고 부분 사용 + 일반
                "12,    9,      100,    9,     3,     3",    // 프로모션 재고 전체 사용 + 일반
                "8,     6,      100,    6,     2,     2",    // 프로모션 재고 전체 사용 (2세트) + 일반
                "7,     9,      100,    7,     0,     2",    // 2세트 가능한데 1개 남음
                "3,     2,      100,    2,     1,     0",    // 프로모션 재고가 세트 구성에 부족
                "15,    9,      100,    9,     6,     3",    // 대량 주문 - 프로모션 최대 사용
        })
        void 프로모션_상품_주문시_재고_차감_결과를_정확히_계산한다(
                int orderQuantity, int promotionalStock, int normalStock,
                int expectedPromotionalUse, int expectedNormalUse, int expectedFreeItems
        ) {
            // given
            OrderContext orderContext = createPromotionalOrderContext(
                    "상품A", 2, 1,  // 2+1 프로모션
                    promotionalStock, normalStock, orderQuantity
            );
            InventoryReduceHandler handler = new InventoryReduceHandler();

            // when
            handler.process(orderContext);

            // then
            Map<Product, StockReduceResultDto> results = orderContext.getStockReduceResults();
            StockReduceResultDto result = results.values().iterator().next();

            assertSoftly(softly -> {
                softly.assertThat(result.normalQuantity()).isEqualTo(expectedNormalUse);
                softly.assertThat(result.promotionalQuantity()).isEqualTo(expectedPromotionalUse);
                softly.assertThat(result.freeQuantity()).isEqualTo(expectedFreeItems);
                softly.assertThat(result.getTotalQuantity())
                        .isEqualTo(expectedNormalUse + expectedPromotionalUse);
            });
        }

        @Test
        void 프로모션_기간이_아닌_경우_일반_재고에서만_차감된다() {
            // given
            OrderContext orderContext = createExpiredPromotionalOrderContext(
                    "상품A", 2, 1, 10, 100, 6
            );
            InventoryReduceHandler handler = new InventoryReduceHandler();

            // when
            handler.process(orderContext);

            // then
            Map<Product, StockReduceResultDto> results = orderContext.getStockReduceResults();
            StockReduceResultDto result = results.values().iterator().next();

            assertSoftly(softly -> {
                softly.assertThat(result.normalQuantity()).isEqualTo(6);
                softly.assertThat(result.promotionalQuantity()).isZero();
                softly.assertThat(result.freeQuantity()).isZero();
                softly.assertThat(result.getTotalQuantity()).isEqualTo(6);
            });
        }
    }

    private OrderContext createNormalOrderContext(
            String productName,
            int normalStock,
            int orderQuantity
    ) {
        ProductDto normalProductDto = ProductDto.of(
                productName,
                "1000",
                String.valueOf(normalStock),
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
            int promotionalStock,
            int normalStock,
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
                String.valueOf(promotionalStock),
                promotionDto.name()
        );

        ProductDto normalProductDto = ProductDto.of(
                productName,
                "1000",
                String.valueOf(normalStock),
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

    private OrderContext createExpiredPromotionalOrderContext(
            String productName,
            int buy,
            int get,
            int promotionalStock,
            int normalStock,
            int orderQuantity
    ) {
        LocalDateTime now = LocalDateTime.now();
        PromotionFileDto promotionDto = new PromotionFileDto(
                productName + "_프로모션",
                buy,
                get,
                now.minusDays(14).toLocalDate(),
                now.minusDays(7).toLocalDate()
        );
        Promotion promotion = Promotion.from(promotionDto);
        Promotions promotions = Promotions.from(List.of(promotion));

        ProductDto promotionalProductDto = ProductDto.of(
                productName,
                "0",
                String.valueOf(promotionalStock),
                promotionDto.name()
        );

        ProductDto normalProductDto = ProductDto.of(
                productName,
                "1000",
                String.valueOf(normalStock),
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
}
