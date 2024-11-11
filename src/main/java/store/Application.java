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
import store.model.order.chain.StockValidationHandler;
import store.util.OrderParser;
import store.util.RetryExecutor;
import store.util.YesNoParser;
import store.view.InputView;
import store.view.OutputView;

public class Application {
    public static void main(String[] args) {
//
//        Map<String, ReceiptDto.OrderItemInfo> orderedItems = new HashMap<>();
//        orderedItems.put("오렌지주스", new ReceiptDto.OrderItemInfo("오렌지주스", 2, 3600));
//
//        Map<String, Integer> promotionalItems = new HashMap<>();
//        promotionalItems.put("오렌지주스", 1);
//
//        ReceiptDto.PriceInfo priceInfo = new ReceiptDto.PriceInfo(
//                2,      // totalQuantity
//                3600,   // totalPrice
//                1800,   // promotionDiscount
//                0       // membershipDiscount
//        );
//
//        ReceiptDto receiptDto = new ReceiptDto(orderedItems, promotionalItems, priceInfo);
//
//        // 출력
//        new OutputView().printReceipt(receiptDto);

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

        stockValidationHandler
                .setNext(promotionalItemAdditionHandler)
                .setNext(insufficientPromotionalStockHandler)
                .setNext(membershipDiscountHandler)
                .setNext(inventoryReduceHandler)
                .handle(orderContext);

        List<ProductDto> productDtos = products.updateDtoQuantities(load);
        new OutputView().printProducts(productDtos);

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
