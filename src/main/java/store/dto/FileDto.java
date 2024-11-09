package store.dto;

public interface FileDto {
    String ERROR_OF_METHOD = "각 구현 클래스에서 of 메서드를 정의해야 합니다.";

    static FileDto of(String[] fields) {
        throw new UnsupportedOperationException(ERROR_OF_METHOD);
    }
}
