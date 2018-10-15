package api.data.products;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 14/oct/2018.
 */
public class ProductEntity {
    public final int id;
    public final String codigoBarras;
    public final String nombre;
    public final String descripcion;
    public final String urlFoto;
    public final String formato;
    public final String categoria;

    public ProductEntity(int id, String codigoBarras, String nombre, String descripcion, String urlFoto, String formato) {
        this(id, codigoBarras, nombre, descripcion, urlFoto, formato, null);
    }
    public ProductEntity(int id, String codigoBarras, String nombre, String descripcion, String urlFoto, String formato, String categoria) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlFoto = urlFoto;
        this.formato = formato;
        this.categoria = categoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductEntity that = (ProductEntity) o;
        return id == that.id &&
                Objects.equals(codigoBarras, that.codigoBarras) &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(descripcion, that.descripcion) &&
                Objects.equals(urlFoto, that.urlFoto) &&
                Objects.equals(formato, that.formato) &&
                Objects.equals(categoria, that.categoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigoBarras, nombre, descripcion, urlFoto, formato, categoria);
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", codigoBarras='" + codigoBarras + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", urlFoto='" + urlFoto + '\'' +
                ", formato='" + formato + '\'' +
                ", categoria='" + categoria + '\'' +
                '}';
    }

    public static class NoProduct extends ProductEntity {
        public NoProduct() {
            super(-1, "","","","","", "");
        }
    }
}
