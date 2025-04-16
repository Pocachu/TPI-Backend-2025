package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;

/**
 * DTO para transferir datos de productos entre capas
 */
public class ProductoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idProducto;
    private String nombre;
    private Boolean activo;
    private String observaciones;
    
    public ProductoDTO() {
    }
    
    public ProductoDTO(Long idProducto, String nombre, Boolean activo, String observaciones) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.activo = activo;
        this.observaciones = observaciones;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    @Override
    public String toString() {
        return "ProductoDTO{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}