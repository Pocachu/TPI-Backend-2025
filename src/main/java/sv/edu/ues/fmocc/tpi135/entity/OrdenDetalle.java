package sv.edu.ues.fmocc.tpi135.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entidad JPA que representa el detalle de una orden en el sistema.
 */
@Entity
@Table(name = "orden_detalle")
@IdClass(OrdenDetallePK.class)
@NamedQueries({
    @NamedQuery(name = "OrdenDetalle.findAll", query = "SELECT od FROM OrdenDetalle od"),
    @NamedQuery(name = "OrdenDetalle.findByIdOrden", query = "SELECT od FROM OrdenDetalle od WHERE od.idOrden = :idOrden"),
    @NamedQuery(name = "OrdenDetalle.findByIdProductoPrecio", query = "SELECT od FROM OrdenDetalle od WHERE od.idProductoPrecio = :idProductoPrecio")
})
public class OrdenDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "id_orden")
    private Long idOrden;
    
    @Id
    @Column(name = "id_producto_precio")
    private Long idProductoPrecio;
    
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    @Column(name = "precio", precision = 6, scale = 2)
    private BigDecimal precio;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    @ManyToOne
    @JoinColumn(name = "id_orden", referencedColumnName = "id_orden", insertable = false, updatable = false)
    private Orden orden;
    
    @ManyToOne
    @JoinColumn(name = "id_producto_precio", referencedColumnName = "id_producto_precio", insertable = false, updatable = false)
    private ProductoPrecio productoPrecio;
    
    public OrdenDetalle() {
    }
    
    public OrdenDetalle(Long idOrden, Long idProductoPrecio) {
        this.idOrden = idOrden;
        this.idProductoPrecio = idProductoPrecio;
    }
    
    public OrdenDetalle(Long idOrden, Long idProductoPrecio, Integer cantidad, BigDecimal precio, String observaciones) {
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
    
    public Orden getOrden() {
        return orden;
    }
    
    public void setOrden(Orden orden) {
        this.orden = orden;
    }
    
    public ProductoPrecio getProductoPrecio() {
        return productoPrecio;
    }
    
    public void setProductoPrecio(ProductoPrecio productoPrecio) {
        this.productoPrecio = productoPrecio;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrden != null ? idOrden.hashCode() : 0);
        hash += (idProductoPrecio != null ? idProductoPrecio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof OrdenDetalle)) {
            return false;
        }
        OrdenDetalle other = (OrdenDetalle) object;
        if ((this.idOrden == null && other.idOrden != null) || 
                (this.idOrden != null && !this.idOrden.equals(other.idOrden))) {
            return false;
        }
        return !((this.idProductoPrecio == null && other.idProductoPrecio != null) || 
                (this.idProductoPrecio != null && !this.idProductoPrecio.equals(other.idProductoPrecio)));
    }

    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.OrdenDetalle[ idOrden=" + idOrden + ", idProductoPrecio=" + idProductoPrecio + " ]";
    }
}