package api.data.products;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 14/oct/2018.
 */
public class ProductEntity {
    public final int id;
    @JsonProperty("codigo_barras")
    public final String codigoBarras;
    public final String nombre;
    public final String descripcion;
    @JsonProperty("url_foto")
    public final String urlFoto;
    public final String formato;
    public final String categoria;
    @JsonProperty("precio_compra")
    public final double precioCompra;
    @JsonProperty("precio_venta")
    public final double precioVenta;
    @JsonProperty("porcentaje_descuento")
    public final int porcentajeDescuento;

    public ProductEntity(int id, String codigoBarras, String nombre, String descripcion, String urlFoto, String formato, String categoria, double precioCompra, double precioVenta, int porcentajeDescuento) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlFoto = urlFoto;
        this.formato = formato;
        this.categoria = categoria;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductEntity that = (ProductEntity) o;
        return id == that.id &&
                Double.compare(that.precioCompra, precioCompra) == 0 &&
                Double.compare(that.precioVenta, precioVenta) == 0 &&
                porcentajeDescuento == that.porcentajeDescuento &&
                Objects.equals(codigoBarras, that.codigoBarras) &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(descripcion, that.descripcion) &&
                Objects.equals(urlFoto, that.urlFoto) &&
                Objects.equals(formato, that.formato) &&
                Objects.equals(categoria, that.categoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigoBarras, nombre, descripcion, urlFoto, formato, categoria, precioCompra, precioVenta, porcentajeDescuento);
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
                ", precioCompra=" + precioCompra +
                ", precioVenta=" + precioVenta +
                ", porcentajeDescuento=" + porcentajeDescuento +
                '}';
    }

    public static class NoProduct extends ProductEntity {
        public NoProduct() {
            super(-1, "","","","","", "", 0D, 0D, 0);
        }
    }
}
