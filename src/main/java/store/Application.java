package store;

import java.util.List;
import store.dto.ProductDto;
import store.dto.PromotionFileDto;
import store.loader.FileDataLoader;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;
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

    }
}
