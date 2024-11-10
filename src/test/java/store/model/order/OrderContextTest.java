package store.model.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.constant.ExceptionMessage.PRODUCT_NOT_FOUND;
import static store.constant.ExceptionMessage.WRONG_ORDER_INPUT;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.dto.OrderItemDto;
import store.dto.ProductDto;
import store.model.domain.Products;
import store.model.domain.Promotions;

class OrderContextTest {

    private Products products;

    @BeforeEach
    void setUp() {
        List<ProductDto> productDtos = List.of(
                ProductDto.of("상품A", "1000", "10", "null"),
                ProductDto.of("상품B", "2000", "20", "null")
        );
        products = Products.from(productDtos, Promotions.from(List.of()));
    }

    @Nested
    class 주문_컨텍스트_생성_테스트 {
        @Test
        void 정상적인_주문을_생성한다() {
            // given
            List<OrderItemDto> items = List.of(
                    OrderItemDto.of("상품A", 1),
                    OrderItemDto.of("상품B", 2)
            );

            // when
            OrderContext orderContext = OrderContext.of(LocalDate.now(), items, products);

            // then
            assertThat(orderContext).isNotNull();
        }

        @Test
        void 동일_상품에_대한_주문은_수량이_합산된다() {
            // given
            List<OrderItemDto> items = List.of(
                    OrderItemDto.of("상품A", 1),
                    OrderItemDto.of("상품A", 2)
            );

            // when
            OrderContext orderContext = OrderContext.of(LocalDate.now(), items, products);

            // then
            assertThat(orderContext).isNotNull();
        }
    }

    @Nested
    class 주문_검증_테스트 {
        @Test
        void 존재하지_않는_상품_주문시_예외가_발생한다() {
            // given
            List<OrderItemDto> items = List.of(
                    OrderItemDto.of("존재하지_않는_상품", 1)
            );

            // when & then
            assertThatThrownBy(() -> OrderContext.of(LocalDate.now(), items, products))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(PRODUCT_NOT_FOUND.message());
        }

        @Test
        void 주문_수량이_0이하면_예외가_발생한다() {
            // given
            List<OrderItemDto> items = List.of(
                    OrderItemDto.of("상품A", 0)
            );

            // when & then
            assertThatThrownBy(() -> OrderContext.of(LocalDate.now(), items, products))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_ORDER_INPUT.message());
        }

        @Test
        void 주문_수량이_음수이면_예외가_발생한다() {
            // given
            List<OrderItemDto> items = List.of(
                    OrderItemDto.of("상품A", -1)
            );

            // when & then
            assertThatThrownBy(() -> OrderContext.of(LocalDate.now(), items, products))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_ORDER_INPUT.message());
        }
    }

    @Nested
    class 복합_주문_테스트 {
        @Test
        void 여러_상품의_정상_주문과_예외_상황을_처리한다() {
            // given
            List<OrderItemDto> items = List.of(
                    OrderItemDto.of("상품A", 1),
                    OrderItemDto.of("상품B", 2),
                    OrderItemDto.of("상품A", 3)  // 동일 상품 추가 주문
            );

            // when
            OrderContext orderContext = OrderContext.of(LocalDate.now(), items, products);

            // then
            assertThat(orderContext).isNotNull();
        }
    }
}
