package api.controllers;

import api.ApiError;
import api.Authenticator;
import api.data.ventas.ItemsRepository;
import api.data.ventas.OrdenRepository;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.reactivex.Flowable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.micronaut.http.HttpResponse.unprocessableEntity;

/**
 * Created by Salvador Montiel on 07/dic/2018.
 */
@Controller("/ventas")
public class VentasController {
    private Authenticator auth;
    private OrdenRepository ordenRepo;
    private ItemsRepository itemsRepo;

    @Inject
    public VentasController(Authenticator auth, OrdenRepository ordenRepo, ItemsRepository itemsRepo) {
        this.auth = auth;
        this.ordenRepo = ordenRepo;
        this.itemsRepo = itemsRepo;
    }

    @Put
    public Flowable<HttpResponse> paymentDone(HttpRequest request, @Body ObjectNode body) {
        Optional<Flowable<HttpResponse>> authError = auth.authorize(request, false, false);
        if (authError.isPresent()) return authError.get();
        List<String> requiredFields = Arrays.asList("id_orden");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }
        int idOrden = body.get("id_orden").asInt();

        DecodedJWT jwt = auth.getJwt(request);
        Claim emailClaim = jwt.getClaim("email");

        return ordenRepo.orderIsPaid(idOrden, emailClaim.asString())
                .map(HttpResponse::ok);
    }

    @Post
    public Flowable<HttpResponse> createVenta(HttpRequest request, @Body ObjectNode body) {
        Optional<Flowable<HttpResponse>> authError = auth.authorize(request, false, false);
        if (authError.isPresent()) return authError.get();
        List<String> requiredFields = Arrays.asList("productos", "cantidades", "precios", "subtotal", "envio");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }

        List<Integer> prods = new ArrayList<>();
        for (JsonNode node : body.get("productos")) {
            prods.add(node.asInt());
        }
        List<Integer> cantidades = new ArrayList<>();
        for (JsonNode node : body.get("cantidades")) {
            cantidades.add(node.asInt());
        }
        List<Double> precios = new ArrayList<>();
        for (JsonNode node : body.get("precios")) {
            precios.add(node.asDouble());
        }

        double subtotal = body.get("subtotal").asDouble();
        double envio = body.get("envio").asDouble();
        System.out.println(body);
        DecodedJWT jwt = auth.getJwt(request);
        Claim emailClaim = jwt.getClaim("email");
        return ordenRepo.createOrder(emailClaim.asString(), subtotal, envio)
                .flatMap(orden -> itemsRepo.addItemsToOrder(orden.id, prods, cantidades, precios)
                        .andThen(Flowable.just(orden)))
                .map(orden -> (HttpResponse) HttpResponse.ok(orden))
                .onErrorReturn(throwable -> ApiError.asResponse(unprocessableEntity(), throwable.getMessage()));
    }
}
