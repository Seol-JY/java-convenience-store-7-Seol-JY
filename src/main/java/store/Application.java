package store;

import java.util.List;
import store.dto.ProductFileDto;
import store.dto.PromotionFileDto;
import store.loader.FileDataLoader;
import store.model.doamin.Promotion;
import store.model.doamin.Promotions;

public class Application {
    public static void main(String[] args) {
        List<ProductFileDto> load = new FileDataLoader<>(ProductFileDto.class)
                .load("src/main/resources/products.md");
        List<PromotionFileDto> load2 = new FileDataLoader<>(PromotionFileDto.class)
                .load("src/main/resources/promotions.md");

        List<Promotion> promotions = load2.stream().map(
                Promotion::from
        ).toList();

        Promotions.from(promotions);
    }
}
