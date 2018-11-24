package api.data.sucursales;

import api.data.products.ProductEntity;
import io.reactivex.Flowable;
import org.davidmoten.rx.jdbc.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Salvador Montiel on 19/oct/2018.
 */
@Singleton
public class InventoryRepository {
    private Database db;

    @Inject
    public InventoryRepository(Database db) {
        this.db = db;
    }

    public Flowable<List<InventoryEntity>> getInventory(int idSucursal) {
        String query = "SELECT * FROM getInventarioBySucursal(?)";
        return db.select(query)
                .parameters(idSucursal)
                .get(rs -> {
                    int idProducto = rs.getInt("id_producto");
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int descuento = rs.getInt("porcentaje_descuento");
                    int idProd = rs.getInt("id_producto");
                    ProductEntity p = new ProductEntity(idProducto, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, descuento);
                    int id = rs.getInt("id");
                    int cantidad = rs.getInt("cantidad");
                    return new InventoryEntity(id, p, cantidad, idSucursal);
                })
                .toList()
                .toFlowable();
    }

    public Flowable<InventoryEntity> getInventoryByIds(int idSucursal, int idProducto) {
        String query = "SELECT * FROM getInventarioBySucursal(?) WHERE id_producto = ?";
        return db.select(query)
                .parameters(idSucursal, idProducto)
                .get(rs -> {
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int porcentajeDescuento = rs.getInt("porcentaje_descuento");
                    int idProd = rs.getInt("id_producto");
                    ProductEntity p = new ProductEntity(idProd, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, porcentajeDescuento);
                    int id = rs.getInt("id");
                    int cantidad = rs.getInt("cantidad");
                    return new InventoryEntity(id, p, cantidad, idSucursal);
                });
    }

    public Flowable<InventoryEntity> getInventoryById(int id) {
        String query = "SELECT * FROM getInventarioById(?)";
        return db.select(query)
                .parameters(id)
                .get(rs -> {
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int porcentajeDescuento = rs.getInt("porcentaje_descuento");
                    int idProducto = rs.getInt("id_producto");
                    ProductEntity p = new ProductEntity(idProducto, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, porcentajeDescuento);
                    int cantidad = rs.getInt("cantidad");
                    int idSucursal = rs.getInt("id_sucursal");
                    return new InventoryEntity(id, p, cantidad, idSucursal);
                });
    }

    public Flowable<InventoryEntity> _createInventory(int idSucursal, int idProducto, int cantidad) {
        String insertQuery = "INSERT INTO inventario (id_sucursal, id_producto, cantidad) " +
                "VALUES (?, ?, ?) RETURNING id";
        return db.select(insertQuery)
                .parameters(idSucursal, idProducto, cantidad)
                .get(rs -> rs.getInt("id"))
                .flatMap(this::getInventoryById);
    }

    public Flowable<InventoryEntity> updateInventory(int idSucursal, int idProducto, int cantidad) {
        String insertQuery = "UPDATE inventario SET cantidad = cantidad + ? " +
                "WHERE id_sucursal = ? " +
                "AND id_producto = ? RETURNING id";
        return db.select(insertQuery)
                .parameters(cantidad, idSucursal, idProducto)
                .get(rs -> rs.getInt("id"))
                .flatMap(this::getInventoryById);
    }
}
