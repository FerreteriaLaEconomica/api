package api.data.sucursales;

import api.data.products.ProductEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 19/oct/2018.
 */
public class InventoryEntity {
    public final int id;
    public final ProductEntity producto;
    public final int cantidad;
    @JsonProperty("id_sucursal")
    public final int idSucursal;

    public InventoryEntity(int id, ProductEntity producto, int cantidad, int idSucursal) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.idSucursal = idSucursal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryEntity that = (InventoryEntity) o;
        return id == that.id &&
                cantidad == that.cantidad &&
                idSucursal == that.idSucursal &&
                Objects.equals(producto, that.producto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, producto, cantidad, idSucursal);
    }

    @Override
    public String toString() {
        return "InventoryEntity{" +
                "id=" + id +
                ", producto=" + producto +
                ", cantidad=" + cantidad +
                ", idSucursal=" + idSucursal +
                '}';
    }

    public static class NoInventory extends InventoryEntity {
        public NoInventory() {
            super(-1, null, -1, -1);
        }
    }
}
