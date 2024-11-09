package store.model.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.dto.PromotionFileDto;

class PromotionsTest {

    @Nested
    class 프로모션_목록_생성_테스트 {
        @Test
        void 중복되지_않은_이름의_프로모션_목록을_생성한다() {
            // given
            List<Promotion> promotions = List.of(
                    createPromotion("프로모션1"),
                    createPromotion("프로모션2"),
                    createPromotion("프로모션3")
            );

            // when & then
            Promotions.from(promotions); // 예외가 발생하지 않아야 함
        }

        @Test
        void 빈_프로모션_목록을_생성한다() {
            // given
            List<Promotion> promotions = List.of();

            // when & then
            Promotions.from(promotions); // 예외가 발생하지 않아야 함
        }
    }

    @Nested
    class 프로모션_이름_중복_검증_테스트 {
        @Test
        void 중복된_이름의_프로모션이_존재하면_예외를_발생시킨다() {
            // given
            List<Promotion> promotions = List.of(
                    createPromotion("중복_프로모션"),
                    createPromotion("프로모션2"),
                    createPromotion("중복_프로모션")
            );

            // when & then
            assertThatThrownBy(() -> Promotions.from(promotions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("중복된 이름의 프로모션이 존재합니다.");
        }

        @Test
        void 여러개의_중복된_이름이_존재하면_예외를_발생시킨다() {
            // given
            List<Promotion> promotions = List.of(
                    createPromotion("중복1"),
                    createPromotion("중복1"),
                    createPromotion("중복2"),
                    createPromotion("중복2")
            );

            // when & then
            assertThatThrownBy(() -> Promotions.from(promotions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("중복된 이름의 프로모션이 존재합니다.");
        }
    }

    private Promotion createPromotion(String name) {
        return Promotion.from(new PromotionFileDto(
                name,
                2,
                1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        ));
    }
}
