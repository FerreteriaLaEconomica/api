package api.data.users;

import java.util.List;
import java.util.Objects;

/**
 * Created by Salvador Montiel on 07/oct/2018.
 */
public class UserEntity {
    public final String nombre;
    public final String apellidos;
    public final String email;
    public final String password;
    public final String url_foto;
    public final String telefono;
    public final List<String> roles;

    public UserEntity(String nombre, String apellidos, String email, String password, String url_foto, String telefono, List<String> roles) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.url_foto = url_foto;
        this.telefono = telefono;
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(nombre, that.nombre) &&
                Objects.equals(apellidos, that.apellidos) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) &&
                Objects.equals(url_foto, that.url_foto) &&
                Objects.equals(telefono, that.telefono) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellidos, email, password, url_foto, telefono, roles);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", url_foto='" + url_foto + '\'' +
                ", telefono='" + telefono + '\'' +
                ", roles=" + roles +
                '}';
    }
}
