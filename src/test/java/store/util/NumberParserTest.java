package store.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static store.constant.ExceptionMessage.WRONG_INPUT;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NumberParserTest {

    @Nested
    class 숫자_파싱_성공_테스트 {
        @Test
        void 정수를_파싱한다() {
            assertThat(NumberParser.parse("123")).isEqualTo(123);
        }

        @Test
        void 앞뒤_공백이_있는_정수를_파싱한다() {
            assertThat(NumberParser.parse(" 123 ")).isEqualTo(123);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "0",
                "1",
                "999999999"  // 10자리 미만 최대값
        })
        void 다양한_범위의_정수를_파싱한다(String input) {
            // when
            Integer result = NumberParser.parse(input);

            // then
            assertThat(result).isEqualTo(Integer.parseInt(input));
        }
    }

    @Nested
    class 숫자_파싱_실패_테스트 {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        void null이거나_빈_문자열이면_예외가_발생한다(String input) {
            assertThatThrownBy(() -> NumberParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_INPUT.message());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "abc",           // 문자
                "12.34",        // 소수
                "123a",         // 숫자+문자
                "a123",         // 문자+숫자
                "12 34",        // 중간 공백
        })
        void 올바르지_않은_정수_형식이면_예외가_발생한다(String input) {
            assertThatThrownBy(() -> NumberParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_INPUT.message());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1234567890",       // 10자리
                "12345678901",      // 11자리
                "999999999999"      // 12자리
        })
        void 자릿수_초과시_예외가_발생한다(String input) {
            assertThatThrownBy(() -> NumberParser.parse(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(WRONG_INPUT.message());
        }
    }
}
