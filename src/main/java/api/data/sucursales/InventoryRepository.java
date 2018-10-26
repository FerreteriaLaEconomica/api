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
        String query = "SELECT i.id, i.id_producto, p.codigo_barras, p.nombre, p.descripcion, p.url_foto, p.formato, " +
                "c.nombre AS \"categoria\", i.cantidad, i.precio_compra, i.precio_venta, i.porcentaje_descuento " +
                "FROM inventario i " +
                "INNER JOIN producto p ON i.id_producto = p.id " +
                "INNER JOIN categoria_producto cp ON p.id = cp.id_producto " +
                "INNER JOIN categoria c ON c.id = cp.id_categoria " +
                "WHERE i.id_sucursal = ?";
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
                    int idProd = rs.getInt("id_producto");
                    ProductEntity p = new ProductEntity(idProducto, codigoBarras, nombre, descripcion, urlFoto, formato, categoria);
                    int id = rs.getInt("id");
                    int cantidad = rs.getInt("cantidad");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int porcentajeDescuento = rs.getInt("porcentaje_descuento");
                    return new InventoryEntity(id, p, cantidad, precioCompra, precioVenta, porcentajeDescuento);
                })
                .toList()
                .toFlowable();
    }

    public Flowable<InventoryEntity> getInventoryByIds(int idSucursal, int idProducto) {
        String query = "SELECT i.id, i.id_producto, p.codigo_barras, p.nombre, p.descripcion, p.url_foto, p.formato, " +
                "c.nombre AS \"categoria\", i.cantidad, i.precio_compra, i.precio_venta, i.porcentaje_descuento " +
                "FROM inventario i " +
                "INNER JOIN producto p ON i.id_producto = p.id " +
                "INNER JOIN categoria_producto cp ON p.id = cp.id_producto " +
                "INNER JOIN categoria c ON c.id = cp.id_categoria " +
                "WHERE i.id_sucursal = ? " +
                "AND i.id_producto = p.id " +
                "AND i.id = ?";
        return db.select(query)
                .parameters(idSucursal, idProducto)
                .get(rs -> {
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");
                    int idProd = rs.getInt("id_producto");
                    ProductEntity p = new ProductEntity(idProd, codigoBarras, nombre, descripcion, urlFoto, formato, categoria);
                    int id = rs.getInt("id");
                    int cantidad = rs.getInt("cantidad");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int porcentajeDescuento = rs.getInt("porcentaje_descuento");
                    return new InventoryEntity(id, p, cantidad, precioCompra, precioVenta, porcentajeDescuento);
                })
                .onErrorReturnItem(new InventoryEntity.NoInventory());
    }

    public Flowable<InventoryEntity> getInventoryById(int id) {
        String query = "SELECT i.id, i.id_producto, p.codigo_barras, p.nombre, p.descripcion, p.url_foto, p.formato, " +
                "c.nombre AS \"categoria\", i.cantidad, i.precio_compra, i.precio_venta, i.porcentaje_descuento " +
                "FROM inventario i " +
                "INNER JOIN producto p ON i.id_producto = p.id " +
                "INNER JOIN categoria_producto cp ON p.id = cp.id_producto " +
                "INNER JOIN categoria c ON c.id = cp.id_categoria " +
                "WHERE i.id = ? " +
                "AND i.id_producto = p.id ";
        return db.select(query)
                .parameters(id)
                .get(rs -> {
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");
                    int idProducto = rs.getInt("id_producto");
                    ProductEntity p = new ProductEntity(idProducto, codigoBarras, nombre, descripcion, urlFoto, formato, categoria);
                    int cantidad = rs.getInt("cantidad");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int porcentajeDescuento = rs.getInt("porcentaje_descuento");
                    return new InventoryEntity(id, p, cantidad, precioCompra, precioVenta, porcentajeDescuento);
                })
                .onErrorReturnItem(new InventoryEntity.NoInventory());
    }

    public Flowable<InventoryEntity> updateInventory(int idSucursal, int idProducto, int cantidad,
                                                         double precioCompra, double precioVenta, int porcentajeDescuento) {
        String updateQuery = "UPDATE inventario SET (cantidad, precio_compra, precio_venta, porcentaje_descuento) " +
                "= (?, ?, ?, ?) " +
                "WHERE id_sucursal = ? " +
                "AND id = ? RETURNING *";
        return db.select(updateQuery)
                .parameters(cantidad, precioCompra, precioVenta, porcentajeDescuento, idSucursal, idProducto)
                .get(rs -> rs.getInt("id"))
                .flatMap(this::getInventoryById);
    }

    public Flowable<InventoryEntity> createInventory(int idSucursal, int idProducto, int cantidad, double precioCompra,
                                                     double precioVenta, int porcentajeDescuento) {
        String insertQuery = "INSERT INTO inventario (id_sucursal, id_producto, cantidad, precio_compra, " +
                    "precio_venta, porcentaje_descuento) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        return db.select(insertQuery)
                .parameters(idSucursal, idProducto, cantidad, precioCompra, precioVenta, porcentajeDescuento)
                .get(rs -> rs.getInt("id"))
                .flatMap(this::getInventoryById);
    }
}
