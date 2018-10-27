package api.data.users;

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
    public final boolean isSuperAdmin;

    public UserEntity(String nombre, String apellidos, String email, String password, String url_foto, String telefono, boolean isSuperAdmin) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.url_foto = url_foto;
        this.telefono = telefono;
        this.isSuperAdmin = isSuperAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return isSuperAdmin == that.isSuperAdmin &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(apellidos, that.apellidos) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) &&
                Objects.equals(url_foto, that.url_foto) &&
                Objects.equals(telefono, that.telefono);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellidos, email, password, url_foto, telefono, isSuperAdmin);
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
                ", isSuperAdmin=" + isSuperAdmin +
                '}';
    }
}
