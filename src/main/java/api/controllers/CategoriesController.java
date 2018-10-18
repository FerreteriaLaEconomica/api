package api.controllers;

import api.ApiError;
import api.Authenticator;
import api.data.categories.CategoriesRepository;
import api.data.categories.CategoryEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.reactivex.Flowable;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.micronaut.http.HttpResponse.notFound;
import static io.micronaut.http.HttpResponse.unprocessableEntity;

/**
 * Created by Salvador Montiel on 17/oct/2018.
 */
@Controller("/categorias")
public class CategoriesController {
    @Inject CategoriesRepository categoriesRepository;
    @Inject Authenticator auth;

    @Get
    public Flowable<HttpResponse> listCategories() {
        return categoriesRepository.getAllCategories().map(HttpResponse::ok);
    }

    @Get("/{id}")
    public Flowable<HttpResponse> getCategory(String id) {
        int newId;
        try {
            newId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return ApiError.of(notFound(), "Categoría con id '" + id + "' no encontrada");
        }
        return categoriesRepository.getCategoryById(newId)
                .first(new CategoryEntity.NoCategory())
                .toFlowable()
                .flatMap(categoryEntity -> {
                    if (categoryEntity instanceof CategoryEntity.NoCategory)
                        return ApiError.of(notFound(), "Categoría con id '" + id + "' no encontrada");
                    return Flowable.just(HttpResponse.ok(categoryEntity));
                });
    }

    @Post
    public Flowable<HttpResponse> createCategory(HttpRequest request, @Body ObjectNode body) {
        Optional<Flowable<HttpResponse>> authError = auth.authenticate(request, "SUPER_ADMIN");
        if (authError.isPresent()) return authError.get();

        List<String> requiredFields = Arrays.asList("nombre");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }

        String nombre = body.get("nombre").asText();
        return categoriesRepository.createCategory(nombre)
                .map(HttpResponse::ok);
    }

    @Put("/{id}")
    public Flowable<HttpResponse> updateProduct(HttpRequest request, String id, @Body ObjectNode body) {
        int oldId;
        try {
            oldId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return ApiError.of(notFound(), "Categoría con id '" + id + "' no encontrada");
        }
        Optional<Flowable<HttpResponse>> authError = auth.authenticate(request, "SUPER_ADMIN");
        if (authError.isPresent()) return authError.get();

        List<String> requiredFields = Arrays.asList("nombre");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }
        CategoryEntity oldCategory = categoriesRepository.getCategoryById(oldId).blockingFirst(new CategoryEntity.NoCategory());
        if (oldCategory instanceof CategoryEntity.NoCategory)
            return ApiError.of(notFound(), "Categoría con id '" + id + "' no encontrada");

        String nombre = body.get("nombre").asText();
        return categoriesRepository.updateCategory(oldId, nombre)
                .onErrorReturn(throwable -> {
                    throwable.printStackTrace();
                    return new CategoryEntity.NoCategory();
                })
                .flatMap(categoryEntity -> {
                    System.out.println(categoryEntity);
                    if (categoryEntity instanceof CategoryEntity.NoCategory)
                        return ApiError.of(HttpResponse.notFound(), "Categoría con id '" + id + "' no encontrada");
                    return Flowable.just(HttpResponse.ok(categoryEntity));
                });
    }

    @Delete("/{id}")
    public Flowable<HttpResponse> deleteCategory(HttpRequest request, String id) {
        int oldId;
        try {
            oldId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Categoría con id '" + id + "' no encontrada");
        }
        Optional<Flowable<HttpResponse>> authError = auth.authenticate(request, "SUPER_ADMIN");
        if (authError.isPresent()) return authError.get();

        return categoriesRepository.getCategoryById(oldId)
                .first(new CategoryEntity.NoCategory())
                .toFlowable()
                .flatMap(categoryEntity -> {
                    if (categoryEntity instanceof CategoryEntity.NoCategory)
                        return ApiError.of(HttpResponse.notFound(), "Categoría con id '" + id + "' no encontrada");
                    return Flowable.just(HttpResponse.ok(categoriesRepository.deleteCategoryById(oldId)));
                });
    }
}
