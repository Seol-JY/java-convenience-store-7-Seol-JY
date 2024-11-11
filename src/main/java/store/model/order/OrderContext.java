package store.model.order;

import static store.constant.ExceptionMessage.PRODUCT_NOT_FOUND;
import static store.constant.ExceptionMessage.WRONG_ORDER_INPUT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import store.dto.OrderItemDto;
import store.model.domain.Product;
import store.model.domain.Products;

public class OrderContext {
    private final LocalDate orderDate;
    private final Map<Product, Integer> orderItems;
    private final Products products;

    private OrderContext(
            final LocalDate orderDate,
            final Map<Product, Integer> orderItems,
            final Products products
    ) {
        this.orderDate = orderDate;
        this.orderItems = orderItems;
        this.products = products;
    }

    public static OrderContext of(final LocalDateTime orderDateTime, final List<OrderItemDto> items,
                                  final Products products) {
        Map<Product, Integer> orderItems = items.stream()
                .collect(Collectors.groupingBy(
                        dto -> findProduct(dto.name(), products),
                        Collectors.summingInt(dto -> validateAndGetQuantity(dto.quantity()))
                ));

        LocalDate orderDate = LocalDate.from(orderDateTime);
        return new OrderContext(orderDate, orderItems, products);
    }

    private static int validateAndGetQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }

        return quantity;
    }

    private static Product findProduct(String name, Products products) {
        return products.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND.message()));
    }

    public void addOrderQuantity(Product product, int quantity) {
        validateAndGetQuantity(quantity);
        orderItems.merge(product, quantity, Integer::sum);
    }

    public void updateOrderQuantity(Product product, int newQuantity) {
        orderItems.put(product, newQuantity);
    }

    public void removeOrderItem(Product product) {
        orderItems.remove(product);
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public Map<Product, Integer> getOrderItems() {
        return Collections.unmodifiableMap(orderItems);
    }
}
