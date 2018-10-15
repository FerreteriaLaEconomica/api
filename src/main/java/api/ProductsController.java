package api;

import api.data.products.ProductEntity;
import api.data.products.ProductsRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.Flowable;

import javax.inject.Inject;

/**
 * Created by Salvador Montiel on 14/oct/2018.
 */
@Controller("/productos")
public class ProductsController {
    @Inject
    ProductsRepository productsRepo;

    @Get
    public Flowable<HttpResponse> listProducts(HttpRequest request) {
        if (request.getParameters().contains("categoria")) {
            String category = request.getParameters().get("categoria");
            return productsRepo.getAllProductsBy(category).map(HttpResponse::ok);
        } else return productsRepo.getAllProducts().map(HttpResponse::ok);
    }

    @Get("/{id}")
    public Flowable<HttpResponse> getProduct(HttpRequest request, String id) {
        int newId = 0;
        try {
            newId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Producto con id '" + id + "' no encontrado");
        }
        return productsRepo.getProductById(newId)
                .first(new ProductEntity.NoProduct())
                .toFlowable()
                .flatMap(productEntity -> {
                    if (productEntity instanceof ProductEntity.NoProduct)
                        return ApiError.of(HttpResponse.notFound(), "Producto con id '" + id + "' no encontrado");
                    return Flowable.just(HttpResponse.ok(productEntity));
                });
    }
}
