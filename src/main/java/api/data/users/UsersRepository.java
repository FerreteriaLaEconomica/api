package api.data.users;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.davidmoten.rx.jdbc.Database;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    public boolean userIsAdmin(String userEmail) {
        return getUserByEmail(userEmail)
                .flatMap(userEntity -> {
                    String query = "select count(*) from sucursal where id_administrador = ?;";
                    return userEntity.isSuperAdmin ? Flowable.just(1) : db.select(query)
                            .parameters(userEntity.id)
                            .get(rs -> rs.getInt("count"));
                })
                .map(c -> c > 0)
                .blockingFirst(false);
        /*String query = "select count(*) from sucursal where id_administrador = ?;";
        return db.select(query)
                .parameters(userEmail)
                .get(rs -> rs.getInt("count"))
                .map(c -> c > 0)
                .blockingFirst(false);*/
    }

    public Flowable<UserEntity> addRoles(String email, boolean isSuperAdmin) {
        return updateUserData(email, "is_super_admin", isSuperAdmin)
                .flatMap(this::getUserByEmail);
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

    private Flowable<String> updateUserData(String email, String fieldName, Object value) {
        String val = value instanceof CharSequence ? "'" + value + "'" : value.toString();

        String query = "UPDATE usuario SET " + fieldName + " = " + val +
                " WHERE email = '" + email + "' RETURNING *";
        return db.update(query).returnGeneratedKeys()
                .get(rs -> rs.getString("email"));
    }

    public Flowable<UserEntity> getUserByEmail(String email) {
        return db.select("SELECT * FROM usuario WHERE email = ?")
                .parameters(email)
                .get(rs -> {
                    int id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    String apellidos = rs.getString("apellidos");
                    String password = rs.getString("password");
                    String urlFoto = rs.getString("url_foto");
                    String telefono = rs.getString("telefono");
                    boolean isSuperAdmin = rs.getBoolean("is_super_admin");
                    return new UserEntity(id, nombre, apellidos, email, password, urlFoto, telefono, isSuperAdmin);
                });
    }

    public Flowable<UserEntity> getUserById(int id) {
        return db.select("SELECT * FROM usuario WHERE id = ?")
                .parameters(id)
                .get(rs -> {
                    String nombre = rs.getString("nombre");
                    String email = rs.getString("email");
                    String apellidos = rs.getString("apellidos");
                    String password = rs.getString("password");
                    String urlFoto = rs.getString("url_foto");
                    String telefono = rs.getString("telefono");
                    boolean isSuperAdmin = rs.getBoolean("is_super_admin");
                    return new UserEntity(id, nombre, apellidos, email, password, urlFoto, telefono, isSuperAdmin);
                });
    }

    public Flowable<Boolean> existsUserWithEmail(String email) {
        return db.select("SELECT COUNT(*) FROM usuario WHERE email = '" + email + "'")
                .get(rs -> rs.getInt("count") == 1);
    }

    public Flowable<UserEntity> saveUser(String nombre, String apellidos, String email, String password, String url_foto, String telefono) {
        return db.update("INSERT INTO usuario (nombre, apellidos, email, password, url_foto, telefono) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING *")
                .parameters(nombre, apellidos, email, password, url_foto, telefono)
                .returnGeneratedKeys()
                .get(rs -> rs.getInt("id"))
                .flatMap(this::getUserById);
    }
}
