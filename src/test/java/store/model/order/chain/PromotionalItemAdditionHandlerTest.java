package store.model.order.chain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import store.dto.OrderItemDto;
import store.dto.ProductDto;
import store.dto.PromotionFileDto;
import store.model.domain.Product;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;
import store.model.order.OrderContext;

class PromotionalItemAdditionHandlerTest {

    @Nested
    class 프로모션_상품_추가_테스트 {

        @Test
        void 프로모션_기준을_정확히_충족하는_경우_추가_상품을_제공한다() {
            // given
            OrderContext orderContext = createOrderContext(
                    "상품A", 2, 1, 1000, 10, 2  // 2+1 프로모션, 2개 구매
            );
            PromotionalItemAdditionHandler handler = createHandler(true);

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(3);
        }

        @ParameterizedTest(name = "{0}개 구매시 {1}개 증정 ({2}+{3}행사)")
        @MethodSource("여러_세트_프로모션_케이스")
        void 여러_세트의_프로모션_주문시_정확한_수량을_추가한다(
                int orderQuantity, int expectedAddition, int buy, int get) {
            // given
            OrderContext orderContext = createOrderContext(
                    "상품A", buy, get, 1000, 10, orderQuantity
            );
            PromotionalItemAdditionHandler handler = createHandler(true);

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum())
                    .isEqualTo(orderQuantity + expectedAddition);
        }

        @Test
        void 프로모션_재고_부족시_추가_상품을_제공하지_않는다() {
            // given
            OrderContext orderContext = createOrderContext(
                    "상품A", 2, 1, 1000, 0, 2  // 재고 0개
            );
            PromotionalItemAdditionHandler handler = createHandler(true);

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(2);
        }

        @Test
        void 사용자가_거절하면_추가_상품을_제공하지_않는다() {
            // given
            OrderContext orderContext = createOrderContext(
                    "상품A", 2, 1, 1000, 10, 2
            );
            PromotionalItemAdditionHandler handler = createHandler(false);

            // when
            handler.process(orderContext);

            // then
            Map<Product, Integer> orderItems = orderContext.getOrderItems();
            assertThat(orderItems.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(2);
        }

        private static Stream<Arguments> 여러_세트_프로모션_케이스() {
            return Stream.of(
                    Arguments.of(6, 0, 2, 1),
                    Arguments.of(9, 1, 1, 1),
                    Arguments.of(8, 1, 2, 1),
                    Arguments.of(7, 1, 2, 2),
                    Arguments.of(6, 0, 3, 1),
                    Arguments.of(11, 0, 3, 1),
                    Arguments.of(10, 0, 3, 2),
                    Arguments.of(10, 0, 3, 2),
                    Arguments.of(9, 0, 3, 1)
            );
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

    private PromotionalItemAdditionHandler createHandler(boolean shouldConfirm) {
        return new PromotionalItemAdditionHandler((name, quantity) -> shouldConfirm);
    }
}
