package store.model.order.chain;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import store.model.domain.Product;
import store.model.domain.Promotion;
import store.model.order.OrderContext;

public class PromotionalItemAdditionHandler extends OrderHandler {
    private final BiFunction<String, Integer, Boolean> promotionConfirmationCallback;

    public PromotionalItemAdditionHandler(BiFunction<String, Integer, Boolean> promotionConfirmationCallback) {
        this.promotionConfirmationCallback = promotionConfirmationCallback;
    }

    @Override
    protected void process(final OrderContext orderContext) {
        LocalDate orderDate = orderContext.getOrderDate();
        Map<Product, Integer> promotionalItems = new HashMap<>();

        orderContext.getOrderItems().forEach((product, quantity) -> {
            if (product.isPromotional(orderDate)) {
                processPromotionalItem(product, quantity, promotionalItems);
            }
        });

        promotionalItems.forEach(orderContext::addOrderQuantity);
    }

    private void processPromotionalItem(Product product, int quantity, Map<Product, Integer> promotionalItems) {
        int promotionalQuantity = calculateAdditionalQuantity(product, quantity);

        if (promotionalQuantity > 0 && promotionConfirmationCallback.apply(product.getName(), promotionalQuantity)) {
            promotionalItems.put(product, promotionalQuantity);
        }
    }

    private int calculateAdditionalQuantity(Product product, int currentQuantity) {
        int promotionalQuantity = calculatePromotionalQuantity(product.getPromotion(), currentQuantity);

        if (promotionalQuantity == 0 || !hasEnoughPromotionalStock(product, currentQuantity, promotionalQuantity)) {
            return 0;
        }

        return promotionalQuantity;
    }

    private int calculatePromotionalQuantity(Promotion promotion, int currentQuantity) {
        int requiredQuantity = promotion.getBuy();
        int promotionSetSize = requiredQuantity + promotion.getGet();
        int remainingQuantity = currentQuantity % promotionSetSize;

        if (remainingQuantity < requiredQuantity) {
            return 0;
        }

        return promotionSetSize - remainingQuantity;
    }

    private boolean hasEnoughPromotionalStock(Product product, int currentQuantity, int promotionalQuantity) {
        return currentQuantity + promotionalQuantity <= product.getPromotionalStock().getQuantity();
    }
}
