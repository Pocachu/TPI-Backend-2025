package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO para transferir datos de detalles de órdenes entre capas
 */
public class OrdenDetalleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idOrden;
    private Long idProductoPrecio;
    private Integer cantidad;
    private BigDecimal precio;
    private String observaciones;
    private ProductoPrecioDTO productoPrecio;
    
    public OrdenDetalleDTO() {
    }
    
    public OrdenDetalleDTO(Long idOrden, Long idProductoPrecio, Integer cantidad, BigDecimal precio, String observaciones) {
        this.idOrden = idOrden;
        this.idProductoPrecio = idProductoPrecio;
        this.cantidad = cantidad;
        this.precio = precio;
        this.observaciones = observaciones;
    }

    public Long getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Long idOrden) {
        this.idOrden = idOrden;
    }

    public Long getIdProductoPrecio() {
        return idProductoPrecio;
    }

    public void setIdProductoPrecio(Long idProductoPrecio) {
        this.idProductoPrecio = idProductoPrecio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public ProductoPrecioDTO getProductoPrecio() {
        return productoPrecio;
    }
    
    public void setProductoPrecio(ProductoPrecioDTO productoPrecio) {
        this.productoPrecio = productoPrecio;
    }
    
    @Override
    public String toString() {
        return "OrdenDetalleDTO{" +
                "idOrden=" + idOrden +
                ", idProductoPrecio=" + idProductoPrecio +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}