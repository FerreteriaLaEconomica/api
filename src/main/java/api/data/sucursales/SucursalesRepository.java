package api.data.sucursales;

import api.controllers.users.UsuarioResponse;
import api.data.users.UsersRepository;
import io.reactivex.Flowable;
import org.davidmoten.rx.jdbc.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Salvador Montiel on 18/oct/2018.
 */
@Singleton
public class SucursalesRepository {
    private Database db;
    private UsersRepository usersRepo;
    private InventoryRepository inventoryRepo;

    @Inject
    public SucursalesRepository(Database db, UsersRepository usersRepo, InventoryRepository inventoryRepo) {
        this.db = db;
        this.usersRepo = usersRepo;
        this.inventoryRepo = inventoryRepo;
    }

    public Flowable<Integer> deleteSucursalById(int id) {
        String deleteRow = "DELETE FROM sucursal WHERE id = ? RETURNING *";
        return db.select(deleteRow)
                .parameters(id)
                .get(rs -> rs.getInt("id"));
    }

    public Flowable<SucursalEntity> createSucursal(String nombre, String calle, String numeroExterior, String colonia,
                                                   int codigoPostal, String localidad, String municipio,
                                                   String estado, String emailAdmin) {
        AdminEntity adminEntity = getAdminByEmail(emailAdmin);

        String addSucursalQuery = "INSERT INTO sucursal (nombre, calle, numero_exterior, colonia, codigo_postal, " +
                "localidad, municipio, estado, id_administrador) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        return db.update(addSucursalQuery)
                .parameters(nombre, calle, numeroExterior, colonia, codigoPostal, localidad, municipio, estado, adminEntity.id)
                .returnGeneratedKeys()
                .get(rs -> {
                    int id = rs.getInt("id");
                    return new SucursalEntity.Builder()
                            .id(id)
                            .nombre(nombre)
                            .calle(calle)
                            .numeroExterior(numeroExterior)
                            .colonia(colonia)
                            .codigoPostal(codigoPostal)
                            .localidad(localidad)
                            .municipio(municipio)
                            .estado(estado)
                            .administrador(adminEntity)
                            .build();
                });
    }

    public Flowable<SucursalEntity> getSucursalByName(String name) {
        String query = "SELECT * FROM sucursal WHERE nombre = ?";
        return db.select(query)
                .parameters(name)
                .get(rs -> {
                    int idAdmin = rs.getInt("id_administrador");
                    AdminEntity admin = getAdminById(idAdmin);
                    return new SucursalEntity.Builder()
                            .id(rs.getInt("id"))
                            .nombre(rs.getString("nombre"))
                            .calle(rs.getString("calle"))
                            .numeroExterior(rs.getString("numero_exterior"))
                            .colonia(rs.getString("colonia"))
                            .codigoPostal(rs.getInt("codigo_postal"))
                            .localidad(rs.getString("localidad"))
                            .municipio(rs.getString("municipio"))
                            .estado(rs.getString("estado"))
                            .administrador(admin)
                            .build();
                });
    }

    public Flowable<SucursalEntity> getSucursalById(int id) {
        String query = "SELECT * FROM sucursal WHERE id = ?";
        return db.select(query)
                .parameters(id)
                .get(rs -> {
                    int idAdmin = rs.getInt("id_administrador");
                    AdminEntity admin = getAdminById(idAdmin);
                    return new SucursalEntity.Builder()
                            .id(rs.getInt("id"))
                            .nombre(rs.getString("nombre"))
                            .calle(rs.getString("calle"))
                            .numeroExterior(rs.getString("numero_exterior"))
                            .colonia(rs.getString("colonia"))
                            .codigoPostal(rs.getInt("codigo_postal"))
                            .localidad(rs.getString("localidad"))
                            .municipio(rs.getString("municipio"))
                            .estado(rs.getString("estado"))
                            .administrador(admin)
                            .build();
                });
    }

    public Flowable<List<SucursalEntity>> getAllSucursales() {
        String query = "SELECT * FROM sucursal";
        return db.select(query)
                .get(rs -> {
                    int idAdmin = rs.getInt("id_administrador");
                    AdminEntity admin = getAdminById(idAdmin);
                    return new SucursalEntity.Builder()
                            .id(rs.getInt("id"))
                            .nombre(rs.getString("nombre"))
                            .calle(rs.getString("calle"))
                            .numeroExterior(rs.getString("numero_exterior"))
                            .colonia(rs.getString("colonia"))
                            .codigoPostal(rs.getInt("codigo_postal"))
                            .localidad(rs.getString("localidad"))
                            .municipio(rs.getString("municipio"))
                            .estado(rs.getString("estado"))
                            .administrador(admin)
                            .build();
                })
                .toList()
                .toFlowable();
    }

    private AdminEntity getAdminById(int id) {
        return usersRepo.getUserById(id)
                .map(u -> new AdminEntity(u.id, u.nombre, u.apellidos, u.email, u.telefono, u.url_foto))
                .blockingLast();
    }

    private AdminEntity getAdminByEmail(String email) {
        return usersRepo.getUserByEmail(email)
                .map(u -> new AdminEntity(u.id, u.nombre, u.apellidos, u.email, u.telefono, u.url_foto))
                .blockingLast();
    }

    public Flowable<List<SucursalEntity>> getSucursalesByAdmin(UsuarioResponse user) {
        if (user.is_super_admin) return getAllSucursales();
        return getAllSucursales().flatMapIterable(sucursalEntities -> sucursalEntities)
                .filter(sucursalEntity -> sucursalEntity.administrador.email.equals(user.email))
                .toList()
                .toFlowable();
    }
}
