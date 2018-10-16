package api;

import api.data.categories.CategoriesRepository;
import api.data.categories.CategoryEntity;
import api.data.products.ProductEntity;
import api.data.products.ProductsRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.reactivex.Flowable;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.micronaut.http.HttpResponse.unprocessableEntity;

/**
 * Created by Salvador Montiel on 14/oct/2018.
 */
@Controller("/productos")
public class ProductsController {
    @Inject CategoriesRepository categoriesRepository;
    @Inject ProductsRepository productsRepo;
    @Inject Authenticator auth;

    @Get
    public Flowable<HttpResponse> listProducts(HttpRequest request) {
        if (request.getParameters().contains("categoria")) {
            String category = request.getParameters().get("categoria");
            return productsRepo.getAllProductsBy(category).map(HttpResponse::ok);
        } else return productsRepo.getAllProducts().map(HttpResponse::ok);
    }

    @Get("/{id}")
    public Flowable<HttpResponse> getProduct(String id) {
        int newId;
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

    @Post
    public Flowable<HttpResponse> createProduct(HttpRequest request, @Body ObjectNode body) {
        Optional<Flowable<HttpResponse>> authError = auth.authenticate(request, "SUPER_ADMIN");
        if (authError.isPresent()) return authError.get();

        List<String> requiredFields = Arrays.asList("codigo_barras", "nombre", "descripcion", "url_foto", "formato", "categoria");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }
        String codigoBarras = body.get("codigo_barras").asText();
        String nombre = body.get("nombre").asText();
        String descripcion = body.get("descripcion").asText();
        String urlFoto = body.get("url_foto").asText();
        String formato = body.get("formato").asText();
        String categoria = body.get("categoria").asText();

        CategoryEntity categoryEntity = categoriesRepository.getCategoryByName(categoria).blockingLast();
        if (categoryEntity instanceof CategoryEntity.NoCategory)
            return ApiError.of(HttpResponse.notFound(), "Categoría '" + categoria + "' no encontrada");

        return productsRepo.createProduct(codigoBarras, nombre, descripcion, urlFoto, formato, categoria)
                .map(HttpResponse::ok);
    }
}
