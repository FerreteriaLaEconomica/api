package api.data.users;

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

    public Flowable<UserEntity> addRoles(String email, List<String> roles) {
        String getUserQuery = "SELECT id FROM usuario WHERE email = ?";
        Flowable<Integer> getUserId = db.select(getUserQuery).parameters(email)
                .get(rs -> rs.getInt("id"));

        return Flowable.fromIterable(roles)
                .flatMap(this::existsRole)
                .filter(roleId -> roleId != -1)
                .filter(roleId -> !existsUserWithRole(email, roleId).blockingLast())
                .flatMap(roleId -> {
                    int userId = getUserId.blockingLast();
                    String addUserRoleQuery = "INSERT INTO user_role(id_usuario, id_role) VALUES(?, ?) RETURNING *";
                    return db.select(addUserRoleQuery)
                            .parameters(userId, roleId)
                            .get(rs -> rs.getInt("id_usuario") + " - " + rs.getInt("id_role"));
                })
                .toList().toFlowable()
                .flatMap(userRoleIds -> {
                    System.out.println(userRoleIds.size() + " roles added to: " + email);
                    return getUserByEmail(email);
                });
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

    public Flowable<UserEntity> getUserById(int id) {
        return getRolesFromUserId(id)
                .flatMap(roles -> db.select("SELECT * FROM usuario WHERE id = ?")
                        .parameters(id)
                        .get(rs -> {
                            String nombre = rs.getString("nombre");
                            String email = rs.getString("email");
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

    public Flowable<List<String>> getRolesFromUserId(int id) {
        return db.select("SELECT r.nombre FROM role r, usuario u, user_role ur WHERE u.id = ? AND u.id = ur.id_usuario AND r.id = ur.id_role")
                .parameters(id)
                .get(rs -> rs.getString("nombre"))
                .toList().toFlowable();
    }

    public Flowable<Boolean> existsUserWithEmail(String email) {
        return db.select("SELECT COUNT(*) FROM usuario WHERE email = '" + email + "'")
                .get(rs -> rs.getInt("count") == 1);
    }

    public Flowable<Boolean> existsUserWithRole(String userEmail, int roleNameId) {
        String getUserQuery = "SELECT id FROM usuario WHERE email = ?";
        Flowable<Integer> getUserId = db.select(getUserQuery).parameters(userEmail)
                .get(rs -> rs.getInt("id"));
        return existsRole(roleNameId)
                .filter(roleId -> roleId != -1)
                .zipWith(getUserId, (roleId, userId) -> {
                    String query = "select count(*) from user_role where id_role = ? and id_usuario = ?";
                    return db.select(query).parameters(roleId, userId);
                })
                .flatMap(selectBuilder -> selectBuilder.get(rs -> rs.getInt("count") != 0))
                .onErrorReturn(throwable -> true);
    }

    public Flowable<Integer> existsRole(int id) {
        return db.select("SELECT id FROM role WHERE id = '" + id + "'")
                .get(rs -> rs.getInt("id"))
                .onErrorReturn(throwable -> -1);
    }

    public Flowable<Integer> existsRole(String name) {
        return db.select("SELECT id FROM role WHERE nombre = '" + name + "'")
                .get(rs -> rs.getInt("id"))
                .onErrorReturn(throwable -> -1);
    }

    public Flowable<Integer> saveUser(String nombre, String apellidos, String email, String password, String url_foto, String telefono) {
        return db.update("INSERT INTO usuario (nombre, apellidos, email, password, url_foto, telefono) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING *")
                .parameters(nombre, apellidos, email, password, url_foto, telefono)
                .returnGeneratedKeys().get(rs -> rs.getInt("id"));
    }
}
