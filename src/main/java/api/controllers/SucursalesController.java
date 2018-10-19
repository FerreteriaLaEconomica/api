package api.controllers;

import api.ApiError;
import api.Authenticator;
import api.data.sucursales.SucursalEntity;
import api.data.sucursales.SucursalesRepository;
import api.data.users.UsersRepository;
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

import static io.micronaut.http.HttpResponse.unprocessableEntity;

/**
 * Created by Salvador Montiel on 19/oct/2018.
 */
@Controller("/sucursales")
public class SucursalesController {
    private SucursalesRepository sucursalesRepo;
    private Authenticator auth;
    private UsersRepository usersRepo;

    @Inject
    public SucursalesController(SucursalesRepository sucursalesRepo, Authenticator auth, UsersRepository usersRepo) {
        this.sucursalesRepo = sucursalesRepo;
        this.auth = auth;
        this.usersRepo = usersRepo;
    }

    @Get
    public Flowable<HttpResponse> listSucursales() {
        return sucursalesRepo.getAllSucursales()
                .map(HttpResponse::ok);
    }

    @Get("/{id}")
    public Flowable<HttpResponse> getSucursal(String id) {
        int newId;
        try {
            newId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Sucursal con id '" + id + "' no encontrada");
        }
        return sucursalesRepo.getSucursalById(newId)
                .first(new SucursalEntity.NoSucursal())
                .toFlowable()
                .flatMap(sucursalEntity -> {
                    if (sucursalEntity instanceof SucursalEntity.NoSucursal)
                        return ApiError.of(HttpResponse.notFound(), "Sucursal con id '" + id + "' no encontrada");
                    return Flowable.just(HttpResponse.ok(sucursalEntity));
                });
    }

    @Post
    public Flowable<HttpResponse> createSucursal(HttpRequest request, @Body ObjectNode body) {
        Optional<Flowable<HttpResponse>> authError = auth.authenticate(request, "SUPER_ADMIN");
        if (authError.isPresent()) return authError.get();

        List<String> requiredFields = Arrays.asList("nombre", "calle", "numero_exterior", "colonia", "codigo_postal",
                "localidad", "municipio", "estado", "email_administrador");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }

        String nombre = body.get("nombre").asText();
        SucursalEntity sucursalEntity = sucursalesRepo.getSucursalByName(nombre).blockingLast(new SucursalEntity.NoSucursal());
        if (!(sucursalEntity instanceof SucursalEntity.NoSucursal))
            return ApiError.of(HttpResponse.badRequest(), "Ya existe una sucursal con éste nombre: '" + nombre + "'");
        System.out.println(sucursalEntity);
        String calle = body.get("calle").asText();
        String numeroExterior = body.get("numero_exterior").asText();
        String colonia = body.get("colonia").asText();
        int codigoPostal = body.get("codigo_postal").asInt();
        String localidad = body.get("localidad").asText();
        String municipio = body.get("municipio").asText();
        String estado = body.get("estado").asText();
        String emailAdmin = body.get("email_administrador").asText();
        boolean existsUser = usersRepo.existsUserWithEmail(emailAdmin)
                .onErrorReturnItem(false)
                .blockingFirst();
        if (!existsUser) return ApiError.of(HttpResponse.notFound(), "No existe ningún usuario con el email: '" + emailAdmin + "'");

        return sucursalesRepo.createSucursal(nombre, calle, numeroExterior, colonia, codigoPostal, localidad, municipio, estado, emailAdmin)
                .map(HttpResponse::ok);
    }

    @Delete("/{id}")
    public Flowable<HttpResponse> deleteProduct(HttpRequest request, String id) {
        int oldId;
        try {
            oldId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Sucursal con id '" + id + "' no encontrada");
        }
        Optional<Flowable<HttpResponse>> authError = auth.authenticate(request, "SUPER_ADMIN");
        if (authError.isPresent()) return authError.get();

        return sucursalesRepo.getSucursalById(oldId)
                .first(new SucursalEntity.NoSucursal())
                .toFlowable()
                .flatMap(sucursalEntity -> {
                    if (sucursalEntity instanceof SucursalEntity.NoSucursal)
                        return ApiError.of(HttpResponse.notFound(), "Sucursal con id '" + id + "' no encontrada");

                    return Flowable.just(HttpResponse.ok(sucursalesRepo.deleteSucursalById(oldId)));
                });
    }
}
