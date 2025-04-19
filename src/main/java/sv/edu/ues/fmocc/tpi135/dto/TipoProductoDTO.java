package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;

/**
 * DTO para transferir datos de tipos de productos entre capas
 */
public class TipoProductoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer idTipoProducto;
    private String nombre;
    private Boolean activo;
    private String observaciones;
    
    public TipoProductoDTO() {
    }
    
    public TipoProductoDTO(Integer idTipoProducto, String nombre, Boolean activo, String observaciones) {
        this.idTipoProducto = idTipoProducto;
        this.nombre = nombre;
        this.activo = activo;
        this.observaciones = observaciones;
    }

    public Integer getIdTipoProducto() {
        return idTipoProducto;
    }

    public void setIdTipoProducto(Integer idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
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
        return "TipoProductoDTO{" +
                "idTipoProducto=" + idTipoProducto +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}