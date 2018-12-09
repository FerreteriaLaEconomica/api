package api.data.ventas;

import api.controllers.users.UsuarioResponse;
import api.data.users.UserEntity;
import api.data.users.UsersRepository;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.davidmoten.rx.jdbc.Database;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Salvador Montiel on 07/dic/2018.
 */
@Singleton
public class OrdenRepository {
    private Database db;
    private UsersRepository usersRepo;

    @Inject
    public OrdenRepository(Database db, UsersRepository usersRepo) {
        this.db = db;
        this.usersRepo = usersRepo;
    }

    public Flowable<OrdenResponse> orderIsPaid(int idOrden, String emailComprador) {
        String query = "UPDATE orden SET estado_orden = 'PAGADO' WHERE id = ? RETURNING *";

        return usersRepo.getUserByEmail(emailComprador)
                .flatMap(user -> db.update(query).parameters(idOrden)
                        .returnGeneratedKeys()
                        .get(rs -> {
                            int id = rs.getInt("id");
                            double subtotal_ = rs.getDouble("subtotal");
                            double envio_ = rs.getDouble("envio");
                            String estadoOrden = rs.getString("estado_orden");
                            UsuarioResponse usuario = new UsuarioResponse(user.email, user.nombre, user.apellidos, user.url_foto, user.telefono, user.direccion, user.isSuperAdmin);
                            return new OrdenResponse(id, usuario, subtotal_, envio_, estadoOrden);
                        }));
    }

    public Flowable<OrdenResponse> createOrder(String emailComprador, double subtotal, double envio) {
        String addOrdenQuery = "INSERT INTO orden (id_comprador, subtotal, envio) " +
                "VALUES (?, ?, ?) RETURNING *";

        return usersRepo.getUserByEmail(emailComprador)
                .flatMap(user -> db.update(addOrdenQuery)
                        .parameters(user.id, subtotal, envio)
                        .returnGeneratedKeys()
                        .get(rs -> {
                            int id = rs.getInt("id");
                            double subtotal_ = rs.getDouble("subtotal");
                            double envio_ = rs.getDouble("envio");
                            String estadoOrden = rs.getString("estado_orden");
                            UsuarioResponse usuario = new UsuarioResponse(user.email, user.nombre, user.apellidos, user.url_foto, user.telefono, user.direccion, user.isSuperAdmin);
                            return new OrdenResponse(id, usuario, subtotal_, envio_, estadoOrden);
                        })
                );
    }
}
