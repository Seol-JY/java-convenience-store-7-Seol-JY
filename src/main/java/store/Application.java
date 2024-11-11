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
import store.model.order.chain.InsufficientPromotionalStockHandler;
import store.model.order.chain.InventoryReduceHandler;
import store.model.order.chain.MembershipDiscountHandler;
import store.model.order.chain.OrderHandler;
import store.model.order.chain.PromotionalItemAdditionHandler;
import store.model.order.chain.PurchaseHandler;
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

        boolean doNext;
        do {
            new OutputView().printProducts(load);
            InputView inputView = new InputView();

            OrderContext orderContext = withRetry(
                    () -> {
                        String orderInput = inputView.getOrderInput();
                        List<OrderItemDto> parse = OrderParser.parse(orderInput);
                        OrderContext orderContext2 = OrderContext.of(DateTimes.now(), parse, products);
                        OrderHandler stockValidationHandler = new StockValidationHandler();
                        stockValidationHandler.handle(orderContext2);
                        return orderContext2;
                    }
            );

            BiFunction<String, Integer, Boolean> confirmer1 =
                    (productName, quantity) -> withRetry(() -> {
                        String userInput = inputView.getPromotionalItemAdd(productName, quantity);
                        return YesNoParser.parse(userInput);
                    });

            OrderHandler promotionalItemAdditionHandler = new PromotionalItemAdditionHandler(confirmer1);

            BiFunction<String, Integer, Boolean> confirmer2 =
                    (productName, quantity) -> withRetry(() -> {
                        String userInput = inputView.getNormalPriceConfirmation(productName, quantity);
                        return YesNoParser.parse(userInput);
                    });
            OrderHandler insufficientPromotionalStockHandler = new InsufficientPromotionalStockHandler(confirmer2);

            Supplier<Boolean> confirmer3 = () -> withRetry(() -> {
                String userInput = inputView.getMembershipDiscountConfirmation();
                return YesNoParser.parse(userInput);
            });
            OrderHandler membershipDiscountHandler = new MembershipDiscountHandler(confirmer3);

            OrderHandler inventoryReduceHandler = new InventoryReduceHandler();

            OrderHandler purchaseHandler = new PurchaseHandler();

            promotionalItemAdditionHandler
                    .setNext(insufficientPromotionalStockHandler)
                    .setNext(membershipDiscountHandler)
                    .setNext(inventoryReduceHandler)
                    .setNext(purchaseHandler);

            promotionalItemAdditionHandler.handle(orderContext);

            new OutputView().printReceipt(orderContext.getReceipt());
            load = products.updateDtoQuantities(load);

            String userInput = inputView.getAdditionalPurchaseConfirmation();
            doNext = YesNoParser.parse(userInput);
        } while (doNext);
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
