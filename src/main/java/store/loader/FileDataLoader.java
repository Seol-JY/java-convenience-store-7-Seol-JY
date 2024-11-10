package store.loader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import store.dto.FileDto;

public class FileDataLoader<T extends FileDto> {
    private static final String FACTORY_METHOD_NAME = "of";
    private static final String DELIMITER = ",";
    public static final String ERROR_EMPTY_FIELD = "필드가 비어있습니다.";
    private static final String ERROR_FILE_READ = "파일을 읽을 수 없습니다: ";
    private static final String ERROR_DTO_CONVERSION = "DTO 변환 중 오류가 발생했습니다.";
    private static final String ERROR_EMPTY_LINE = "빈 라인이 존재합니다.";
    private static final String ERROR_EMPTY_FILE = "파일이 비어있습니다.";

    private final Class<T> dtoClass;

    public FileDataLoader(final Class<T> dtoClass) {
        this.dtoClass = dtoClass;
    }

    public List<T> load(final String filePath) {
        List<String> lines = readLines(filePath);
        validateNotEmpty(lines);
        return parseLines(skipHeader(lines));
    }

    private List<String> readLines(final String filePath) {
        try {
            return Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(ERROR_FILE_READ + filePath);
        }
    }

    private List<T> parseLines(final List<String> lines) {
        return lines.stream()
                .map(this::parseLine)
                .collect(Collectors.toList());
    }

    private T parseLine(final String line) {
        validateLine(line);
        String[] fields = line.strip().split(DELIMITER);

        // 필드 검증 및 스트립 처리
        List<String> nonEmptyFields = validateAndStripFields(fields);

        // DTO 변환 처리
        return convertToDto(nonEmptyFields);
    }

    private List<String> validateAndStripFields(String[] fields) {
        return Arrays.stream(fields)
                .map(String::strip)  // 각 필드에서 공백 제거
                .peek(field -> {
                    if (field.isEmpty()) {
                        throw new IllegalArgumentException(ERROR_EMPTY_FIELD);
                    }
                })
                .toList();
    }

    private T convertToDto(List<String> nonEmptyFields) {
        try {
            Method ofMethod = Arrays.stream(dtoClass.getMethods())
                    .filter(method -> method.getName().equals(FACTORY_METHOD_NAME))
                    .filter(method -> method.getParameterCount() == nonEmptyFields.size())
                    .filter(method -> Arrays.stream(method.getParameterTypes())
                            .allMatch(type -> type.equals(String.class)))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException(ERROR_DTO_CONVERSION));

            @SuppressWarnings("unchecked")
            T dto = (T) ofMethod.invoke(null, (Object[]) nonEmptyFields.toArray(new String[0]));
            return dto;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(ERROR_DTO_CONVERSION, e);
        }
    }

    private void validateLine(final String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException(ERROR_EMPTY_LINE);
        }
    }


    private List<String> skipHeader(final List<String> lines) {
        return lines.stream()
                .skip(1)
                .toList();
    }

    private void validateNotEmpty(final List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(ERROR_EMPTY_FILE);
        }
    }
}
