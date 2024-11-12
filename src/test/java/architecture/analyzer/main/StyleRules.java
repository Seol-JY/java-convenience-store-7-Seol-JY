package architecture.analyzer.main;

import architecture.analyzer.main.rules.IndentDepthRule;
import architecture.analyzer.main.rules.MethodParameterFinalRule;
import architecture.analyzer.main.rules.MethodSizeRule;
import architecture.analyzer.main.rules.NoElseRule;
import architecture.analyzer.main.rules.NoSwitchRule;
import architecture.analyzer.main.rules.TernaryOperatorRule;

public class StyleRules {
    public static IndentDepthRule indentDepth(int maxDepth) {
        return new IndentDepthRule(maxDepth);
    }

    public static TernaryOperatorRule noTernaryOperator() {
        return new TernaryOperatorRule();
    }

    public static MethodSizeRule methodSize(int maxLines) {
        return new MethodSizeRule(maxLines);
    }

    public static NoElseRule noElse() {
        return new NoElseRule();
    }

    public static NoSwitchRule noSwitch() {
        return new NoSwitchRule();
    }

    public static MethodParameterFinalRuleBuilder methodParameterFinal() {
        return new MethodParameterFinalRuleBuilder();
    }

    public static class MethodParameterFinalRuleBuilder {
        private final MethodParameterFinalRule rule;

        private MethodParameterFinalRuleBuilder() {
            this.rule = new MethodParameterFinalRule();
        }

        public MethodParameterFinalRuleBuilder exclude(String... classNames) {
            rule.exclude(classNames);
            return this;
        }

        public MethodParameterFinalRule build() {
            return rule;
        }
    }
}
