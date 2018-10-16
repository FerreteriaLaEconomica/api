package api.data.products;

import api.data.categories.CategoriesRepository;
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

    @Inject
    public ProductsRepository(Database db, CategoriesRepository categoriesRepository) {
        this.db = db;
        this.categoriesRepository = categoriesRepository;
    }

    public Flowable<ProductEntity> getProductById(int id) {
        String query = "SELECT cp.id_categoria, cp.id_producto, p.codigo_barras, p.nombre, p.url_foto, p.descripcion, p.formato, c.nombre AS \"categoria\" " +
                "FROM categoria_producto cp " +
                "INNER JOIN producto p ON cp.id_producto = p.id " +
                "INNER JOIN categoria c ON cp.id_categoria = c.id " +
                "WHERE cp.id_producto = ?";
        return db.select(query).parameters(id).get(rs -> {
            int idProducto = rs.getInt("id_producto");
            String codigoBarras = rs.getString("codigo_barras");
            String nombre = rs.getString("nombre");
            String urlFoto = rs.getString("url_foto");
            String descripcion = rs.getString("descripcion");
            String formato = rs.getString("formato");
            String categoria = rs.getString("categoria");

            return new ProductEntity(idProducto, codigoBarras, nombre, descripcion, urlFoto, formato, categoria);
        }).onErrorReturn(throwable -> new ProductEntity.NoProduct());
    }

    public Flowable<List<ProductEntity>> getAllProducts() {
        String query = "SELECT cp.id_categoria, cp.id_producto, p.codigo_barras, p.nombre, p.url_foto, p.descripcion, p.formato, c.nombre AS \"categoria\" " +
                "FROM categoria_producto cp " +
                "INNER JOIN producto p ON cp.id_producto = p.id " +
                "INNER JOIN categoria c ON cp.id_categoria = c.id;";
        return db.select(query)
                .get(rs -> {
                    int id = rs.getInt("id_producto");
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");

                    return new ProductEntity(id, codigoBarras, nombre, descripcion, urlFoto, formato, categoria);
                }).toList().toFlowable();
    }

    public Flowable<List<ProductEntity>> getAllProductsBy(String category) {
        String query = "SELECT cp.id_categoria, cp.id_producto, p.codigo_barras, p.nombre, p.url_foto, p.descripcion, p.formato, c.nombre AS \"categoria\" " +
                "FROM categoria_producto cp " +
                "INNER JOIN producto p ON cp.id_producto = p.id " +
                "INNER JOIN categoria c ON cp.id_categoria = c.id " +
                "WHERE c.nombre = ?";
        return db.select(query).parameters(category)
                .get(rs -> {
                    int id = rs.getInt("id_producto");
                    String codigoBarras = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    String urlFoto = rs.getString("url_foto");
                    String descripcion = rs.getString("descripcion");
                    String formato = rs.getString("formato");
                    String categoria = rs.getString("categoria");

                    return new ProductEntity(id, codigoBarras, nombre, descripcion, urlFoto, formato, categoria);
                }).toList().toFlowable();
    }

    public Flowable<ProductEntity> createProduct(String codigoBarras, String nombre, String descripcion, String urlFoto, String formato, String categoria) {
        String addProductQuery = "INSERT INTO producto (codigo_barras, nombre, descripcion, url_foto, formato) " +
                "VALUES (?, ?, ?, ?, '"+formato+"') RETURNING id";
        return db.update(addProductQuery)
                .parameters(codigoBarras, nombre, descripcion, urlFoto)
                .returnGeneratedKeys()
                .get(rs -> {
                    int id = rs.getInt("id");
                    return new ProductEntity(id, codigoBarras, nombre, descripcion, urlFoto, formato);
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
                                    productEntity.descripcion, productEntity.urlFoto, productEntity.formato, categoryEntity.nombre)
                            );
                });
    }
}
