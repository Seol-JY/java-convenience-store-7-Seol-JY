package store.model.order;

import static store.constant.ExceptionMessage.PRODUCT_NOT_FOUND;
import static store.constant.ExceptionMessage.WRONG_ORDER_INPUT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import store.dto.OrderItemDto;
import store.dto.ReceiptDto;
import store.dto.StockReduceResultDto;
import store.model.domain.Product;
import store.model.domain.Products;

public class OrderContext {
    public static final String MEMBERSHIP_DISCOUNT_NOT_APPLIED = "멤버십 할인이 적용되지 않았습니다.";

    private final LocalDate orderDate;
    private final Map<Product, Integer> orderItems;
    private final Products products;
    private Function<Integer, Integer> membershipDiscountSupplier;
    private Map<Product, StockReduceResultDto> stockReduceResults;
    private ReceiptDto receiptDto;

    private OrderContext(
            final LocalDate orderDate,
            final Map<Product, Integer> orderItems,
            final Products products
    ) {
        this.orderDate = orderDate;
        this.orderItems = orderItems;
        this.products = products;
    }

    public static OrderContext of(
            final LocalDateTime orderDateTime,
            final List<OrderItemDto> items,
            final Products products
    ) {
        Map<Product, Integer> orderItems = items.stream()
                .collect(Collectors.groupingBy(
                        dto -> findProduct(dto.name(), products),
                        Collectors.summingInt(dto -> validateAndGetQuantity(dto.quantity()))
                ));

        LocalDate orderDate = LocalDate.from(orderDateTime);
        return new OrderContext(orderDate, orderItems, products);
    }

    private static int validateAndGetQuantity(final int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(WRONG_ORDER_INPUT.message());
        }

        return quantity;
    }

    private static Product findProduct(final String name, final Products products) {
        return products.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND.message()));
    }

    public void addOrderQuantity(final Product product, final int quantity) {
        validateAndGetQuantity(quantity);
        orderItems.merge(product, quantity, Integer::sum);
    }

    public void updateOrderQuantity(final Product product, final int newQuantity) {
        orderItems.put(product, newQuantity);
    }

    public boolean isMembershipDiscountApplied() {
        return membershipDiscountSupplier != null;
    }

    public void removeOrderItem(final Product product) {
        orderItems.remove(product);
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public Map<Product, Integer> getOrderItems() {
        return Collections.unmodifiableMap(orderItems);
    }

    public ReceiptDto getReceipt() {
        return receiptDto;
    }

    public Map<Product, StockReduceResultDto> getStockReduceResults() {
        return Collections.unmodifiableMap(stockReduceResults);
    }

    public Function<Integer, Integer> getMembershipDiscountSupplier() {
        if (membershipDiscountSupplier == null) {
            throw new IllegalStateException(MEMBERSHIP_DISCOUNT_NOT_APPLIED);
        }

        return membershipDiscountSupplier;
    }

    public void attachReceipt(final ReceiptDto receiptDto) {
        this.receiptDto = receiptDto;
    }

    public void setMembershipDiscountSupplier(final Function<Integer, Integer> membershipDiscountSupplier) {
        this.membershipDiscountSupplier = membershipDiscountSupplier;
    }

    public void attachStockReduceResults(Map<Product, StockReduceResultDto> results) {
        this.stockReduceResults = results;
    }
}
