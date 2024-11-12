package store.model.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.dto.ProductDto;
import store.dto.PromotionFileDto;

class ProductsTest {

    @Nested
    class 상품_목록_생성_테스트 {
        @Test
        void 일반_상품만_있는_목록을_생성한다() {
            // given
            List<ProductDto> dtos = List.of(
                    ProductDto.of("상품1", "1000", "10", "null"),
                    ProductDto.of("상품2", "2000", "20", "null")
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
            List<ProductDto> dtos = List.of(
                    ProductDto.of("상품1", "1000", "10", "null"),
                    ProductDto.of("상품2", "2000", "20", promotionName)
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
            List<ProductDto> dtos = List.of(
                    ProductDto.of("동일상품", "1000", "10", "null"),
                    ProductDto.of("동일상품", "800", "5", promotionName)
            );

            // when
            Products products = Products.from(dtos, promotions);

            // then
            assertThat(products).isNotNull();
        }

        @Test
        void 빈_상품_목록을_생성한다() {
            // given
            List<ProductDto> dtos = List.of();
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
            List<ProductDto> dtos = List.of(
                    ProductDto.of("상품1", "1000", "10", nonExistentPromotion)
            );
            Promotions promotions = Promotions.from(List.of());

            // when & then
            assertThatThrownBy(() -> Products.from(dtos, promotions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("존재하지 않는 프로모션입니다: %s", nonExistentPromotion));
        }
    }

    @Nested
    class 상품_재고_수량_갱신_테스트 {
        @Test
        void 일반_상품의_수량을_갱신한다() {
            // given
            List<ProductDto> originalDtos = List.of(
                    ProductDto.of("상품1", "1000", "10", "null")
            );
            Products products = Products.from(originalDtos, Promotions.from(List.of()));

            List<ProductDto> updateTargetDtos = List.of(
                    ProductDto.of("상품1", "1000", "5", "null")
            );

            // when
            List<ProductDto> updatedDtos = products.updateDtoQuantities(updateTargetDtos);

            // then
            assertThat(updatedDtos)
                    .hasSize(1)
                    .element(0)
                    .extracting(ProductDto::quantity)
                    .isEqualTo(10); // 원래 재고량이 반영되어야 함
        }

        @Test
        void 프로모션_상품의_수량을_갱신한다() {
            // given
            String promotionName = "테스트프로모션";
            Promotions promotions = Promotions.from(List.of(createPromotion(promotionName)));
            List<ProductDto> originalDtos = List.of(
                    ProductDto.of("상품1", "1000", "10", promotionName)
            );
            Products products = Products.from(originalDtos, promotions);

            List<ProductDto> updateTargetDtos = List.of(
                    ProductDto.of("상품1", "1000", "5", promotionName)
            );

            // when
            List<ProductDto> updatedDtos = products.updateDtoQuantities(updateTargetDtos);

            // then
            assertThat(updatedDtos)
                    .hasSize(1)
                    .element(0)
                    .extracting(ProductDto::quantity)
                    .isEqualTo(10); // 원래 프로모션 재고량이 반영되어야 함
        }

        @Test
        void 존재하지_않는_상품_수량_갱신시_예외가_발생한다() {
            // given
            Products products = Products.from(List.of(), Promotions.from(List.of()));
            List<ProductDto> updateTargetDtos = List.of(
                    ProductDto.of("존재하지_않는_상품", "1000", "10", "null")
            );

            // when & then
            assertThatThrownBy(() -> products.updateDtoQuantities(updateTargetDtos))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("상품을 찾을 수 없습니다: 존재하지_않는_상품");
        }

        @Test
        void 유효하지_않은_프로모션으로_수량_갱신시_예외가_발생한다() {
            // given
            String promotionName = "테스트프로모션";
            Promotions promotions = Promotions.from(List.of(createPromotion(promotionName)));
            List<ProductDto> originalDtos = List.of(
                    ProductDto.of("상품1", "1000", "10", "null")  // 일반 상품으로 생성
            );
            Products products = Products.from(originalDtos, promotions);

            List<ProductDto> updateTargetDtos = List.of(
                    ProductDto.of("상품1", "1000", "5", promotionName)  // 프로모션 상품으로 조회 시도
            );

            // when & then
            assertThatThrownBy(() -> products.updateDtoQuantities(updateTargetDtos))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("유효하지 않은 프로모션 재고입니다.");
        }

        @Test
        void 여러_상품의_수량을_한번에_갱신한다() {
            // given
            String promotionName = "테스트프로모션";
            Promotions promotions = Promotions.from(List.of(createPromotion(promotionName)));
            List<ProductDto> originalDtos = List.of(
                    ProductDto.of("상품1", "1000", "10", "null"),
                    ProductDto.of("상품2", "2000", "20", promotionName)
            );
            Products products = Products.from(originalDtos, promotions);

            List<ProductDto> updateTargetDtos = List.of(
                    ProductDto.of("상품1", "1000", "5", "null"),
                    ProductDto.of("상품2", "2000", "15", promotionName)
            );

            // when
            List<ProductDto> updatedDtos = products.updateDtoQuantities(updateTargetDtos);

            // then
            assertThat(updatedDtos)
                    .hasSize(2)
                    .extracting(ProductDto::quantity)
                    .containsExactly(10, 20); // 각각 원래 재고량이 반영되어야 함
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
