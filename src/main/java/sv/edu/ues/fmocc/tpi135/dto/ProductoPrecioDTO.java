package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO para transferir datos de precios de productos entre capas
 */
public class ProductoPrecioDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idProductoPrecio;
    private Long idProducto;
    private Date fechaDesde;
    private Date fechaHasta;
    private BigDecimal precioSugerido;
    private ProductoDTO producto;
    
    public ProductoPrecioDTO() {
    }
    
    public ProductoPrecioDTO(Long idProductoPrecio, Long idProducto, Date fechaDesde, Date fechaHasta, BigDecimal precioSugerido) {
        this.idProductoPrecio = idProductoPrecio;
        this.idProducto = idProducto;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.precioSugerido = precioSugerido;
    }

    public Long getIdProductoPrecio() {
        return idProductoPrecio;
    }

    public void setIdProductoPrecio(Long idProductoPrecio) {
        this.idProductoPrecio = idProductoPrecio;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public BigDecimal getPrecioSugerido() {
        return precioSugerido;
    }

    public void setPrecioSugerido(BigDecimal precioSugerido) {
        this.precioSugerido = precioSugerido;
    }
    
    public ProductoDTO getProducto() {
        return producto;
    }
    
    public void setProducto(ProductoDTO producto) {
        this.producto = producto;
    }
    
    @Override
    public String toString() {
        return "ProductoPrecioDTO{" +
                "idProductoPrecio=" + idProductoPrecio +
                ", idProducto=" + idProducto +
                ", fechaDesde=" + fechaDesde +
                ", fechaHasta=" + fechaHasta +
                ", precioSugerido=" + precioSugerido +
                '}';
    }
}