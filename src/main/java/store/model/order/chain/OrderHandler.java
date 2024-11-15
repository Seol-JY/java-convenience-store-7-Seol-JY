package store.model.order.chain;

import store.model.order.OrderContext;

public abstract class OrderHandler {
    protected OrderHandler nextHandler;

    public OrderHandler setNext(final OrderHandler handler) {
        this.nextHandler = handler;
        return handler;
    }

    public void handle(final OrderContext context) {
        process(context);

        if (nextHandler != null) {
            nextHandler.handle(context);
        }
    }

    protected abstract void process(final OrderContext context);
}
