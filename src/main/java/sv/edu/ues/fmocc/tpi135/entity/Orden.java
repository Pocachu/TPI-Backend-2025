package sv.edu.ues.fmocc.tpi135.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entidad JPA que representa una orden en el sistema.
 */
@Entity
@Table(name = "orden")
@NamedQueries({
    @NamedQuery(name = "Orden.findAll", query = "SELECT o FROM Orden o"),
    @NamedQuery(name = "Orden.findById", query = "SELECT o FROM Orden o WHERE o.idOrden = :idOrden"),
    @NamedQuery(name = "Orden.findByFecha", query = "SELECT o FROM Orden o WHERE o.fecha = :fecha"),
    @NamedQuery(name = "Orden.findBySucursal", query = "SELECT o FROM Orden o WHERE o.sucursal = :sucursal"),
    @NamedQuery(name = "Orden.findByAnulada", query = "SELECT o FROM Orden o WHERE o.anulada = :anulada")
})
public class Orden implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;
    
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    @Column(name = "sucursal", length = 5)
    private String sucursal;
    
    @Column(name = "anulada")
    private Boolean anulada;
    
    public Orden() {
    }
    
    public Orden(Long idOrden) {
        this.idOrden = idOrden;
    }
    
    public Orden(Long idOrden, Date fecha, String sucursal, Boolean anulada) {
        this.idOrden = idOrden;
        this.fecha = fecha;
        this.sucursal = sucursal;
        this.anulada = anulada;
    }

    public Long getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Long idOrden) {
        this.idOrden = idOrden;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrden != null ? idOrden.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Orden)) {
            return false;
        }
        Orden other = (Orden) object;
        return !((this.idOrden == null && other.idOrden != null) || 
                (this.idOrden != null && !this.idOrden.equals(other.idOrden)));
    }

    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.Orden[ idOrden=" + idOrden + " ]";
    }
}