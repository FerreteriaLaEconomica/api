package api.data;

import io.reactivex.*;
import io.reactivex.functions.Function;
import org.davidmoten.rx.jdbc.Database;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Salvador Montiel on 07/oct/2018.
 */
@Singleton
public class UsersRepository {
    private Database db;

    @Inject
    public UsersRepository(Database db) {
        this.db = db;
    }

    public Flowable<UserEntity> getUserByEmail(String email) {
        return getRolesFromUser(email)
                .flatMap(roles -> db.select("SELECT * FROM usuario WHERE email = ?")
                        .parameters(email)
                        .get(rs -> {
                            String nombre = rs.getString("nombre");
                            String apellidos = rs.getString("apellidos");
                            String password = rs.getString("password");
                            String urlFoto = rs.getString("url_foto");
                            String telefono = rs.getString("telefono");
                            return new UserEntity(nombre, apellidos, email, password, urlFoto, telefono, roles);
                        }));
    }

    public Flowable<List<String>> getRolesFromUser(String email) {
        return db.select("SELECT r.nombre FROM role r, usuario u, user_role ur WHERE u.email = ? AND u.id = ur.id_usuario AND r.id = ur.id_role")
                .parameters(email)
                .get(rs -> rs.getString("nombre"))
                .toList().toFlowable();
    }

    public Flowable<Boolean> existsUserWithEmail(String email) {
        return db.select("SELECT COUNT(*) FROM usuario WHERE email = '" + email + "'")
                .get(rs -> rs.getInt("count") == 1);
    }

    public Flowable<Integer> saveUser(String nombre, String apellidos, String email, String password, String url_foto, String telefono) {
        return db.update("INSERT INTO usuario (nombre, apellidos, email, password, url_foto, telefono) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING *")
                .parameters(nombre, apellidos, email, password, url_foto, telefono)
                .returnGeneratedKeys().get(rs -> rs.getInt("id"));
    }
}
