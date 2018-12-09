package api.data.ventas;

import api.controllers.users.UsuarioResponse;
import api.data.users.UserEntity;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 07/dic/2018.
 */
public class OrdenResponse {
    public final int id;
    public final UsuarioResponse comprador;
    public final double subtotal;
    public final double envio;
    public final String estado_orden;

    public OrdenResponse(int id, UsuarioResponse comprador, double subtotal, double envio, String estado_orden) {
        this.id = id;
        this.comprador = comprador;
        this.subtotal = subtotal;
        this.envio = envio;
        this.estado_orden = estado_orden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenResponse that = (OrdenResponse) o;
        return id == that.id &&
                Double.compare(that.subtotal, subtotal) == 0 &&
                Double.compare(that.envio, envio) == 0 &&
                Objects.equals(comprador, that.comprador) &&
                Objects.equals(estado_orden, that.estado_orden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, comprador, subtotal, envio, estado_orden);
    }

    @Override
    public String toString() {
        return "OrdenResponse{" +
                "id=" + id +
                ", comprador=" + comprador +
                ", subtotal=" + subtotal +
                ", envio=" + envio +
                ", estado_orden='" + estado_orden + '\'' +
                '}';
    }
}
