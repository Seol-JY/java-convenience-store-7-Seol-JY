package store.model.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.dto.PromotionFileDto;

class PromotionTest {

    @Nested
    class 행사_정보_생성_테스트 {
        @Test
        void DTO로부터_행사_정보를_생성한다() {
            // given
            PromotionFileDto dto = createValidPromotionDto();

            // when & then
            Promotion.from(dto); // 예외가 발생하지 않아야 함
        }
    }

    @Nested
    class 구매_수량_검증_테스트 {
        @Test
        void 구매_수량이_1개_미만이면_예외를_발생시킨다() {
            // given
            PromotionFileDto dto = createPromotionDtoWithInvalidBuyQuantity();

            // when & then
            assertThatThrownBy(() -> Promotion.from(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("구매 수량은 1개 이상이어야 합니다.");
        }

        @Test
        void 구매_수량이_null이면_예외를_발생시킨다() {
            // given
            PromotionFileDto dto = createPromotionDtoWithNullBuyQuantity();

            // when & then
            assertThatThrownBy(() -> Promotion.from(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("구매 수량은 1개 이상이어야 합니다.");
        }
    }

    @Nested
    class 증정_수량_검증_테스트 {
        @Test
        void 증정_수량이_1개_미만이면_예외를_발생시킨다() {
            // given
            PromotionFileDto dto = createPromotionDtoWithInvalidGetQuantity();

            // when & then
            assertThatThrownBy(() -> Promotion.from(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("증정 수량은 1개 이상이어야 합니다.");
        }

        @Test
        void 증정_수량이_null이면_예외를_발생시킨다() {
            // given
            PromotionFileDto dto = createPromotionDtoWithNullGetQuantity();

            // when & then
            assertThatThrownBy(() -> Promotion.from(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("증정 수량은 1개 이상이어야 합니다.");
        }
    }

    private PromotionFileDto createValidPromotionDto() {
        return new PromotionFileDto(
                "테스트 프로모션",
                2,
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    private PromotionFileDto createPromotionDtoWithInvalidBuyQuantity() {
        return new PromotionFileDto(
                "테스트 프로모션",
                0,  // 유효하지 않은 구매 수량
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    private PromotionFileDto createPromotionDtoWithNullBuyQuantity() {
        return new PromotionFileDto(
                "테스트 프로모션",
                null,  // null 구매 수량
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    private PromotionFileDto createPromotionDtoWithInvalidGetQuantity() {
        return new PromotionFileDto(
                "테스트 프로모션",
                2,
                0,  // 유효하지 않은 증정 수량
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

    private PromotionFileDto createPromotionDtoWithNullGetQuantity() {
        return new PromotionFileDto(
                "테스트 프로모션",
                2,
                null,  // null 증정 수량
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }
}
