package api.data.ventas;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 07/dic/2018.
 */
public class OrdenEntity {
    public final int id;
    public final int id_comprador;
    public final double subtotal;
    public final double envio;
    public final String estado_orden;

    public OrdenEntity(int id, int id_comprador, double subtotal, double envio, String estado_orden) {
        this.id = id;
        this.id_comprador = id_comprador;
        this.subtotal = subtotal;
        this.envio = envio;
        this.estado_orden = estado_orden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenEntity that = (OrdenEntity) o;
        return id == that.id &&
                id_comprador == that.id_comprador &&
                Double.compare(that.subtotal, subtotal) == 0 &&
                Double.compare(that.envio, envio) == 0 &&
                Objects.equals(estado_orden, that.estado_orden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, id_comprador, subtotal, envio, estado_orden);
    }

    @Override
    public String toString() {
        return "OrdenEntity{" +
                "id=" + id +
                ", id_comprador=" + id_comprador +
                ", subtotal=" + subtotal +
                ", envio=" + envio +
                ", estado_orden='" + estado_orden + '\'' +
                '}';
    }
}
