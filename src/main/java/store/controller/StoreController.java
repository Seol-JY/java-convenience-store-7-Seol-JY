package store.controller;

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

public class StoreController {
    private static final String PRODUCTS_FILE_PATH = "src/main/resources/products.md";
    private static final String PROMOTIONS_FILE_PATH = "src/main/resources/promotions.md";

    private final InputView inputView;
    private final OutputView outputView;

    public StoreController(InputView inputView, OutputView outputView) {
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        List<ProductDto> products = new FileDataLoader<>(ProductDto.class).load(PRODUCTS_FILE_PATH);
        Products productsModel = createProductsModel(products);

        boolean shouldContinue;
        do {
            shouldContinue = processSingleOrder(products, productsModel);
            products = productsModel.updateDtoQuantities(products);

        } while (shouldContinue);
    }

    private boolean processSingleOrder(List<ProductDto> products, Products productsModel) {
        outputView.printProducts(products);

        OrderContext orderContext = createValidatedOrder(productsModel);
        processOrderWithHandlerChain(orderContext);

        outputView.printReceipt(orderContext.getReceipt());

        String userInput = inputView.getAdditionalPurchaseConfirmation();
        return YesNoParser.parse(userInput);
    }

    private Products createProductsModel(List<ProductDto> products) {
        List<PromotionFileDto> promotions = new FileDataLoader<>(PromotionFileDto.class)
                .load(PROMOTIONS_FILE_PATH);

        List<Promotion> rawPromotions = promotions.stream()
                .map(Promotion::from)
                .toList();

        Promotions promotionsModel = Promotions.from(rawPromotions);
        return Products.from(products, promotionsModel);
    }

    private OrderContext createValidatedOrder(Products productsModel) {
        return withRetry(() -> {
            String orderInput = inputView.getOrderInput();
            List<OrderItemDto> orderItems = OrderParser.parse(orderInput);
            OrderContext context = OrderContext.of(DateTimes.now(), orderItems, productsModel);
            OrderHandler stockValidationHandler = new StockValidationHandler();
            stockValidationHandler.handle(context);
            return context;
        });
    }

    private void processOrderWithHandlerChain(OrderContext orderContext) {
        OrderHandler promotionalItemAdditionHandler = createPromotionalItemHandler();
        OrderHandler insufficientPromotionalStockHandler = createInsufficientStockHandler();
        OrderHandler membershipDiscountHandler = createMembershipDiscountHandler();

        promotionalItemAdditionHandler
                .setNext(insufficientPromotionalStockHandler)
                .setNext(membershipDiscountHandler)
                .setNext(new InventoryReduceHandler())
                .setNext(new PurchaseHandler());

        promotionalItemAdditionHandler.handle(orderContext);
    }

    private OrderHandler createPromotionalItemHandler() {
        BiFunction<String, Integer, Boolean> promotionalConfirmer =
                (productName, quantity) -> withRetry(() -> {
                    String userInput = inputView.getPromotionalItemAdd(productName, quantity);
                    return YesNoParser.parse(userInput);
                });

        return new PromotionalItemAdditionHandler(promotionalConfirmer);
    }

    private OrderHandler createInsufficientStockHandler() {
        BiFunction<String, Integer, Boolean> insufficientStockConfirmer =
                (productName, quantity) -> withRetry(() -> {
                    String userInput = inputView.getNormalPriceConfirmation(productName, quantity);
                    return YesNoParser.parse(userInput);
                });

        return new InsufficientPromotionalStockHandler(insufficientStockConfirmer);
    }

    private OrderHandler createMembershipDiscountHandler() {
        Supplier<Boolean> membershipConfirmer = () -> withRetry(() -> {
            String userInput = inputView.getMembershipDiscountConfirmation();
            return YesNoParser.parse(userInput);
        });

        return new MembershipDiscountHandler(membershipConfirmer);
    }

    private <T> T withRetry(Supplier<T> function) {
        return RetryExecutor.execute(
                function,
                (error) -> outputView.printError(error.getMessage()),
                IllegalArgumentException.class
        );
    }
}
