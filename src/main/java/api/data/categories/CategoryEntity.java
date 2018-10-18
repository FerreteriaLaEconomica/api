package api.data.categories;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 15/oct/2018.
 */
public class CategoryEntity {
    public final int id;
    public final String nombre;

    public CategoryEntity(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryEntity that = (CategoryEntity) o;
        return id == that.id &&
                Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }

    public static class NoCategory extends CategoryEntity {
        public NoCategory() {
            super(-1, "");
        }
    }
}
