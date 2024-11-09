package architecture.analyzer.test.rules;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import architecture.analyzer.main.CodeViolation;
import architecture.analyzer.main.rules.MethodParameterFinalRule;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("메서드 파라미터 final 키워드 검사 규칙")
class MethodParameterFinalRuleTest {

    private final MethodParameterFinalRule rule = new MethodParameterFinalRule();

    @Test
    void 모든_파라미터가_final이면_위반을_감지하지_않는다() {
        // given
        String code = """
                public class Test {
                    public void method(final String param1, final int param2) {
                        System.out.println(param1 + param2);
                    }
                }
                """;

        // when
        List<CodeViolation> violations = rule.analyze("Test.java", code.lines().toList());

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void final이_없는_파라미터가_있으면_위반을_감지한다() {
        // given
        String code = """
                public class Test {
                    public void method(final String param1, int param2) {
                        System.out.println(param1 + param2);
                    }
                }
                """;

        // when
        List<CodeViolation> violations = rule.analyze("Test.java", code.lines().toList());

        // then
        assertThat(violations)
                .hasSize(1)
                .extracting("message")
                .contains("파라미터 'param2'에 final 키워드가 없습니다");
    }

    @Test
    void 제외된_클래스의_메서드는_검사하지_않는다() {
        // given
        String code = """
                public class ExcludedTest {
                    public void method(String param1, int param2) {
                        System.out.println(param1 + param2);
                    }
                }
                """;

        // when
        List<CodeViolation> violations = new MethodParameterFinalRule()
                .exclude("ExcludedTest")
                .analyze("Test.java", code.lines().toList());

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 여러_클래스_중_제외된_클래스만_검사하지_않는다() {
        // given
        String code = """
                public class Test1 {
                    public void method(String param) {
                        System.out.println(param);
                    }
                }
                
                public class ExcludedTest {
                    public void method(String param) {
                        System.out.println(param);
                    }
                }
                
                public class Test2 {
                    public void method(String param) {
                        System.out.println(param);
                    }
                }
                """;

        // when
        List<CodeViolation> violations = new MethodParameterFinalRule()
                .exclude("ExcludedTest")
                .analyze("Test.java", code.lines().toList());

        // then
        assertThat(violations)
                .hasSize(2)
                .extracting("message")
                .containsExactly(
                        "파라미터 'param'에 final 키워드가 없습니다",
                        "파라미터 'param'에 final 키워드가 없습니다"
                );
    }
}
