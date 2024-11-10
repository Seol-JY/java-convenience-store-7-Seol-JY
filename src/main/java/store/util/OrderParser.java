package store.util;

import static store.constant.ExceptionMessage.WRONG_ORDER_INPUT;

import java.util.ArrayList;
import java.util.List;
import store.dto.OrderItemDto;

public class OrderParser {
    private static final String OPENING_BRACKET = "[";
    private static final String CLOSING_BRACKET = "]";
    private static final String HYPHEN = "-";
    private static final int EXPECTED_PARTS_LENGTH = 2;
    private static final int START_INDEX = 1;
    private static final String OrderSplitterPattern = "\\s*]\\s*,\\s*\\[\\s*";

    private OrderParser() {
    }

    public static List<OrderItemDto> parse(final String input) {
        validateInput(input);

        return parseOrders(input);
    }

    private static void validateInput(final String input) {
        if (isInvalidInput(input)) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }
    }

    private static boolean isInvalidInput(final String input) {
        return input == null
                || input.isBlank()
                || !input.startsWith(OPENING_BRACKET)
                || !input.endsWith(CLOSING_BRACKET);
    }

    private static List<OrderItemDto> parseOrders(final String input) {
        String content = input.substring(START_INDEX, input.length() - START_INDEX);
        String[] orderParts = content.split(OrderSplitterPattern);

        List<OrderItemDto> orders = new ArrayList<>();
        for (String orderPart : orderParts) {
            orders.add(parseOrderPart(orderPart.strip()));
        }

        validateOrders(orders);
        return orders;
    }

    private static void validateOrders(final List<OrderItemDto> orders) {
        if (orders.isEmpty()) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }
    }

    private static OrderItemDto parseOrderPart(final String orderPart) {
        String[] parts = orderPart.split(HYPHEN);
        validateParts(parts);

        return OrderItemDto.of(parts[0].strip(), NumberParser.parse(parts[1]));
    }

    private static void validateParts(final String[] parts) {
        if (parts.length != EXPECTED_PARTS_LENGTH) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }

        String firstPart = parts[0].strip();
        if (firstPart.contains("[") || firstPart.contains("]")) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }

        String secondPart = parts[1].strip();
        if (!secondPart.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }
    }
}
