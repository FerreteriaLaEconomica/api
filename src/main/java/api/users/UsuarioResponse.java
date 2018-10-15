package api.users;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Salvador Montiel on 11/oct/2018.
 */
public class UsuarioResponse {
    public final String email;
    public final String token;
    public final String nombre;
    public final String apellidos;
    public final String url_foto;
    public final String telefono;
    public final List<String> roles;

    public UsuarioResponse(String email, String token, String nombre, String apellidos, String url_foto, String telefono) {
        this(email, token, nombre, apellidos, url_foto, telefono, Arrays.asList());
    }

    public UsuarioResponse(String email, String token, String nombre, String apellidos, String url_foto, String telefono, List<String> roles) {
        this.email = email;
        this.token = token;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.url_foto = url_foto;
        this.telefono = telefono;
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioResponse that = (UsuarioResponse) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(token, that.token) &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(apellidos, that.apellidos) &&
                Objects.equals(url_foto, that.url_foto) &&
                Objects.equals(telefono, that.telefono) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, token, nombre, apellidos, url_foto, telefono, roles);
    }

    @Override
    public String toString() {
        return "UsuarioResponse{" +
                "email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", url_foto='" + url_foto + '\'' +
                ", telefono='" + telefono + '\'' +
                ", roles=" + roles +
                '}';
    }
}
