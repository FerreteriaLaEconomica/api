package api.controllers.users;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 11/oct/2018.
 */
public class UsuarioResponse {
    public final String email;
    public final String nombre;
    public final String apellidos;
    public final String url_foto;
    public final String telefono;
    public final String direccion;
    public final boolean is_super_admin;

    public UsuarioResponse(String email, String nombre, String apellidos, String url_foto, String telefono, String direccion) {
        this(email, nombre, apellidos, url_foto, telefono, direccion, false);
    }

    public UsuarioResponse(String email, String nombre, String apellidos, String url_foto, String telefono, String direccion, boolean isSuperAdmin) {
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.url_foto = url_foto;
        this.telefono = telefono;
        this.direccion = direccion;
        this.is_super_admin = isSuperAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioResponse that = (UsuarioResponse) o;
        return is_super_admin == that.is_super_admin &&
                Objects.equals(email, that.email) &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(apellidos, that.apellidos) &&
                Objects.equals(url_foto, that.url_foto) &&
                Objects.equals(telefono, that.telefono) &&
                Objects.equals(direccion, that.direccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, nombre, apellidos, url_foto, telefono, direccion, is_super_admin);
    }

    @Override
    public String toString() {
        return "UsuarioResponse{" +
                "email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", url_foto='" + url_foto + '\'' +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                ", is_super_admin=" + is_super_admin +
                '}';
    }
}
