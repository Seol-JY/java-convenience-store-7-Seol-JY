package store;

import static camp.nextstep.edu.missionutils.test.Assertions.assertNowTest;
import static camp.nextstep.edu.missionutils.test.Assertions.assertSimpleTest;
import static org.assertj.core.api.Assertions.assertThat;

import camp.nextstep.edu.missionutils.test.NsTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ApplicationTest extends NsTest {
    @Test
    void 파일에_있는_상품_목록_출력() {
        assertSimpleTest(() -> {
            run("[물-1]", "N", "N");
            assertThat(output()).contains(
                    "- 콜라 1,000원 10개 탄산2+1",
                    "- 콜라 1,000원 10개",
                    "- 사이다 1,000원 8개 탄산2+1",
                    "- 사이다 1,000원 7개",
                    "- 오렌지주스 1,800원 9개 MD추천상품",
                    "- 오렌지주스 1,800원 재고 없음",
                    "- 탄산수 1,200원 5개 탄산2+1",
                    "- 탄산수 1,200원 재고 없음",
                    "- 물 500원 10개",
                    "- 비타민워터 1,500원 6개",
                    "- 감자칩 1,500원 5개 반짝할인",
                    "- 감자칩 1,500원 5개",
                    "- 초코바 1,200원 5개 MD추천상품",
                    "- 초코바 1,200원 5개",
                    "- 에너지바 2,000원 5개",
                    "- 정식도시락 6,400원 8개",
                    "- 컵라면 1,700원 1개 MD추천상품",
                    "- 컵라면 1,700원 10개"
            );
        });
    }

    @Test
    void 여러_개의_일반_상품_구매() {
        assertSimpleTest(() -> {
            run("[비타민워터-3],[물-2],[정식도시락-2]", "N", "N");
            assertThat(output().replaceAll("\\s", "")).contains("내실돈18,300");
        });
    }

    @Test
    void 기간에_해당하지_않는_프로모션_적용() {
        assertNowTest(() -> {
            run("[감자칩-2]", "N", "N");
            assertThat(output().replaceAll("\\s", "")).contains("내실돈3,000");
        }, LocalDate.of(2024, 2, 1).atStartOfDay());
    }

    @Test
    void 예외_테스트() {
        assertSimpleTest(() -> {
            runException("[컵라면-12]", "N", "N");
            assertThat(output()).contains("[ERROR] 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요.");
        });
    }

    @Nested
    class 프로모션_적용_테스트 {
        @Test
        void 탄산2플러스1_프로모션_정상_적용() {
            assertSimpleTest(() -> {
                run("[콜라-6]", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("내실돈4,000")  // 6000 - 2000(2개 무료)
                        .contains("콜라2");  // 증정 2개
            });
        }

        @Test
        void MD추천상품_프로모션_정상_적용() {
            assertSimpleTest(() -> {
                run("[오렌지주스-2]", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("내실돈1,800")  // 3600 - 1800(1개 무료)
                        .contains("오렌지주스1");  // 증정 1개
            });
        }

        @Test
        void 프로모션_재고_부족시_일반_재고_사용() {
            assertSimpleTest(() -> {
                run("[컵라면-3]", "Y", "N", "N");
                assertThat(output()).contains("현재 컵라면 3개는 프로모션 할인이 적용되지 않습니다.");
            });
        }
    }

    @Nested
    class 멤버십_할인_테스트 {
        @Test
        void 멤버십_할인_최대한도_적용() {
            assertSimpleTest(() -> {
                run("[정식도시락-5]", "Y", "N");  // 32,000원 주문
                assertThat(output().replaceAll("\\s", ""))
                        .contains("멤버십할인-8,000");  // 최대 한도 8,000원
            });
        }

        @Test
        void 프로모션과_멤버십_할인_함께_적용() {
            assertSimpleTest(() -> {
                run("[콜라-7]", "Y", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("행사할인-2,000")
                        .contains("멤버십할인-300");  // (6000-2000)*0.3
            });
        }
    }

    @Nested
    class 추가_구매_프로세스_테스트 {
        @Test
        void 추가_구매시_재고_정상_반영() {
            assertSimpleTest(() -> {
                run("[콜라-3]", "N", "Y", "[콜라-3]", "N", "Y", "[정식도시락-1]", "N", "N");
                assertThat(output()).contains("콜라 1,000원 4개 탄산2+1");  // 10-6=4 남은 재고
            });
        }
    }

    @Nested
    class 입력_검증_테스트 {
        @Test
        void 잘못된_상품명_입력() {
            assertSimpleTest(() -> {
                runException("[존재안함-1]", "N", "N");
                assertThat(output()).contains("[ERROR] 존재하지 않는 상품입니다.");
            });
        }

        @Test
        void 잘못된_입력_형식() {
            assertSimpleTest(() -> {
                runException("콜라-1", "N", "N");
                assertThat(output()).contains("[ERROR] 올바르지 않은 형식으로 입력했습니다.");
            });
        }

        @Test
        void 잘못된_YN_입력() {
            assertSimpleTest(() -> {
                runException("[콜라-1]", "A", "N");
                assertThat(output()).contains("[ERROR] 잘못된 입력입니다.");
            });
        }
    }

    @Nested
    class 프로모션_추가_제안_테스트 {
        @Test
        void 프로모션_추가_구매_수락시_증정품_제공() {
            assertSimpleTest(() -> {
                run("[오렌지주스-1]", "Y", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("오렌지주스2")  // 주문 수량
                        .contains("오렌지주스1"); // 증정 수량
            });
        }

        @Test
        void 프로모션_추가_구매_거절시_기본_수량만_처리() {
            assertSimpleTest(() -> {
                run("[오렌지주스-1]", "N", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("오렌지주스1")  // 주문 수량만
                        .doesNotContain("=============증정===============오렌지주스");
            });
        }

        @Test
        void 여러_상품_동시_주문시_각각_프로모션_제안() {
            assertSimpleTest(() -> {
                run("[오렌지주스-1],[초코바-1]", "Y", "Y", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("오렌지주스2")
                        .contains("초코바2")
                        .contains("오렌지주스1")
                        .contains("초코바1");
            });
        }
    }

    @Nested
    class 재고_관리_테스트 {
        @Test
        void 일반_재고와_프로모션_재고_분리_관리() {
            assertSimpleTest(() -> {
                run("[콜라-3]", "N", "Y", "[콜라-8]", "Y", "N", "Y", "[에너지바-1]", "N", "N");
                assertThat(output())
                        .contains("콜라 1,000원 9개")  // 첫 구매 후 일반 재고
                        .contains("콜라 1,000원 재고 없음 탄산2+1");  // 프로모션 재고 소진
            });
        }

        @Test
        void 프로모션_재고_소진후_일반_재고_사용() {
            assertSimpleTest(() -> {
                run("[콜라-12]", "Y", "N", "Y", "[에너지바-1]", "N", "N");
                assertThat(output())
                        .contains("현재 콜라 3개는 프로모션 할인이 적용되지 않습니다")
                        .contains("콜라 1,000원 8개");  // 남은 일반 재고
            });
        }
    }

    @Nested
    class 영수증_출력_테스트 {
        @Test
        void 복합_할인_적용시_영수증_정확성() {
            assertSimpleTest(() -> {
                run("[콜라-6],[물-2]", "Y", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("총구매액87,000")  // 콜라6 + 물2 = 8개
                        .contains("행사할인-2,000")  // 콜라 2개 무료
                        .contains("내실돈4,700");  // 7,000 - 2,000 - 300
            });
        }

        @Test
        void 증정_상품_없을때_영수증_형식() {
            assertSimpleTest(() -> {
                run("[물-2]", "N", "N");
                assertThat(output())
                        .doesNotContain("=============증 정===============");
            });
        }

        @Test
        void 멤버십_할인_거절시_영수증_형식() {
            assertSimpleTest(() -> {
                run("[물-2]", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("멤버십할인-0");
            });
        }
    }

    @Nested
    class 예외_처리_추가_테스트 {
        @Test
        void 음수_수량_입력() {
            assertSimpleTest(() -> {
                runException("[콜라--1]", "N", "N");
                assertThat(output()).contains("[ERROR] 올바르지 않은 형식으로 입력했습니다.");
            });
        }

        @Test
        void 잘못된_대괄호_형식() {
            assertSimpleTest(() -> {
                runException("[콜라-1,물-2]", "N", "N");
                assertThat(output()).contains("[ERROR] 올바르지 않은 형식으로 입력했습니다.");
            });
        }

        @Test
        void 빈_입력() {
            assertSimpleTest(() -> {
                runException("", "N", "N");
                assertThat(output()).contains("[ERROR] 올바르지 않은 형식으로 입력했습니다.");
            });
        }
    }

    @Nested
    class 가격_계산_테스트 {
        @Test
        void 프로모션_할인_최대_적용() {
            assertSimpleTest(() -> {
                run("[콜라-12]", "Y", "N", "N");  // 2+1 프로모션 최대 적용
                assertThat(output().replaceAll("\\s", ""))
                        .contains("행사할인-3,000");  // 3개 무료
            });
        }

        @Test
        void 프로모션과_멤버십_할인_한도_계산() {
            assertSimpleTest(() -> {
                run("[정식도시락-5]", "Y", "N");  // 32,000원
                assertThat(output().replaceAll("\\s", ""))
                        .contains("멤버십할인-8,000")  // 최대 한도 적용
                        .contains("내실돈24,000");  // 32,000 - 8,000
            });
        }
    }

    @Nested
    class 날짜_기반_프로모션_테스트 {
        @Test
        void 프로모션_시작일_이전_구매() {
            assertNowTest(() -> {
                run("[감자칩-2]", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("=============증정===============없음");
            }, LocalDate.of(2023, 10, 31).atStartOfDay());
        }

        @Test
        void 프로모션_종료일_이후_구매() {
            assertNowTest(() -> {
                run("[감자칩-2]", "N", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("=============증정===============없음");
            }, LocalDate.of(2024, 12, 1).atStartOfDay());
        }

        @Test
        void 프로모션_기간_중_구매() {
            assertNowTest(() -> {
                run("[감자칩-2]", "Y", "N");
                assertThat(output().replaceAll("\\s", ""))
                        .contains("감자칩1");  // 증정 수량
            }, LocalDate.of(2024, 11, 15).atStartOfDay());
        }
    }

    @Override
    public void runMain() {
        Application.main(new String[]{});
    }
}