package api.data.sucursales;

import java.util.Objects;

/**
 * Created by Salvador Montiel on 18/oct/2018.
 */
public class SucursalEntity {
    public final int id;
    public final String nombre;
    public final String calle;
    public final String numeroExterior;
    public final String colonia;
    public final int codigoPostal;
    public final String localidad;
    public final String municipio;
    public final String estado;
    public final AdminEntity administrador;

    private SucursalEntity(Builder builder) {
        id = builder.id;
        nombre = builder.nombre;
        calle = builder.calle;
        numeroExterior = builder.numeroExterior;
        colonia = builder.colonia;
        codigoPostal = builder.codigoPostal;
        localidad = builder.localidad;
        municipio = builder.municipio;
        estado = builder.estado;
        administrador = builder.administrador;
    }

    public static final class Builder {
        private int id;
        private String nombre;
        private String calle;
        private String numeroExterior;
        private String colonia;
        private int codigoPostal;
        private String localidad;
        private String municipio;
        private String estado;
        private AdminEntity administrador;

        public Builder() {}

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder calle(String calle) {
            this.calle = calle;
            return this;
        }

        public Builder numeroExterior(String numeroExterior) {
            this.numeroExterior = numeroExterior;
            return this;
        }

        public Builder colonia(String colonia) {
            this.colonia = colonia;
            return this;
        }

        public Builder codigoPostal(int codigoPostal) {
            this.codigoPostal = codigoPostal;
            return this;
        }

        public Builder localidad(String localidad) {
            this.localidad = localidad;
            return this;
        }

        public Builder municipio(String municipio) {
            this.municipio = municipio;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public Builder administrador(AdminEntity administrador) {
            this.administrador = administrador;
            return this;
        }

        public SucursalEntity build() {
            return new SucursalEntity(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SucursalEntity that = (SucursalEntity) o;
        return id == that.id &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(calle, that.calle) &&
                Objects.equals(numeroExterior, that.numeroExterior) &&
                Objects.equals(colonia, that.colonia) &&
                Objects.equals(codigoPostal, that.codigoPostal) &&
                Objects.equals(localidad, that.localidad) &&
                Objects.equals(municipio, that.municipio) &&
                Objects.equals(estado, that.estado) &&
                Objects.equals(administrador, that.administrador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, calle, numeroExterior, colonia, codigoPostal, localidad, municipio, estado, administrador);
    }

    @Override
    public String toString() {
        return "SucursalEntity{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", calle='" + calle + '\'' +
                ", numeroExterior='" + numeroExterior + '\'' +
                ", colonia='" + colonia + '\'' +
                ", codigoPostal='" + codigoPostal + '\'' +
                ", localidad='" + localidad + '\'' +
                ", municipio='" + municipio + '\'' +
                ", estado='" + estado + '\'' +
                ", administrador=" + administrador +
                '}';
    }

    public static class NoSucursal extends SucursalEntity {
        public NoSucursal() {
            super(new SucursalEntity.Builder());
        }
    }
}
