package api.data.products;

import api.data.categories.CategoriesRepository;
import api.data.categories.CategoryEntity;
import api.data.sucursales.InventoryRepository;
import io.reactivex.Flowable;
import org.davidmoten.rx.jdbc.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Salvador Montiel on 14/oct/2018.
 */
@Singleton
public class ProductsRepository {
    private Database db;
    private CategoriesRepository categoriesRepository;
    private InventoryRepository inventoryRepo;

    @Inject
    public ProductsRepository(Database db, CategoriesRepository categoriesRepository, InventoryRepository inventoryRepo) {
        this.db = db;
        this.categoriesRepository = categoriesRepository;
        this.inventoryRepo = inventoryRepo;
    }

    public Flowable<ProductEntity> getProductById(int id) {
        String query = "SELECT * FROM getAllProducts WHERE id_producto = ?";
        return db.select(query).parameters(id).get(rs -> {
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

            return new ProductEntity(idProducto, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, descuento);
        });
    }

    public Flowable<List<ProductEntity>> getAllProducts() {
        String query = "SELECT * FROM getAllProducts;";
        return db.select(query)
                .get(rs -> {
                    int id = rs.getInt("id_producto");
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int descuento = rs.getInt("porcentaje_descuento");

                    return new ProductEntity(id, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, descuento);
                }).toList().toFlowable();
    }

    public Flowable<List<ProductEntity>> getAllProductsBy(String category) {
        String query = "SELECT * FROM getAllProducts WHERE categoria = ?";
        return db.select(query).parameters(category)
                .get(rs -> {
                    int id = rs.getInt("id_producto");
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");
                    double precioCompra = rs.getDouble("precio_compra");
                    double precioVenta = rs.getDouble("precio_venta");
                    int descuento = rs.getInt("porcentaje_descuento");

                    return new ProductEntity(id, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, descuento);
                }).toList().toFlowable();
    }

    public Flowable<ProductEntity> createProduct(String codigoBarras, String nombre, String descripcion, String urlFoto, String formato, String categoria, double precioCompra, double precioVenta, int porcentajeDescuento) {
        String addProductQuery = "INSERT INTO producto (codigo_barras, nombre, descripcion, url_foto, formato, precio_compra, precio_venta, porcentaje_descuento) " +
                "VALUES (?, ?, ?, ?, '"+formato+"', ?, ?, ?) RETURNING *";
        return db.update(addProductQuery)
                .parameters(codigoBarras, nombre, descripcion, urlFoto, precioCompra, precioVenta, porcentajeDescuento)
                .returnGeneratedKeys()
                .get(rs -> {
                    int id = rs.getInt("id");
                    return new ProductEntity(id, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, porcentajeDescuento);
                })
                .flatMap(productEntity -> addProductToCategory(productEntity, categoria));
    }

    private Flowable<ProductEntity> addProductToCategory(ProductEntity productEntity, String categoria) {
        return categoriesRepository.getCategoryByName(categoria)
                .flatMap(categoryEntity -> {
                    String query = "INSERT INTO categoria_producto(id_categoria, id_producto) VALUES(?, ?) RETURNING *";
                    return db.select(query)
                            .parameters(categoryEntity.id, productEntity.id)
                            .get(rs -> new ProductEntity(productEntity.id, productEntity.codigoBarras, productEntity.nombre,
                                    productEntity.descripcion, productEntity.urlFoto, productEntity.formato, categoryEntity.nombre,
                                    productEntity.precioCompra, productEntity.precioVenta, productEntity.porcentajeDescuento)
                            );
                });
    }

    private Flowable<Integer> addProductToCategory(int productId, String categoria) {
        return categoriesRepository.getCategoryByName(categoria)
                .flatMap(categoryEntity -> {
                    String query = "INSERT INTO categoria_producto(id_categoria, id_producto) VALUES(?, ?) RETURNING *";
                    return db.select(query)
                            .parameters(categoryEntity.id, productId)
                            .get(rs -> rs.getInt("id_producto"));
                });
    }

    public Flowable<ProductEntity> updateProduct(ProductEntity oldProduct, String codigoBarras, String nombre, String descripcion, String urlFoto, String formato, String category, double precioCompra, double precioVenta, int porcentajeDescuento) {
        if (!oldProduct.categoria.equals(category)) {
            CategoryEntity categoryEntity = categoriesRepository.getCategoryByName(oldProduct.categoria).blockingLast();
            String deleteRow = "DELETE FROM categoria_producto WHERE id_producto = ? AND id_categoria = ?  RETURNING *";
            db.select(deleteRow)
                    .parameters(oldProduct.id, categoryEntity.id)
                    .get(rs -> "").blockingLast();
            addProductToCategory(oldProduct.id, category).blockingFirst();
        }
        String updateQuery = "UPDATE producto SET (codigo_barras, nombre, descripcion, url_foto, formato, precio_compra, precio_venta, porcentaje_descuento) " +
                "= (?, ?, ?, ?, '" + formato + "', ?, ?, ?) WHERE id = ? RETURNING *";
        return db.select(updateQuery)
                .parameters(codigoBarras, nombre, descripcion, urlFoto, precioCompra, precioVenta, porcentajeDescuento, oldProduct.id)
                .get(rs -> {
                    int id = rs.getInt("id");
                    return new ProductEntity(id, codigoBarras, nombre, descripcion, urlFoto, formato, category, precioCompra, precioVenta, porcentajeDescuento);
                });
    }

    public Flowable<Integer> deleteProductById(int id) {
        String deleteRow = "UPDATE producto SET is_deleted = true WHERE id = ? RETURNING *";
        return db.select(deleteRow)
                .parameters(id)
                .get(rs -> rs.getInt("id"));
    }
}
