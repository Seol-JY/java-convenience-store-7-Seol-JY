package store.model.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.dto.ProductFileDto;
import store.dto.PromotionFileDto;

class ProductsTest {

    @Nested
    class 상품_목록_생성_테스트 {
        @Test
        void 일반_상품만_있는_목록을_생성한다() {
            // given
            List<ProductFileDto> dtos = List.of(
                    ProductFileDto.of("상품1", "1000", "10", "null"),
                    ProductFileDto.of("상품2", "2000", "20", "null")
            );
            Promotions promotions = Promotions.from(List.of());

            // when
            Products products = Products.from(dtos, promotions);

            // then
            assertThat(products).isNotNull();
        }

        @Test
        void 프로모션_상품이_포함된_목록을_생성한다() {
            // given
            String promotionName = "테스트프로모션";
            Promotions promotions = Promotions.from(List.of(createPromotion(promotionName)));
            List<ProductFileDto> dtos = List.of(
                    ProductFileDto.of("상품1", "1000", "10", "null"),
                    ProductFileDto.of("상품2", "2000", "20", promotionName)
            );

            // when
            Products products = Products.from(dtos, promotions);

            // then
            assertThat(products).isNotNull();
        }

        @Test
        void 동일_상품의_일반_재고와_프로모션_재고를_모두_가진_목록을_생성한다() {
            // given
            String promotionName = "테스트프로모션";
            Promotions promotions = Promotions.from(List.of(createPromotion(promotionName)));
            List<ProductFileDto> dtos = List.of(
                    ProductFileDto.of("동일상품", "1000", "10", "null"),
                    ProductFileDto.of("동일상품", "800", "5", promotionName)
            );

            // when
            Products products = Products.from(dtos, promotions);

            // then
            assertThat(products).isNotNull();
        }

        @Test
        void 빈_상품_목록을_생성한다() {
            // given
            List<ProductFileDto> dtos = List.of();
            Promotions promotions = Promotions.from(List.of());

            // when
            Products products = Products.from(dtos, promotions);

            // then
            assertThat(products).isNotNull();
        }
    }

    @Nested
    class 프로모션_검증_테스트 {
        @Test
        void 존재하지_않는_프로모션으로_상품을_생성하면_예외가_발생한다() {
            // given
            String nonExistentPromotion = "존재하지_않는_프로모션";
            List<ProductFileDto> dtos = List.of(
                    ProductFileDto.of("상품1", "1000", "10", nonExistentPromotion)
            );
            Promotions promotions = Promotions.from(List.of());

            // when & then
            assertThatThrownBy(() -> Products.from(dtos, promotions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("존재하지 않는 프로모션입니다: %s", nonExistentPromotion));
        }
    }

    private Promotion createPromotion(String name) {
        return Promotion.from(
                PromotionFileDto.of(
                        name,
                        "2",
                        "1",
                        "2024-01-01",
                        "2024-12-31"
                )
        );
    }
}
