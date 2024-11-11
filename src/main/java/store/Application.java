package store;

import camp.nextstep.edu.missionutils.DateTimes;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import store.dto.OrderItemDto;
import store.dto.ProductDto;
import store.dto.PromotionFileDto;
import store.loader.FileDataLoader;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;
import store.model.order.OrderContext;
import store.model.order.chain.OrderHandler;
import store.model.order.chain.PromotionalItemAdditionHandler;
import store.model.order.chain.StockValidationHandler;
import store.util.OrderParser;
import store.util.RetryExecutor;
import store.util.YesNoParser;
import store.view.InputView;
import store.view.OutputView;

public class Application {
    public static void main(String[] args) {
        List<ProductDto> load = new FileDataLoader<>(ProductDto.class)
                .load("src/main/resources/products.md");
        List<PromotionFileDto> load2 = new FileDataLoader<>(PromotionFileDto.class)
                .load("src/main/resources/promotions.md");

        List<Promotion> rawPromotions = load2.stream().map(
                Promotion::from
        ).toList();

        Promotions promotions = Promotions.from(rawPromotions);
        Products products = Products.from(load, promotions);
        // 재고 업데이트
        products.updateDtoQuantities(load);

        new OutputView().printProducts(load);
        InputView inputView = new InputView();

        // TODO: 재시도 범위
        String orderInput = new InputView().getOrderInput();
        List<OrderItemDto> parse = OrderParser.parse(orderInput);
        OrderContext orderContext = OrderContext.of(DateTimes.now(), parse, products);
        OrderHandler stockValidationHandler = new StockValidationHandler();

        BiFunction<String, Integer, Boolean> confirmer =
                (productName, quantity) -> withRetry(() -> {
                    String userInput = inputView.getPromotionalItemAdd(productName, quantity);
                    return YesNoParser.parse(userInput);
                });

        OrderHandler promotionalItemAdditionHandler = new PromotionalItemAdditionHandler(confirmer);

        stockValidationHandler
                .setNext(promotionalItemAdditionHandler)
                .handle(orderContext);
    }

    private static <T> T withRetry(Supplier<T> function) {
        OutputView outputView = new OutputView();

        return RetryExecutor.execute(
                function,
                (error) -> outputView.printError(error.getMessage()),
                IllegalArgumentException.class
        );
    }
}
