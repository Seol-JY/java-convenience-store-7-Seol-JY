package store.model.order.chain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
import store.model.domain.Product;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;
import store.model.order.OrderContext;

class InsufficientPromotionalStockHandlerTest {

    @Nested
    class 프로모션_재고_부족_처리_테스트 {

        @ParameterizedTest(name = "프로모션[{0}+{1}] 재고[{2}] 주문[{3}] -> 최종주문[{4}]")
        @CsvSource({
                // buy, get, stock, order, expected   // 설명
                "2,    1,    9,     12,    9",       // 정확히 3세트(9개) 가능
                "2,    1,    8,     12,    6",       // 2세트(6개)만 가능
                "3,    1,    8,     12,    8",       // 2세트(8개) 가능
                "3,    2,    10,    12,    10",      // 2세트(10개) 가능
        })
        void 프로모션_재고_부족시_세트단위로_주문수량을_조정한다(
                int buy, int get, int stockQuantity, int orderQuantity, int expectedQuantity
        ) {
            // given
            OrderContext orderContext = createOrderContext(
                    "상품A", buy, get, 1000, stockQuantity, orderQuantity
            );
            InsufficientPromotionalStockHandler handler = createHandler(false);  // 일반가 구매 거부

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            if (expectedQuantity == 0) {
                assertThat(orderItems).isEmpty();
            } else {
                assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum())
                        .isEqualTo(expectedQuantity);
            }
        }

        @Test
        void 일반가_구매_수락시_주문수량을_유지한다() {
            // given
            OrderContext orderContext = createOrderContext(
                    "상품A", 2, 1, 1000, 6, 12  // 2+1 프로모션, 재고 6개, 주문 12개
            );
            InsufficientPromotionalStockHandler handler = createHandler(true);  // 일반가 구매 수락

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum())
                    .isEqualTo(12);  // 주문수량 유지
        }

        @Test
        void 프로모션_상품이_아닌_경우_처리하지_않는다() {
            // given
            OrderContext orderContext = createNonPromotionalOrderContext(
                    "상품A", 1000, 5, 10  // 일반상품, 재고 5개, 주문 10개
            );
            InsufficientPromotionalStockHandler handler = createHandler(false);

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum())
                    .isEqualTo(10);  // 수량 변화 없음
        }

        @Test
        void 프로모션_재고가_충분한_경우_처리하지_않는다() {
            // given
            OrderContext orderContext = createOrderContext(
                    "상품A", 2, 1, 1000, 12, 9  // 재고 12개로 충분
            );
            InsufficientPromotionalStockHandler handler = createHandler(false);

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum())
                    .isEqualTo(9);  // 수량 변화 없음
        }
    }

    private OrderContext createOrderContext(
            String productName,
            int buy,
            int get,
            int normalPrice,
            int promotionalStock,
            int orderQuantity
    ) {
        // 프로모션 생성
        PromotionFileDto promotionDto = new PromotionFileDto(
                productName + "_프로모션",
                buy,
                get,
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        );
        Promotion promotion = Promotion.from(promotionDto);
        Promotions promotions = Promotions.from(List.of(promotion));

        // 프로모션 상품 정보 생성
        ProductDto promotionalProductDto = ProductDto.of(
                productName,
                String.valueOf(0),
                String.valueOf(promotionalStock),
                promotionDto.name()
        );

        // 일반 상품 정보 생성
        ProductDto normalProductDto = ProductDto.of(
                productName,
                String.valueOf(normalPrice),
                String.valueOf(100),
                null
        );

        // Products 생성
        Products products = Products.from(
                List.of(normalProductDto, promotionalProductDto),
                promotions
        );

        // OrderContext 생성
        List<OrderItemDto> orderItems = List.of(
                new OrderItemDto(productName, orderQuantity)
        );

        return OrderContext.of(LocalDateTime.now(), orderItems, products);
    }

    private OrderContext createNonPromotionalOrderContext(
            String productName,
            int normalPrice,
            int stockQuantity,
            int orderQuantity
    ) {
        // 일반 상품 정보만 생성
        ProductDto normalProductDto = ProductDto.of(
                productName,
                String.valueOf(normalPrice),
                String.valueOf(stockQuantity),
                null
        );

        // Products 생성 (프로모션 없이)
        Products products = Products.from(
                List.of(normalProductDto),
                Promotions.from(List.of())
        );

        // OrderContext 생성
        List<OrderItemDto> orderItems = List.of(
                new OrderItemDto(productName, orderQuantity)
        );

        return OrderContext.of(LocalDateTime.now(), orderItems, products);
    }

    private InsufficientPromotionalStockHandler createHandler(boolean confirmNormalPrice) {
        return new InsufficientPromotionalStockHandler((name, quantity) -> confirmNormalPrice);
    }
}
