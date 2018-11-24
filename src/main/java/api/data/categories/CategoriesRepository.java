package api.data.categories;

import io.reactivex.Flowable;
import org.davidmoten.rx.jdbc.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Salvador Montiel on 15/oct/2018.
 */
@Singleton
public class CategoriesRepository {
    private Database db;

    @Inject
    public CategoriesRepository(Database db) {
        this.db = db;
    }

    public Flowable<CategoryEntity> getCategoryByName(String name) {
        String query = "SELECT id, nombre FROM categoria WHERE nombre = ?";
        return db.select(query)
                .parameters(name)
                .get(rs -> {
                    int id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    return new CategoryEntity(id, nombre);
                })
                .onErrorReturnItem(new CategoryEntity.NoCategory());
    }

    public Flowable<List<CategoryEntity>> getAllCategories() {
        String query = "SELECT id, nombre FROM categoria";
        return db.select(query)
                .get(rs -> {
                    int id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    return new CategoryEntity(id, nombre);
                })
                .toList()
                .toFlowable();
    }

    public Flowable<CategoryEntity> getCategoryById(int id) {
        String query = "SELECT id, nombre FROM categoria WHERE id = ?";
        return db.select(query)
                .parameters(id)
                .get(rs -> {
                    String nombre = rs.getString("nombre");
                    return new CategoryEntity(id, nombre);
                })
                .onErrorReturnItem(new CategoryEntity.NoCategory());
    }

    public Flowable<CategoryEntity> createCategory(String nombre) {
        String addProductQuery = "INSERT INTO categoria (nombre) " +
                "VALUES (?) RETURNING id";
        return db.update(addProductQuery)
                .parameters(nombre)
                .returnGeneratedKeys()
                .get(rs -> {
                    int id = rs.getInt("id");
                    return new CategoryEntity(id, nombre);
                });
    }

    public Flowable<CategoryEntity> updateCategory(int id, String nombre) {
        String updateQuery = "UPDATE categoria SET nombre = ? WHERE id = ? " +
                "RETURNING *";
        return db.select(updateQuery)
                .parameters(nombre, id)
                .get(rs -> {
                    int i = rs.getInt("id");
                    String n = rs.getString("nombre");
                    return new CategoryEntity(i, n);
                });
    }

    public Flowable<Integer> deleteCategoryById(int id) {
        String deleteRow = "DELETE FROM categoria WHERE id = ? RETURNING *";
        return db.select(deleteRow)
                .parameters(id)
                .get(rs -> rs.getInt("id"));
    }
}
