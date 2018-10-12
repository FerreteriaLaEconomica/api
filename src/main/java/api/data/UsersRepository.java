package api.data;

import io.reactivex.Flowable;
import org.davidmoten.rx.jdbc.Database;

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

    public Flowable<UserEntity> updateUserData(String email, String nombre, String apellidos, String urlFoto, String telefono) {
        Flowable<String> result = null;
        if (nombre != null) {
            result = updateUserData(email, "nombre", nombre);
        }
        if (apellidos != null) {
            if (result == null) result = updateUserData(email, "apellidos", apellidos);
            else result = result.flatMap(upstream -> updateUserData(email, "apellidos", apellidos));
        }
        if (urlFoto != null) {
            if (result == null) result = updateUserData(email, "url_foto", urlFoto);
            else result = result.flatMap(upstream -> updateUserData(email, "url_foto", urlFoto));
        }
        if (telefono != null) {
            if (result == null) result = updateUserData(email, "telefono", telefono);
            else result = result.flatMap(upstream -> updateUserData(email, "telefono", telefono));
        }
        if (result == null) return getUserByEmail(email);
        return result.flatMap(this::getUserByEmail);
    }

    private Flowable<String> updateUserData(String email, String fieldName, String value) {
        String query = "UPDATE usuario SET " + fieldName + " = '" + value + "' " +
                "WHERE email = '" + email + "'";
        return db.update(query).returnGeneratedKeys()
                .get(rs -> rs.getString("email"));
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
