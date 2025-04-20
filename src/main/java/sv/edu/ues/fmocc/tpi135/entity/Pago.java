package sv.edu.ues.fmocc.tpi135.entity;

import java.io.Serializable;
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
 * Entidad JPA que representa un pago en el sistema.
 */
@Entity
@Table(name = "pago")
@NamedQueries({
    @NamedQuery(name = "Pago.findAll", query = "SELECT p FROM Pago p"),
    @NamedQuery(name = "Pago.findById", query = "SELECT p FROM Pago p WHERE p.idPago = :idPago"),
    @NamedQuery(name = "Pago.findByIdOrden", query = "SELECT p FROM Pago p WHERE p.idOrden = :idOrden"),
    @NamedQuery(name = "Pago.findByFecha", query = "SELECT p FROM Pago p WHERE p.fecha = :fecha"),
    @NamedQuery(name = "Pago.findByMetodoPago", query = "SELECT p FROM Pago p WHERE p.metodoPago = :metodoPago")
})
public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;
    
    @Column(name = "id_orden")
    private Long idOrden;
    
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    @Column(name = "metodo_pago", length = 10)
    private String metodoPago;
    
    @Column(name = "referencia")
    private String referencia;
    
    @ManyToOne
    @JoinColumn(name = "id_orden", insertable = false, updatable = false)
    private Orden orden;
    
    public Pago() {
    }
    
    public Pago(Long idPago) {
        this.idPago = idPago;
    }
    
    public Pago(Long idPago, Long idOrden, Date fecha, String metodoPago, String referencia) {
        this.idPago = idPago;
        this.idOrden = idOrden;
        this.fecha = fecha;
        this.metodoPago = metodoPago;
        this.referencia = referencia;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
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

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
    public Orden getOrden() {
        return orden;
    }
    
    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPago != null ? idPago.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Pago)) {
            return false;
        }
        Pago other = (Pago) object;
        return !((this.idPago == null && other.idPago != null) || 
                (this.idPago != null && !this.idPago.equals(other.idPago)));
    }

    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.Pago[ idPago=" + idPago + " ]";
    }
}