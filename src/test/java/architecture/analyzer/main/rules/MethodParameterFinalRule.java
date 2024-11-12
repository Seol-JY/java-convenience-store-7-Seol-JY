package architecture.analyzer.main.rules;

import architecture.analyzer.main.CodeViolation;
import architecture.analyzer.main.utils.LineUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
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

            updateCurrentClassName(line);

            if (!LineUtils.isMethodStart(line) || isExcludedClass()) {
                continue;
            }

            checkMethodParameters(fileName, line, i, violations);
        }

        return violations;
    }

    private void updateCurrentClassName(String line) {
        Matcher classMatcher = CLASS_PATTERN.matcher(line);
        if (!classMatcher.find()) {
            return;
        }
        currentClassName = classMatcher.group(3);
    }

    private boolean isExcludedClass() {
        return currentClassName != null && excludedClasses.contains(currentClassName);
    }

    private void checkMethodParameters(String fileName, String line, int lineNumber, List<CodeViolation> violations) {
        Matcher matcher = METHOD_PATTERN.matcher(line);
        if (!matcher.find()) {
            return;
        }

        String parameters = matcher.group(3).trim();
        if (parameters.isEmpty()) {
            return;
        }

        List<String> paramList = parseParameters(parameters);
        for (String param : paramList) {
            if (param.startsWith("final ")) {
                continue;
            }

            String paramName = extractParameterName(param);
            violations.add(new CodeViolation(
                    fileName,
                    lineNumber + 1,
                    String.format("파라미터 '%s'에 final 키워드가 없습니다", paramName)
            ));
        }
    }

    private List<String> parseParameters(String parameters) {
        List<String> paramList = new ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        Stack<Character> genericStack = new Stack<>();

        for (char c : parameters.toCharArray()) {
            if (shouldPushToGenericStack(c)) {
                genericStack.push(c);
                currentParam.append(c);
                continue;
            }

            if (shouldPopFromGenericStack(c, genericStack)) {
                genericStack.pop();
                currentParam.append(c);
                continue;
            }

            if (shouldAddParameterToList(c, genericStack, currentParam)) {
                paramList.add(currentParam.toString().trim());
                currentParam = new StringBuilder();
                continue;
            }

            currentParam.append(c);
        }

        addRemainingParameter(currentParam, paramList);
        return paramList;
    }

    private boolean shouldPushToGenericStack(char c) {
        return c == '<';
    }

    private boolean shouldPopFromGenericStack(char c, Stack<Character> genericStack) {
        return c == '>' && !genericStack.isEmpty();
    }

    private boolean shouldAddParameterToList(char c, Stack<Character> genericStack, StringBuilder currentParam) {
        return c == ',' && genericStack.isEmpty() && currentParam.length() > 0;
    }

    private void addRemainingParameter(StringBuilder currentParam, List<String> paramList) {
        if (currentParam.length() > 0) {
            paramList.add(currentParam.toString().trim());
        }
    }

    private String extractParameterName(String param) {
        String processedParam = removeGenericPart(param);
        String[] parts = processedParam.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    private String removeGenericPart(String param) {
        int genericStart = param.indexOf('<');
        int genericEnd = param.lastIndexOf('>');

        if (genericStart == -1 || genericEnd == -1) {
            return param;
        }

        return param.substring(0, genericStart) + param.substring(genericEnd + 1);
    }
}
