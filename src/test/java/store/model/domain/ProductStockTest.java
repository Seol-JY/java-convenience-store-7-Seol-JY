package store.model.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductStockTest {

    @Nested
    class 상품_재고_생성_테스트 {
        @Test
        void 가격과_수량으로_상품_재고를_생성한다() {
            // given
            int price = 1000;
            int quantity = 10;

            // when
            ProductStock stock = ProductStock.of(price, quantity);

            // then
            assertThat(stock.getPrice()).isEqualTo(price);
            assertThat(stock.getQuantity()).isEqualTo(quantity);
        }

        @Test
        void 가격과_수량이_0인_상품_재고를_생성한다() {
            // given
            int price = 0;
            int quantity = 0;

            // when
            ProductStock stock = ProductStock.of(price, quantity);

            // then
            assertThat(stock.getPrice()).isZero();
            assertThat(stock.getQuantity()).isZero();
        }
    }

    @Nested
    class 가격_유효성_검증_테스트 {
        @Test
        void 가격이_null이면_예외를_발생시킨다() {
            // when & then
            assertThatThrownBy(() -> ProductStock.of(null, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("가격은 0 이상의 정수여야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -1000})
        void 가격이_음수이면_예외를_발생시킨다(int price) {
            // when & then
            assertThatThrownBy(() -> ProductStock.of(price, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("가격은 0 이상의 정수여야 합니다.");
        }
    }

    @Nested
    class 수량_유효성_검증_테스트 {
        @Test
        void 수량이_null이면_예외를_발생시킨다() {
            // when & then
            assertThatThrownBy(() -> ProductStock.of(1000, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("수량은 0 이상의 정수여야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -1000})
        void 수량이_음수이면_예외를_발생시킨다(int quantity) {
            // when & then
            assertThatThrownBy(() -> ProductStock.of(1000, quantity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("수량은 0 이상의 정수여야 합니다.");
        }
    }
}
