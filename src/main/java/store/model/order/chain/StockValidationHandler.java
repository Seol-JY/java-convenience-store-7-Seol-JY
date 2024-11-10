package store.model.order.chain;

import static store.constant.ExceptionMessage.INSUFFICIENT_STOCK;

import java.time.LocalDate;
import store.model.domain.Product;
import store.model.order.OrderContext;

public class StockValidationHandler extends OrderHandler {
    @Override
    protected void process(final OrderContext orderContext) {
        LocalDate orderDate = orderContext.getOrderDate();
        orderContext.getOrderItems()
                .forEach((product, orderQuantity) -> validateStock(orderDate, product, orderQuantity));
    }

    private void validateStock(final LocalDate orderDate, final Product product, final int orderQuantity) {
        Integer totalStock = product.getTotalStock(orderDate);

        if (totalStock < orderQuantity) {
            throw new IllegalArgumentException(INSUFFICIENT_STOCK.message());
        }
    }
}
