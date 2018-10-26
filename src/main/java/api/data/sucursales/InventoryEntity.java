package api.data.sucursales;

import api.data.products.ProductEntity;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 19/oct/2018.
 */
public class InventoryEntity {
    public final int id;
    public final ProductEntity producto;
    public final int cantidad;
    public final double precioCompra;
    public final double precioVenta;
    public final int porcentajeDescuento;

    public InventoryEntity(int id, ProductEntity producto, int cantidad, double precioCompra, double precioVenta,
                           int porcentajeDescuento) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryEntity that = (InventoryEntity) o;
        return id == that.id &&
                cantidad == that.cantidad &&
                Double.compare(that.precioCompra, precioCompra) == 0 &&
                Double.compare(that.precioVenta, precioVenta) == 0 &&
                porcentajeDescuento == that.porcentajeDescuento &&
                Objects.equals(producto, that.producto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, producto, cantidad, precioCompra, precioVenta, porcentajeDescuento);
    }

    @Override
    public String toString() {
        return "InventaryEntity{" +
                "id=" + id +
                ", producto=" + producto +
                ", cantidad=" + cantidad +
                ", precioCompra=" + precioCompra +
                ", precioVenta=" + precioVenta +
                ", porcentajeDescuento=" + porcentajeDescuento +
                '}';
    }

    public static class NoInventory extends InventoryEntity {
        public NoInventory() {
            super(-1, null, -1, -1D, -1D, -1);
        }
    }
}
