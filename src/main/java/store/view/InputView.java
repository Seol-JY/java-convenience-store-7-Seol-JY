package store.view;

import static camp.nextstep.edu.missionutils.Console.readLine;

public class InputView {
    private static final String ORDER_INPUT_MESSAGE = "구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])";

    public String getOrderInput() {
        return readLineWithPrompt(ORDER_INPUT_MESSAGE);
    }

    private String readLineWithPrompt(final String prompt) {
        System.out.println(prompt);
        return readLine().strip();
    }
}
