package api.data.ventas;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 07/dic/2018.
 */
public class ItemOrdenEntity {
    public final int id;
    public final int id_orden;
    public final int id_producto;
    public final int cantidad;
    public final double precio;

    public ItemOrdenEntity(int id, int id_orden, int id_producto, int cantidad, double precio) {
        this.id = id;
        this.id_orden = id_orden;
        this.id_producto = id_producto;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemOrdenEntity that = (ItemOrdenEntity) o;
        return id == that.id &&
                id_orden == that.id_orden &&
                id_producto == that.id_producto &&
                cantidad == that.cantidad &&
                Double.compare(that.precio, precio) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, id_orden, id_producto, cantidad, precio);
    }

    @Override
    public String toString() {
        return "ItemOrdenResponse{" +
                "id=" + id +
                ", id_orden=" + id_orden +
                ", id_producto=" + id_producto +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                '}';
    }
}
