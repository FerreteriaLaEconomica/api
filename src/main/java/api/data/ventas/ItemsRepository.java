package api.data.ventas;

import api.data.sucursales.InventoryEntity;
import api.data.sucursales.InventoryRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import org.davidmoten.rx.jdbc.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Salvador Montiel on 07/dic/2018.
 */
@Singleton
public class ItemsRepository {
    private Database db;
    private InventoryRepository inventoryRepo;

    public ItemsRepository(Database db, InventoryRepository inventoryRepo) {
        this.db = db;
        this.inventoryRepo = inventoryRepo;
    }

    public Completable addItemsToOrder(int idOrden, List<Integer> productos, List<Integer> cantidades, List<Double> precios) {
        checkIfThereAreSufficientItems(productos, cantidades);
        updateInventory(productos, cantidades);

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO item_orden (id_orden, id_producto_inventario, cantidad, precio) VALUES ");
        for (int i = 0; i < productos.size(); i++) {
            sb.append("(").append(idOrden).append(", ").append(productos.get(i)).append(", ").append(cantidades.get(i))
                    .append(", ").append(precios.get(i)).append(")");
            if (productos.size() != i + 1) sb.append(", ");
        }
        return db.update(sb.toString())
                .complete();
    }

    private void updateInventory(List<Integer> productos, List<Integer> cantidades) {
        for (int i = 0; i < productos.size(); i++) {
            inventoryRepo.updateInventoryById(productos.get(i), (cantidades.get(i) * -1))
                    .subscribe(System.out::println, throwable -> throwable.printStackTrace());
        }
    }

    private void checkIfThereAreSufficientItems(List<Integer> productos, List<Integer> cantidades) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM inventario i WHERE id IN (");
        for (int i = 0; i < productos.size(); i++) {
            sb.append(productos.get(i));
            if (productos.size() != i + 1) sb.append(", ");
        }
        sb.append(")");
        db.select(sb.toString())
                .get(rs -> rs.getInt("cantidad"))
                .toList()
                .zipWith(Single.just(cantidades), new BiFunction<List<Integer>, List<Integer>, Boolean>() {
                    @Override
                    public Boolean apply(List<Integer> productos, List<Integer> cantidades) throws Exception {
                        for (int i = 0; i < productos.size(); i++) {
                            int inDb = productos.get(i);
                            int minus = cantidades.get(i);
                            if (inDb - minus < 0) throw new RuntimeException("No hay cantidad suficiente en inventario.");
                        }
                        return true;
                    }
                }).blockingGet();
    }
}
