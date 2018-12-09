package api.data.ventas;

import api.data.sucursales.InventoryEntity;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 07/dic/2018.
 */
public class ItemOrdenResponse {
    public final int id;
    public final OrdenResponse orden;
    public final InventoryEntity producto;
    public final int cantidad;
    public final double precio;

    public ItemOrdenResponse(int id, OrdenResponse orden, InventoryEntity producto, int cantidad, double precio) {
        this.id = id;
        this.orden = orden;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemOrdenResponse that = (ItemOrdenResponse) o;
        return id == that.id &&
                cantidad == that.cantidad &&
                Double.compare(that.precio, precio) == 0 &&
                Objects.equals(orden, that.orden) &&
                Objects.equals(producto, that.producto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orden, producto, cantidad, precio);
    }

    @Override
    public String toString() {
        return "ItemOrdenResponse{" +
                "id=" + id +
                ", orden=" + orden +
                ", producto=" + producto +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                '}';
    }
}
