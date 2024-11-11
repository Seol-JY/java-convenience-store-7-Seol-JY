package store.model.order.chain;

import java.time.LocalDate;
import java.util.function.BiFunction;
import store.model.domain.Product;
import store.model.domain.ProductStock;
import store.model.domain.Promotion;
import store.model.order.OrderContext;

public class InsufficientPromotionalStockHandler extends OrderHandler {
    private final BiFunction<String, Integer, Boolean> normalPriceConfirmationCallback;

    public InsufficientPromotionalStockHandler(BiFunction<String, Integer, Boolean> normalPriceConfirmationCallback) {
        this.normalPriceConfirmationCallback = normalPriceConfirmationCallback;
    }

    @Override
    protected void process(final OrderContext orderContext) {
        LocalDate orderDate = orderContext.getOrderDate();

        orderContext.getOrderItems().forEach((product, quantity) -> {
            if (product.isPromotional(orderDate)) {
                processPromotionalProduct(orderContext, product, quantity);
            }
        });
    }

    private void processPromotionalProduct(OrderContext orderContext, Product product, int quantity) {
        ProductStock promotionalStock = product.getPromotionalStock();
        Promotion promotion = product.getPromotion();
        int promotionSetSize = promotion.getBuy() + promotion.getGet();

        int availableFullSets = promotionalStock.getQuantity() / promotionSetSize;
        int maxPromotionalQuantity = availableFullSets * promotionSetSize;

        if (quantity > maxPromotionalQuantity && promotionalStock.getQuantity() < quantity) {
            handleExcessQuantity(orderContext, product, quantity, maxPromotionalQuantity);
        }
    }

    private void handleExcessQuantity(
            OrderContext orderContext,
            Product product,
            int originalQuantity,
            int maxPromotionalQuantity
    ) {
        int excessQuantity = originalQuantity - maxPromotionalQuantity;

        if (normalPriceConfirmationCallback.apply(product.getName(), excessQuantity)) {
            return;
        }

        if (maxPromotionalQuantity <= 0) {
            orderContext.removeOrderItem(product);
            return;
        }

        orderContext.updateOrderQuantity(product, maxPromotionalQuantity);
    }
}
