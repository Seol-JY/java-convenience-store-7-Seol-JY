package store.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import store.dto.FileDto;

class FileDataLoaderTest {

    @Nested
    class 파일_데이터_로드_테스트 {
        @Test
        void Markdown_형식의_파일을_DTO_리스트로_변환한다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    name,age,location
                    John,30,New York
                    Jane,25,London
                    """);
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when
            List<TestDto> result = loader.load(mdFile.toString());

            // then
            assertThat(result)
                    .hasSize(2)
                    .containsExactly(
                            TestDto.of("John", "30", "New York"),
                            TestDto.of("Jane", "25", "London")
                    );
        }

        @Test
        void 필드의_앞뒤_공백을_제거하고_로드한다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header1,header2,header3
                      data1  ,  data2  ,  data3  
                    """);
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when
            List<TestDto> result = loader.load(mdFile.toString());

            // then
            assertThat(result)
                    .hasSize(1)
                    .containsExactly(TestDto.of("data1", "data2", "data3"));
        }
    }

    @Nested
    class 필드_검증_테스트 {
        @Test
        void 필드_개수가_부족하면_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header1,header2,header3
                    data1,data2
                    """);
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("DTO 변환 중 오류가 발생했습니다.");
        }

        @Test
        void 필드_개수가_초과하면_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header1,header2,header3
                    data1,data2,data3,data4
                    """);
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("DTO 변환 중 오류가 발생했습니다.");
        }

        @Test
        void 빈_필드가_있는_경우_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header1,header2,header3
                    data1,,data3
                    """);
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("필드가 비어있습니다.");
        }

        @Test
        void 공백만_있는_필드는_빈_필드로_처리한다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header1,header2,header3
                    data1,   ,data3
                    """);
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("필드가 비어있습니다.");
        }
    }

    @Nested
    class 파일_구조_검증_테스트 {
        @Test
        void 빈_파일은_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path emptyFile = tempDir.resolve("empty.md");
            Files.writeString(emptyFile, "");
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(emptyFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("파일이 비어있습니다.");
        }

        @Test
        void 존재하지_않는_파일은_예외를_발생시킨다() {
            // given
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load("nonexistent.md"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageStartingWith("파일을 읽을 수 없습니다: ");
        }

        @Test
        void 빈_라인이_있는_경우_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header1,header2,header3
                    data1,data2,data3
                    
                    data4,data5,data6
                    """);
            FileDataLoader<TestDto> loader = new FileDataLoader<>(TestDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("빈 라인이 존재합니다.");
        }
    }

    @Nested
    class DTO_변환_테스트 {
        @Test
        void 잘못된_DTO_클래스는_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header
                    data
                    """);
            FileDataLoader<InvalidDto> loader = new FileDataLoader<>(InvalidDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("DTO 변환 중 오류가 발생했습니다.");
        }

        @Test
        void of_메서드가_없는_DTO는_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    header1,header2,header3
                    value1,value2,value3
                    """);
            FileDataLoader<NoFactoryMethodDto> loader = new FileDataLoader<>(NoFactoryMethodDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("DTO 변환 중 오류가 발생했습니다.");
        }
    }

    // 테스트용 DTO 클래스들
    static class TestDto implements FileDto {
        private final String field1;
        private final String field2;
        private final String field3;

        private TestDto(String field1, String field2, String field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        public static TestDto of(
                String field1,
                String field2,
                String field3
        ) {
            return new TestDto(field1, field2, field3);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TestDto other)) {
                return false;
            }
            return field1.equals(other.field1) &&
                    field2.equals(other.field2) &&
                    field3.equals(other.field3);
        }
    }

    @Nested
    class 메서드_인자수_검증_테스트 {
        @Test
        void 메서드_인자수가_적으면_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    name,price,quantity,promotion
                    상품,1000,10,행사상품
                    """);
            FileDataLoader<ThreeParamDto> loader = new FileDataLoader<>(ThreeParamDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("DTO 변환 중 오류가 발생했습니다.");
        }

        @Test
        void 메서드_인자수가_많으면_예외를_발생시킨다(@TempDir Path tempDir) throws IOException {
            // given
            Path mdFile = tempDir.resolve("test.md");
            Files.writeString(mdFile, """
                    name,price
                    상품,1000
                    """);
            FileDataLoader<ThreeParamDto> loader = new FileDataLoader<>(ThreeParamDto.class);

            // when & then
            assertThatThrownBy(() -> loader.load(mdFile.toString()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("DTO 변환 중 오류가 발생했습니다.");
        }

        static class ThreeParamDto implements FileDto {
            private final String field1;
            private final String field2;
            private final String field3;

            private ThreeParamDto(String field1, String field2, String field3) {
                this.field1 = field1;
                this.field2 = field2;
                this.field3 = field3;
            }

            public static ThreeParamDto of(
                    String field1,
                    String field2,
                    String field3
            ) {
                return new ThreeParamDto(field1, field2, field3);
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof ThreeParamDto other)) {
                    return false;
                }
                return field1.equals(other.field1) &&
                        field2.equals(other.field2) &&
                        field3.equals(other.field3);
            }
        }
    }

    static class InvalidDto implements FileDto {
        private InvalidDto() {
        }
        // of 메서드 없음
    }

    static class NoFactoryMethodDto implements FileDto {
        private final String field1;
        private final String field2;
        private final String field3;

        private NoFactoryMethodDto(String field1, String field2, String field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        // 다른 이름의 팩토리 메서드
        public static NoFactoryMethodDto create(
                String field1,
                String field2,
                String field3
        ) {
            return new NoFactoryMethodDto(field1, field2, field3);
        }
    }
}
