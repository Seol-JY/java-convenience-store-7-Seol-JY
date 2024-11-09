package store;

import java.util.List;
import store.dto.ProductFileDto;
import store.dto.PromotionFileDto;
import store.loader.FileDataLoader;

public class Application {
    public static void main(String[] args) {
        List<ProductFileDto> load = new FileDataLoader<>(ProductFileDto.class)
                .load("src/main/resources/products.md");
        List<PromotionFileDto> load2 = new FileDataLoader<>(PromotionFileDto.class)
                .load("src/main/resources/promotions.md");
        System.out.println(load2);
    }
}
