package store.view;

import static camp.nextstep.edu.missionutils.Console.readLine;

public class InputView {
    private static final String ORDER_INPUT_MESSAGE
            = "구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])";
    private static final String PROMOTION_ADDITIONAL_MESSAGE
            = "%n현재 %s은(는) %d개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)%n";
    private static final String NORMAL_PRICE_CONFIRMATION_MESSAGE =
            "%n현재 %s %d개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)%n";

    public String getOrderInput() {
        return readLineWithPrompt(ORDER_INPUT_MESSAGE);
    }

    public String getPromotionalItemAdd(String productName, int quantity) {
        return readLineWithFormattedPrompt(PROMOTION_ADDITIONAL_MESSAGE, productName, quantity);
    }

    public String getNormalPriceConfirmation(String productName, int quantity) {
        return readLineWithFormattedPrompt(NORMAL_PRICE_CONFIRMATION_MESSAGE, productName, quantity);
    }

    private String readLineWithPrompt(final String prompt) {
        System.out.println(prompt);
        return readLine().strip();
    }

    private String readLineWithFormattedPrompt(final String prompt, final Object... args) {
        System.out.printf(prompt, args);
        return readLine().strip();
    }
}
