package store.model.order.chain;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import store.dto.StockReduceResultDto;
import store.model.domain.Product;
import store.model.domain.ProductStock;
import store.model.domain.Promotion;
import store.model.order.OrderContext;

public class InventoryReduceHandler extends OrderHandler {

    @Override
    protected void process(final OrderContext orderContext) {
        Map<Product, Integer> orderItems = orderContext.getOrderItems();
        LocalDate orderDate = orderContext.getOrderDate();

        Map<Product, StockReduceResultDto> stockReduceResults = new HashMap<>();
        orderItems.forEach((product, quantity) -> {
            StockReduceResultDto result = reduceStock(product, quantity, orderDate);
            stockReduceResults.put(product, result);
        });

        orderContext.attachStockReduceResults(stockReduceResults);
    }

    private StockReduceResultDto reduceStock(
            final Product product,
            final int orderQuantity,
            final LocalDate orderDate
    ) {
        if (!product.isPromotional(orderDate)) {
            return reduceNormalStock(product, orderQuantity);
        }

        return reducePromotionalStock(product, orderQuantity);
    }

    private StockReduceResultDto reduceNormalStock(
            final Product product,
            final int quantity
    ) {
        ProductStock normalStock = product.getNormalStock();
        normalStock.reduceQuantity(quantity);

        return StockReduceResultDto.of(quantity, 0, 0);
    }

    private StockReduceResultDto reducePromotionalStock(
            final Product product,
            final int orderQuantity
    ) {
        ProductStock promotionalStock = product.getPromotionalStock();
        ProductStock normalStock = product.getNormalStock();
        Promotion promotion = product.getPromotion();

        // 프로모션 재고를 최대한 사용
        int availablePromotionalStock = promotionalStock.getQuantity();
        int promotionalQuantityToUse = Math.min(orderQuantity, availablePromotionalStock);

        // 프로모션 세트 계산
        int setSize = promotion.getSetSize();
        int completeSets = promotionalQuantityToUse / setSize;
        int freeItems = completeSets * promotion.getGet();

        // 남은 수량은 일반 재고에서 차감
        int remainingQuantity = orderQuantity - promotionalQuantityToUse;

        // 재고 차감 실행
        if (promotionalQuantityToUse > 0) {
            promotionalStock.reduceQuantity(promotionalQuantityToUse);
        }

        if (remainingQuantity > 0) {
            normalStock.reduceQuantity(remainingQuantity);
        }

        return StockReduceResultDto.of(remainingQuantity, promotionalQuantityToUse, freeItems);
    }
}
