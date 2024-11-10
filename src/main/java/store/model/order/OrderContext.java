package store.model.order;

import static store.constant.ExceptionMessage.PRODUCT_NOT_FOUND;
import static store.constant.ExceptionMessage.WRONG_ORDER_INPUT;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import store.dto.OrderItemDto;
import store.model.domain.Product;
import store.model.domain.Products;

public class OrderContext {
    private final Map<Product, Integer> orderItems;
    private final Products products;

    private OrderContext(Map<Product, Integer> orderItems, Products products) {
        this.orderItems = orderItems;
        this.products = products;
    }

    public static OrderContext from(List<OrderItemDto> items, Products products) {
        Map<Product, Integer> orderItems = items.stream()
                .collect(Collectors.groupingBy(
                        dto -> findProduct(dto.name(), products),
                        Collectors.summingInt(dto -> validateAndGetQuantity(dto.quantity()))
                ));

        return new OrderContext(orderItems, products);
    }

    private static Product findProduct(String name, Products products) {
        return products.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND.message()));
    }

    private static int validateAndGetQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }

        return quantity;
    }
}
