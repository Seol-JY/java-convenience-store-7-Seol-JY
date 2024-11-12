package store.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static store.constant.ExceptionMessage.WRONG_ORDER_INPUT;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import store.dto.OrderItemDto;

class OrderParserTest {

    @Nested
    class 주문_형식_검증_테스트 {
        @Test
        void 올바른_형식의_주문을_파싱한다() {
            // given
            String input = "[상품A-1], [상품B-2]";

            // when
            List<OrderItemDto> result = OrderParser.parse(input);

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(OrderItemDto::name, OrderItemDto::quantity)
                    .containsExactly(
                            tuple("상품A", 1),
                            tuple("상품B", 2)
                    );
        }

        @Test
        void 단일_주문을_파싱한다() {
            // given
            String input = "[상품A-1]";

            // when
            List<OrderItemDto> result = OrderParser.parse(input);

            // then
            assertThat(result)
                    .hasSize(1)
                    .extracting(OrderItemDto::name, OrderItemDto::quantity)
                    .containsExactly(
                            tuple("상품A", 1)
                    );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        void 빈_입력이나_공백은_예외를_발생시킨다(String input) {
            assertThatThrownBy(() -> OrderParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_ORDER_INPUT.message());
        }
    }

    @Nested
    class 대괄호_형식_검증_테스트 {
        @Test
        void 대괄호가_없으면_예외를_발생시킨다() {
            // given
            String input = "상품A-1, 상품B-2";

            // when & then
            assertThatThrownBy(() -> OrderParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_ORDER_INPUT.message());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "]상품A-1[",          // 대괄호 순서가 반대
                "[상품A-1",           // 닫는 대괄호 누락
                "상품A-1]",           // 여는 대괄호 누락
                "[[상품A-1]]",        // 중첩된 대괄호
                "[상품A-1], 상품B-2"  // 일부 항목만 대괄호
        })
        void 잘못된_대괄호_형식은_예외를_발생시킨다(String input) {
            assertThatThrownBy(() -> OrderParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_ORDER_INPUT.message());
        }
    }

    @Nested
    class 하이픈_구분자_검증_테스트 {
        @ParameterizedTest
        @ValueSource(strings = {
                "[상품A=1]",           // 잘못된 구분자
                "[상품A:1]",           // 잘못된 구분자
                "[상품A1]",            // 구분자 누락
                "[상품A--1]",          // 중복 하이픈
                "[상품A-1-2]"          // 추가 하이픈
        })
        void 잘못된_하이픈_형식은_예외를_발생시킨다(String input) {
            assertThatThrownBy(() -> OrderParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_ORDER_INPUT.message());
        }

        @Test
        void 하이픈_앞뒤_공백은_허용한다() {
            // given
            String input = "[상품A - 1], [상품B - 2]";

            // when
            List<OrderItemDto> result = OrderParser.parse(input);

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(OrderItemDto::name, OrderItemDto::quantity)
                    .containsExactly(
                            tuple("상품A", 1),
                            tuple("상품B", 2)
                    );
        }
    }

    @Nested
    class 쉼표_구분자_검증_테스트 {
        @ParameterizedTest
        @ValueSource(strings = {
                "[상품A-1];[상품B-2]",     // 잘못된 구분자
                "[상품A-1]&[상품B-2]",     // 잘못된 구분자
                "[상품A-1][상품B-2]",      // 구분자 누락
                "[상품A-1],,[상품B-2]",    // 중복 쉼표
                "[상품A-1],[상품B-2],",     // 끝에 추가 쉼표
                ",[상품A-1],[상품B-2]"     // 시작에 추가 쉼표
        })
        void 잘못된_쉼표_형식은_예외를_발생시킨다(String input) {
            assertThatThrownBy(() -> OrderParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_ORDER_INPUT.message());
        }

        @Test
        void 쉼표_앞뒤_공백은_허용한다() {
            // given
            String input = "[상품A-1] , [상품B-2]";

            // when
            List<OrderItemDto> result = OrderParser.parse(input);

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(OrderItemDto::name, OrderItemDto::quantity)
                    .containsExactly(
                            tuple("상품A", 1),
                            tuple("상품B", 2)
                    );
        }
    }

    @Test
    void 복합_케이스_검증() {
        // given
        String input = "[상품A - 1] , [상품B - 2] , [상품C - 3]";

        // when
        List<OrderItemDto> result = OrderParser.parse(input);

        // then
        assertThat(result)
                .hasSize(3)
                .extracting(OrderItemDto::name, OrderItemDto::quantity)
                .containsExactly(
                        tuple("상품A", 1),
                        tuple("상품B", 2),
                        tuple("상품C", 3)
                );
    }
}
