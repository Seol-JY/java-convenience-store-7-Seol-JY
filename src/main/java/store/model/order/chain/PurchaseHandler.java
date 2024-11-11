package store.model.order.chain;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import store.dto.ReceiptDto;
import store.dto.ReceiptDto.OrderItemInfo;
import store.dto.ReceiptDto.PriceInfo;
import store.dto.StockReduceResultDto;
import store.model.domain.Product;
import store.model.domain.Promotion;
import store.model.order.OrderContext;

public class PurchaseHandler extends OrderHandler {

    @Override
    protected void process(final OrderContext orderContext) {
        ReceiptDto receiptDto = generateReceipt(orderContext);
        orderContext.attachReceipt(receiptDto);
    }

    private ReceiptDto generateReceipt(final OrderContext orderContext) {
        Map<Product, StockReduceResultDto> stockReduceResults = orderContext.getStockReduceResults();
        Map<Product, Integer> orderItems = orderContext.getOrderItems();

        return new ReceiptDto(
                createOrderedItems(orderItems),
                createPromotionalItems(stockReduceResults),
                calculatePriceInfo(orderContext, orderItems, stockReduceResults)
        );
    }

    private Map<String, OrderItemInfo> createOrderedItems(final Map<Product, Integer> orderItems) {
        return orderItems.entrySet().stream()
                .collect(HashMap::new,
                        (map, entry) -> map.put(
                                entry.getKey().getName(),
                                createOrderItemInfo(entry)
                        ),
                        HashMap::putAll
                );
    }

    private OrderItemInfo createOrderItemInfo(Entry<Product, Integer> entry) {
        Product product = entry.getKey();
        int quantity = entry.getValue();
        int price = calculateItemPrice(product, quantity);

        return new OrderItemInfo(product.getName(), quantity, price);
    }

    private int calculateItemPrice(Product product, int quantity) {
        return product.getNormalStock().getPrice() * quantity;
    }

    private Map<String, Integer> createPromotionalItems(
            final Map<Product, StockReduceResultDto> stockReduceResults
    ) {
        return stockReduceResults.entrySet().stream()
                .filter(entry -> entry.getKey().getPromotion() != null)
                .filter(entry ->
                        entry.getValue().freeQuantity() > 0)
                .collect(HashMap::new,
                        (map, entry) -> map.put(
                                entry.getKey().getName(),
                                entry.getValue().freeQuantity()
                        ),
                        HashMap::putAll
                );
    }

    private PriceInfo calculatePriceInfo(
            final OrderContext orderContext,
            final Map<Product, Integer> orderItems,
            final Map<Product, StockReduceResultDto> stockReduceResults
    ) {
        int totalQuantity = calculateTotalQuantity(orderItems);
        int totalPrice = calculateTotalPrice(orderItems);
        int promotionDiscount = calculatePromotionDiscount(stockReduceResults);
        int membershipDiscount = calculateMembershipDiscount(orderContext, orderItems, stockReduceResults);

        return new PriceInfo(totalQuantity, totalPrice, promotionDiscount, membershipDiscount);
    }

    private int calculateTotalQuantity(final Map<Product, Integer> orderItems) {
        return orderItems.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int calculateTotalPrice(final Map<Product, Integer> orderItems) {
        return orderItems.entrySet().stream()
                .mapToInt(entry -> calculateItemPrice(entry.getKey(), entry.getValue()))
                .sum();
    }

    private int calculatePromotionDiscount(
            final Map<Product, StockReduceResultDto> stockReduceResults
    ) {
        return stockReduceResults.entrySet().stream()
                .filter(entry -> entry.getKey().getPromotion() != null)
                .mapToInt(entry -> calculatePromotionDiscountForProduct(entry.getKey(), entry.getValue()))
                .sum();
    }

    private int calculatePromotionDiscountForProduct(Product product, StockReduceResultDto result) {
        int freeItems = result.freeQuantity();
        return freeItems * product.getNormalStock().getPrice();
    }

    private int calculateMembershipDiscount(
            final OrderContext orderContext,
            final Map<Product, Integer> orderItems,
            final Map<Product, StockReduceResultDto> stockReduceResults
    ) {
        if (!orderContext.isMembershipDiscountApplied()) {
            return 0;
        }

        int priceAfterPromotion = calculatePriceAfterPromotion(orderItems, stockReduceResults);
        return orderContext.getMembershipDiscountSupplier().apply(priceAfterPromotion);
    }

    private int calculatePriceAfterPromotion(
            final Map<Product, Integer> orderItems,
            final Map<Product, StockReduceResultDto> stockReduceResults
    ) {
        return stockReduceResults.entrySet().stream()
                .mapToInt(entry -> calculateProductPriceAfterPromotion(entry.getKey(), entry.getValue(), orderItems))
                .sum();
    }

    private int calculateProductPriceAfterPromotion(
            Product product,
            StockReduceResultDto result,
            Map<Product, Integer> orderItems
    ) {
        if (product.getPromotion() == null) {
            return calculateItemPrice(product, orderItems.get(product));
        }

        int remainingQuantity = calculateRemainingQuantity(product.getPromotion(), result, orderItems.get(product));
        return calculateItemPrice(product, remainingQuantity);
    }

    private int calculateRemainingQuantity(Promotion promotion, StockReduceResultDto result, int totalQuantity) {
        int promotionalQuantity = result.promotionalQuantity();
        int setSize = promotion.getSetSize();
        int setCount = promotionalQuantity / setSize;
        int quantityInSets = setCount * setSize;

        return totalQuantity - quantityInSets;
    }
}
