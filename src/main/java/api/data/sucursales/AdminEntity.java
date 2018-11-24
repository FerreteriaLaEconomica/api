package api.data.sucursales;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 18/oct/2018.
 */
public class AdminEntity {
    public final int id;
    public final String nombre;
    public final String apellidos;
    public final String email;
    public final String telefono;
    @JsonProperty("url_foto")
    public final String urlFoto;

    public AdminEntity(int id, String nombre, String apellidos, String email, String telefono, String urlFoto) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.urlFoto = urlFoto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminEntity that = (AdminEntity) o;
        return id == that.id &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(apellidos, that.apellidos) &&
                Objects.equals(email, that.email) &&
                Objects.equals(telefono, that.telefono) &&
                Objects.equals(urlFoto, that.urlFoto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, apellidos, email, telefono, urlFoto);
    }

    @Override
    public String toString() {
        return "AdminEntity{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", urlFoto='" + urlFoto + '\'' +
                '}';
    }
}
