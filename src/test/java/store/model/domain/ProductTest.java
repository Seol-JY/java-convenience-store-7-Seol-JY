package store.model.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import store.dto.PromotionFileDto;

class ProductTest {

    @Nested
    class 상품_생성_성공_테스트 {
        @Test
        void 일반_재고만_있는_상품을_생성한다() {
            // given
            String name = "상품명";
            ProductStock normalStock = ProductStock.of(1000, 10);

            // when
            Product product = Product.builder(name)
                    .normalStock(normalStock)
                    .build();

            // then
            assertThat(product).isNotNull();
        }

        @Test
        void 프로모션_상품을_생성한다() {
            // given
            String name = "상품명";
            ProductStock promotionalStock = ProductStock.of(1000, 10);
            Promotion promotion = createPromotion();

            // when
            Product product = Product.builder(name)
                    .promotionalStock(promotionalStock)
                    .promotion(promotion)
                    .build();

            // then
            assertThat(product).isNotNull();
        }

        @Test
        void 일반_재고와_프로모션_재고_모두_있는_상품을_생성한다() {
            // given
            String name = "상품명";
            ProductStock normalStock = ProductStock.of(1000, 10);
            ProductStock promotionalStock = ProductStock.of(800, 5);
            Promotion promotion = createPromotion();

            // when
            Product product = Product.builder(name)
                    .normalStock(normalStock)
                    .promotionalStock(promotionalStock)
                    .promotion(promotion)
                    .build();

            // then
            assertThat(product).isNotNull();
        }
    }

    @Nested
    class 상품_생성_실패_테스트 {
        @ParameterizedTest
        @NullAndEmptySource
        void 상품_이름이_null이거나_빈_문자열이면_예외가_발생한다(String name) {
            assertThatThrownBy(() -> Product.builder(name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("상품 이름은 필수입니다.");
        }

        @Test
        void 일반_재고와_프로모션_재고가_모두_없으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> Product.builder("상품명").build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("상품은 일반 재고와 프로모션 재고 중 적어도 하나는 있어야 합니다.");
        }

        @Test
        void 프로모션_재고만_있고_프로모션이_없으면_예외가_발생한다() {
            // given
            ProductStock promotionalStock = ProductStock.of(1000, 10);

            // when & then
            assertThatThrownBy(() -> Product.builder("상품명")
                    .promotionalStock(promotionalStock)
                    .build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("프로모션 재고가 있다면 프로모션이 반드시 있어야 합니다.");
        }
    }

    @Nested
    class 중복_재고_추가_테스트 {
        @Test
        void 일반_재고를_중복_추가하면_예외가_발생한다() {
            // given
            ProductStock normalStock = ProductStock.of(1000, 10);
            Product.Builder builder = Product.builder("상품명")
                    .normalStock(normalStock);

            // when & then
            assertThatThrownBy(() -> builder.normalStock(normalStock))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("동일한 상품에 대해 중복된 일반 상품이 존재합니다.");
        }

        @Test
        void 프로모션_재고를_중복_추가하면_예외가_발생한다() {
            // given
            ProductStock promotionalStock = ProductStock.of(1000, 10);
            Promotion promotion = createPromotion();
            Product.Builder builder = Product.builder("상품명")
                    .promotionalStock(promotionalStock)
                    .promotion(promotion);

            // when & then
            assertThatThrownBy(() -> builder.promotionalStock(promotionalStock))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("동일한 상품에 대해 중복된 프로모션 상품이 존재합니다.");
        }
    }

    private Promotion createPromotion() {
        PromotionFileDto dto = PromotionFileDto.of(
                "테스트 프로모션",
                "2",
                "1",
                "2024-01-01",
                "2024-12-31"
        );
        return Promotion.from(dto);
    }

    @Nested
    class 총_재고_계산_테스트 {
        private static final LocalDate TEST_DATE = LocalDate.of(2024, 6, 15);

        @Test
        void 일반_재고만_있는_경우_해당_수량을_반환한다() {
            // given
            ProductStock normalStock = ProductStock.of(1000, 10);
            Product product = Product.builder("상품")
                    .normalStock(normalStock)
                    .build();

            // when
            Integer totalStock = product.getTotalStock(TEST_DATE);

            // then
            assertThat(totalStock).isEqualTo(10);
        }

        @Test
        void 프로모션_기간_내_총_재고를_계산한다() {
            // given
            String promotionName = "테스트프로모션";
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);
            Promotion promotion = Promotion.from(
                    PromotionFileDto.of(
                            promotionName,
                            "2",
                            "1",
                            startDate.toString(),
                            endDate.toString()
                    )
            );

            Product product = Product.builder("상품")
                    .normalStock(ProductStock.of(1000, 10))
                    .promotionalStock(ProductStock.of(800, 5))
                    .promotion(promotion)
                    .build();

            // when
            Integer totalStock = product.getTotalStock(TEST_DATE);

            // then
            assertThat(totalStock).isEqualTo(15); // 일반 재고 10 + 프로모션 재고 5
        }

        @Test
        void 프로모션_기간이_아닌_경우_일반_재고만_계산한다() {
            // given
            String promotionName = "테스트프로모션";
            LocalDate startDate = LocalDate.of(2025, 1, 1);  // 미래의 프로모션
            LocalDate endDate = LocalDate.of(2025, 12, 31);
            Promotion promotion = Promotion.from(
                    PromotionFileDto.of(
                            promotionName,
                            "2",
                            "1",
                            startDate.toString(),
                            endDate.toString()
                    )
            );

            Product product = Product.builder("상품")
                    .normalStock(ProductStock.of(1000, 10))
                    .promotionalStock(ProductStock.of(800, 5))
                    .promotion(promotion)
                    .build();

            // when
            Integer totalStock = product.getTotalStock(TEST_DATE);

            // then
            assertThat(totalStock).isEqualTo(10); // 일반 재고만 포함
        }

        @Test
        void 프로모션_재고만_있고_프로모션_기간인_경우_해당_수량을_반환한다() {
            // given
            String promotionName = "테스트프로모션";
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);
            Promotion promotion = Promotion.from(
                    PromotionFileDto.of(
                            promotionName,
                            "2",
                            "1",
                            startDate.toString(),
                            endDate.toString()
                    )
            );

            Product product = Product.builder("상품")
                    .promotionalStock(ProductStock.of(800, 5))
                    .promotion(promotion)
                    .build();

            // when
            Integer totalStock = product.getTotalStock(TEST_DATE);

            // then
            assertThat(totalStock).isEqualTo(5);
        }

        @Test
        void 프로모션_재고만_있고_프로모션_기간이_아닌_경우_0을_반환한다() {
            // given
            String promotionName = "테스트프로모션";
            LocalDate startDate = LocalDate.of(2025, 1, 1);  // 미래의 프로모션
            LocalDate endDate = LocalDate.of(2025, 12, 31);
            Promotion promotion = Promotion.from(
                    PromotionFileDto.of(
                            promotionName,
                            "2",
                            "1",
                            startDate.toString(),
                            endDate.toString()
                    )
            );

            Product product = Product.builder("상품")
                    .promotionalStock(ProductStock.of(800, 5))
                    .promotion(promotion)
                    .build();

            // when
            Integer totalStock = product.getTotalStock(TEST_DATE);

            // then
            assertThat(totalStock).isZero();
        }
    }
}
