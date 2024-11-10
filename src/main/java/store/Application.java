package store;

import java.util.List;
import store.dto.ProductFileDto;
import store.dto.PromotionFileDto;
import store.loader.FileDataLoader;
import store.model.domain.Products;
import store.model.domain.Promotion;
import store.model.domain.Promotions;

public class Application {
    public static void main(String[] args) {
        List<ProductFileDto> load = new FileDataLoader<>(ProductFileDto.class)
                .load("src/main/resources/products.md");
        List<PromotionFileDto> load2 = new FileDataLoader<>(PromotionFileDto.class)
                .load("src/main/resources/promotions.md");

        List<Promotion> rawPromotions = load2.stream().map(
                Promotion::from
        ).toList();

        Promotions promotions = Promotions.from(rawPromotions);
        Products products = Products.from(load, promotions);


    }
}
