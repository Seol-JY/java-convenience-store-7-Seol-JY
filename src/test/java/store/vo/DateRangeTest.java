package store.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DateRangeTest {

    @Nested
    class 날짜_범위_생성_테스트 {
        @Test
        void 시작일과_종료일로_날짜_범위를_생성한다() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // when
            DateRange dateRange = DateRange.of(startDate, endDate);

            // then
            assertThat(dateRange.startDate()).isEqualTo(startDate);
            assertThat(dateRange.endDate()).isEqualTo(endDate);
        }

        @Test
        void 시작일과_종료일이_같은_날짜_범위를_생성한다() {
            // given
            LocalDate date = LocalDate.of(2024, 1, 1);

            // when
            DateRange dateRange = DateRange.of(date, date);

            // then
            assertThat(dateRange.startDate()).isEqualTo(date);
            assertThat(dateRange.endDate()).isEqualTo(date);
        }
    }

    @Nested
    class 날짜_범위_검증_테스트 {
        @Test
        void 종료일이_시작일보다_이전이면_예외를_발생시킨다() {
            // given
            LocalDate startDate = LocalDate.of(2024, 12, 31);
            LocalDate endDate = LocalDate.of(2024, 1, 1);

            // when & then
            assertThatThrownBy(() -> DateRange.of(startDate, endDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("시작일은 종료일보다 이전이어야 합니다.");
        }

        @Test
        void 시작일이_null이면_예외를_발생시킨다() {
            // given
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // when & then
            assertThatThrownBy(() -> DateRange.of(null, endDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("시작일은 종료일보다 이전이어야 합니다.");
        }

        @Test
        void 종료일이_null이면_예외를_발생시킨다() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 1);

            // when & then
            assertThatThrownBy(() -> DateRange.of(startDate, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("시작일은 종료일보다 이전이어야 합니다.");
        }
    }

    @Nested
    class 날짜_범위_동등성_테스트 {
        @Test
        void 동일한_시작일과_종료일을_가진_날짜_범위는_같다() {
            // given
            DateRange dateRange1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            DateRange dateRange2 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );

            // when & then
            assertThat(dateRange1).isEqualTo(dateRange2);
            assertThat(dateRange1.hashCode()).isEqualTo(dateRange2.hashCode());
        }

        @Test
        void 다른_시작일을_가진_날짜_범위는_다르다() {
            // given
            DateRange dateRange1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            DateRange dateRange2 = DateRange.of(
                    LocalDate.of(2024, 2, 1),
                    LocalDate.of(2024, 12, 31)
            );

            // when & then
            assertThat(dateRange1).isNotEqualTo(dateRange2);
        }

        @Test
        void 다른_종료일을_가진_날짜_범위는_다르다() {
            // given
            DateRange dateRange1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            DateRange dateRange2 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 11, 30)
            );

            // when & then
            assertThat(dateRange1).isNotEqualTo(dateRange2);
        }
    }
}