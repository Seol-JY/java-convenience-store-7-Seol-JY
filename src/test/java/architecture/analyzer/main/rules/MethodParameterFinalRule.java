package architecture.analyzer.main.rules;

import architecture.analyzer.main.CodeViolation;
import architecture.analyzer.main.utils.LineUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodParameterFinalRule implements CodeStyleRule {
    private static final Pattern METHOD_PATTERN =
            Pattern.compile("\\s*(public|private|protected)?\\s*(static)?\\s*\\w+\\s+\\w+\\s*\\((.*)\\)");
    private static final Pattern CLASS_PATTERN =
            Pattern.compile("\\s*(public|private|protected)?\\s*(static)?\\s*class\\s+(\\w+)");

    private final Set<String> excludedClasses;
    private String currentClassName;

    public MethodParameterFinalRule() {
        this.excludedClasses = new HashSet<>();
    }

    public MethodParameterFinalRule exclude(String... classNames) {
        for (String className : classNames) {
            excludedClasses.add(className);
        }
        return this;
    }

    @Override
    public List<CodeViolation> analyze(String fileName, List<String> lines) {
        List<CodeViolation> violations = new ArrayList<>();
        currentClassName = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                currentClassName = classMatcher.group(3);
            }

            if (LineUtils.isMethodStart(line) && !isExcludedClass()) {
                checkMethodParameters(fileName, line, i, violations);
            }
        }

        return violations;
    }

    private boolean isExcludedClass() {
        return currentClassName != null && excludedClasses.contains(currentClassName);
    }

    private void checkMethodParameters(String fileName, String line, int lineNumber, List<CodeViolation> violations) {
        Matcher matcher = METHOD_PATTERN.matcher(line);

        if (matcher.find()) {
            String parameters = matcher.group(3).trim();

            if (!parameters.isEmpty()) {
                List<String> paramList = parseParameters(parameters);

                for (String param : paramList) {
                    if (!param.startsWith("final ")) {
                        violations.add(new CodeViolation(
                                fileName,
                                lineNumber + 1,
                                String.format("파라미터 '%s'에 final 키워드가 없습니다", param.split("\\s+")[1])
                        ));
                    }
                }
            }
        }
    }

    private List<String> parseParameters(String parameters) {
        List<String> paramList = new ArrayList<>();
        String[] params = parameters.split(",");

        for (String param : params) {
            param = param.trim();
            if (!param.isEmpty()) {
                paramList.add(param);
            }
        }

        return paramList;
    }
}
