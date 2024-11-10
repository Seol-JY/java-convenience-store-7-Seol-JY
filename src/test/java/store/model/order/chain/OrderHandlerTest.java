package store.model.order.chain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.model.order.OrderContext;

class OrderHandlerTest {

    @Nested
    class 주문_처리_체인_테스트 {
        @Test
        void 다음_핸들러가_없으면_현재_핸들러만_실행한다() {
            // given
            List<String> executionOrder = new ArrayList<>();
            TestOrderHandler handler = new TestOrderHandler("handler1", executionOrder);
            OrderContext context = createOrderContext();

            // when
            handler.handle(context);

            // then
            assertThat(executionOrder)
                    .hasSize(1)
                    .containsExactly("handler1");
        }

        @Test
        void 다음_핸들러가_있으면_순차적으로_실행한다() {
            // given
            List<String> executionOrder = new ArrayList<>();
            TestOrderHandler firstHandler = new TestOrderHandler("handler1", executionOrder);
            TestOrderHandler secondHandler = new TestOrderHandler("handler2", executionOrder);
            TestOrderHandler thirdHandler = new TestOrderHandler("handler3", executionOrder);

            firstHandler.setNext(secondHandler).setNext(thirdHandler);
            OrderContext context = createOrderContext();

            // when
            firstHandler.handle(context);

            // then
            assertThat(executionOrder)
                    .hasSize(3)
                    .containsExactly("handler1", "handler2", "handler3");
        }
    }

    private static class TestOrderHandler extends OrderHandler {
        private final String handlerName;
        private final List<String> executionOrder;

        TestOrderHandler(String handlerName, List<String> executionOrder) {
            this.handlerName = handlerName;
            this.executionOrder = executionOrder;
        }

        @Override
        protected void process(OrderContext context) {
            executionOrder.add(handlerName);
        }
    }

    private OrderContext createOrderContext() {
        // Dummy
        return null;
    }
}
