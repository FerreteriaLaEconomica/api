package api.controllers;

import api.ApiError;
import api.Authenticator;
import api.data.products.ProductsRepository;
import api.data.sucursales.InventoryEntity;
import api.data.sucursales.InventoryRepository;
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

import static io.micronaut.http.HttpResponse.notFound;
import static io.micronaut.http.HttpResponse.unprocessableEntity;

/**
 * Created by Salvador Montiel on 19/oct/2018.
 */
@Controller("/sucursales")
public class SucursalesController {
    private SucursalesRepository sucursalesRepo;
    private Authenticator auth;
    private UsersRepository usersRepo;
    private InventoryRepository inventoryRepo;
    private ProductsRepository productsRepo;

    @Inject
    public SucursalesController(SucursalesRepository sucursalesRepo, Authenticator auth, UsersRepository usersRepo, InventoryRepository inventoryRepo, ProductsRepository productsRepo) {
        this.sucursalesRepo = sucursalesRepo;
        this.auth = auth;
        this.usersRepo = usersRepo;
        this.inventoryRepo = inventoryRepo;
        this.productsRepo = productsRepo;
    }

    @Put("/{idSucursal}/productos/{idProducto}")
    public Flowable<HttpResponse> updateProductOfSucursal(HttpRequest request, @Body ObjectNode body, String idSucursal, String idProducto) {
        Optional<Flowable<HttpResponse>> authError = auth.authorize(request, "SUPER_ADMIN", "ADMIN");
        if (authError.isPresent()) return authError.get();
        int newSucursalId;
        try {
            newSucursalId = Integer.valueOf(idSucursal);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Sucursal con id '" + idSucursal + "' no encontrada");
        }
        SucursalEntity s = sucursalesRepo.getSucursalById(newSucursalId).blockingFirst(new SucursalEntity.NoSucursal());
        if (s instanceof SucursalEntity.NoSucursal)
            return ApiError.of(notFound(), "La sucursal con id '" + newSucursalId + "' no existe");

        int newId;
        try {
            newId = Integer.valueOf(idProducto);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Producto con id '" + idProducto + "' no encontrado");
        }
        InventoryEntity i = inventoryRepo.getInventoryById(newId).blockingFirst(new InventoryEntity.NoInventory());
        if (i instanceof InventoryEntity.NoInventory)
            return ApiError.of(notFound(), "El producto con id '" + newId + "' no existe");
        List<String> requiredFields = Arrays.asList("cantidad", "precio_compra", "precio_venta", "porcentaje_descuento");
        String fields = requiredFields.stream().filter(required -> body.get(required) == null)
                .collect(Collectors.joining(", "));
        if (!fields.equals("")) {
            return ApiError.of(unprocessableEntity(), "Faltan este(os) campo(s) para proceder: " + fields + ".");
        }
        int cantidad = body.get("cantidad").asInt();
        double precioCompra = body.get("precio_compra").asDouble();
        double precioVenta = body.get("precio_venta").asDouble();
        int porcentajeDescuento = body.get("porcentaje_descuento").asInt();

        return inventoryRepo.updateInventory(newSucursalId, newId, cantidad, precioCompra, precioVenta, porcentajeDescuento)
                .first(new InventoryEntity.NoInventory())
                .toFlowable()
                .flatMap(inventoryEntity -> {
                    if (inventoryEntity instanceof InventoryEntity.NoInventory)
                        return ApiError.of(notFound(), "El producto con id '" + newId + "' no encontrado");
                    return Flowable.just(HttpResponse.ok(inventoryEntity));
                });
    }

    @Get("/{idSucursal}/productos/{idProducto}")
    public Flowable<HttpResponse> getProductOfSucursal(String idSucursal, String idProducto) {
        int newSucursalId;
        try {
            newSucursalId = Integer.valueOf(idSucursal);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Sucursal con id '" + idSucursal + "' no encontrada");
        }
        SucursalEntity s = sucursalesRepo.getSucursalById(newSucursalId).blockingFirst(new SucursalEntity.NoSucursal());
        if (s instanceof SucursalEntity.NoSucursal)
            return ApiError.of(notFound(), "La sucursal con id '" + newSucursalId + "' no existe");

        int newId;
        try {
            newId = Integer.valueOf(idProducto);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Producto con id '" + idProducto + "' no encontrado");
        }
        InventoryEntity i = inventoryRepo.getInventoryById(newId).blockingFirst(new InventoryEntity.NoInventory());
        if (i instanceof InventoryEntity.NoInventory)
            return ApiError.of(notFound(), "El producto con id '" + newId + "' no existe");

        return inventoryRepo.getInventoryByIds(newSucursalId, newId)
                .first(new InventoryEntity.NoInventory())
                .toFlowable()
                .flatMap(inventoryEntity -> {
                    if (inventoryEntity instanceof InventoryEntity.NoInventory)
                        return ApiError.of(notFound(), "El producto con id '" + newId + "' no encontrado");
                    return Flowable.just(HttpResponse.ok(inventoryEntity));
                });
    }

    @Get("/{idSucursal}/productos")
    public Flowable<HttpResponse> listProductsOfSucursal(String idSucursal) {
        int newId;
        try {
            newId = Integer.valueOf(idSucursal);
        } catch (NumberFormatException e) {
            return ApiError.of(HttpResponse.notFound(), "Sucursal con id '" + idSucursal + "' no encontrada");
        }
        SucursalEntity s = sucursalesRepo.getSucursalById(newId).blockingFirst(new SucursalEntity.NoSucursal());
        if (s instanceof SucursalEntity.NoSucursal)
            return ApiError.of(notFound(), "La sucursal con id '" + newId + "' no existe");

        return inventoryRepo.getInventory(newId)
                .map(HttpResponse::ok);
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
        Optional<Flowable<HttpResponse>> authError = auth.authorize(request, "SUPER_ADMIN");
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
                .map(sucursal -> {
                    productsRepo.createInventoryForAllProducts(sucursal.id, 0, 0.0, 0.1, 0)
                            .blockingFirst(false);
                    return sucursal;
                })
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
        Optional<Flowable<HttpResponse>> authError = auth.authorize(request, "SUPER_ADMIN");
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
