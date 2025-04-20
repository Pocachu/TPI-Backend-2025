package sv.edu.ues.fmocc.tpi135.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entidad JPA que representa el precio de un producto en el sistema.
 * Esta entidad permite gestionar el historial de precios de cada producto,
 * manteniendo un registro cronológico de los diferentes precios que ha tenido.
 */
@Entity
@Table(name = "producto_precio")
@NamedQueries({
    @NamedQuery(name = "ProductoPrecio.findAll", query = "SELECT pp FROM ProductoPrecio pp"),
    @NamedQuery(name = "ProductoPrecio.findById", query = "SELECT pp FROM ProductoPrecio pp WHERE pp.idProductoPrecio = :idProductoPrecio"),
    @NamedQuery(name = "ProductoPrecio.findByIdProducto", query = "SELECT pp FROM ProductoPrecio pp WHERE pp.idProducto = :idProducto"),
    @NamedQuery(name = "ProductoPrecio.findVigentesByFecha", query = "SELECT pp FROM ProductoPrecio pp WHERE (pp.fechaHasta IS NULL OR pp.fechaHasta >= :fecha) AND pp.fechaDesde <= :fecha"),
    @NamedQuery(name = "ProductoPrecio.findVigente", query = "SELECT pp FROM ProductoPrecio pp WHERE pp.idProducto = :idProducto AND (pp.fechaHasta IS NULL OR pp.fechaHasta >= :fecha) AND pp.fechaDesde <= :fecha")
})
public class ProductoPrecio implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_precio")
    private Long idProductoPrecio;
    
    @Column(name = "id_producto")
    private Long idProducto;
    
    @Column(name = "fecha_desde")
    @Temporal(TemporalType.DATE)
    private Date fechaDesde;
    
    @Column(name = "fecha_hasta")
    @Temporal(TemporalType.DATE)
    private Date fechaHasta;
    
    @Column(name = "precio_sugerido", precision = 8, scale = 2)
    private BigDecimal precioSugerido;
    
    @ManyToOne
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    private Producto producto;
    
    public ProductoPrecio() {
    }
    
    public ProductoPrecio(Long idProductoPrecio) {
        this.idProductoPrecio = idProductoPrecio;
    }
    
    public ProductoPrecio(Long idProductoPrecio, Long idProducto, Date fechaDesde, Date fechaHasta, BigDecimal precioSugerido) {
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
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProductoPrecio != null ? idProductoPrecio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ProductoPrecio)) {
            return false;
        }
        ProductoPrecio other = (ProductoPrecio) object;
        return !((this.idProductoPrecio == null && other.idProductoPrecio != null) || 
                (this.idProductoPrecio != null && !this.idProductoPrecio.equals(other.idProductoPrecio)));
    }

    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.ProductoPrecio[ idProductoPrecio=" + idProductoPrecio + " ]";
    }
}