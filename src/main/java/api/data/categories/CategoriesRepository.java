package api.data.categories;

import io.reactivex.Flowable;
import org.davidmoten.rx.jdbc.Database;

import javax.inject.Inject;
import javax.inject.Singleton;

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
}
