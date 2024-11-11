package store.model.order.chain;

import java.util.function.Function;
import java.util.function.Supplier;
import store.model.order.OrderContext;

public class MembershipDiscountHandler extends OrderHandler {
    private static final double MEMBERSHIP_DISCOUNT_RATE = 0.3;
    private static final int MAX_DISCOUNT_AMOUNT = 8000;

    private final Supplier<Boolean> membershipConfirmSupplier;

    public MembershipDiscountHandler(Supplier<Boolean> membershipConfirmSupplier) {
        this.membershipConfirmSupplier = membershipConfirmSupplier;
    }

    @Override
    protected void process(final OrderContext orderContext) {
        if (membershipConfirmSupplier.get()) {
            Function<Integer, Integer> membershipDiscountCalculator = (priceWithoutPromotion) -> {
                int calculatedDiscount = (int) (priceWithoutPromotion * MEMBERSHIP_DISCOUNT_RATE);
                return Math.min(calculatedDiscount, MAX_DISCOUNT_AMOUNT);
            };

            orderContext.setMembershipDiscountSupplier(membershipDiscountCalculator);
        }
    }
}
