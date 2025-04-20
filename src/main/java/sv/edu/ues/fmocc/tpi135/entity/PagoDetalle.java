package sv.edu.ues.fmocc.tpi135.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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

/**
 * Entidad JPA que representa el detalle de un pago en el sistema.
 */
@Entity
@Table(name = "pago_detalle")
@NamedQueries({
    @NamedQuery(name = "PagoDetalle.findAll", query = "SELECT pd FROM PagoDetalle pd"),
    @NamedQuery(name = "PagoDetalle.findById", query = "SELECT pd FROM PagoDetalle pd WHERE pd.idPagoDetalle = :idPagoDetalle"),
    @NamedQuery(name = "PagoDetalle.findByIdPago", query = "SELECT pd FROM PagoDetalle pd WHERE pd.idPago = :idPago")
})
public class PagoDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago_detalle")
    private Long idPagoDetalle;
    
    @Column(name = "id_pago")
    private Long idPago;
    
    @Column(name = "monto", precision = 6, scale = 2)
    private BigDecimal monto;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    @ManyToOne
    @JoinColumn(name = "id_pago", insertable = false, updatable = false)
    private Pago pago;
    
    public PagoDetalle() {
    }
    
    public PagoDetalle(Long idPagoDetalle) {
        this.idPagoDetalle = idPagoDetalle;
    }
    
    public PagoDetalle(Long idPagoDetalle, Long idPago, BigDecimal monto, String observaciones) {
        this.idPagoDetalle = idPagoDetalle;
        this.idPago = idPago;
        this.monto = monto;
        this.observaciones = observaciones;
    }

    public Long getIdPagoDetalle() {
        return idPagoDetalle;
    }

    public void setIdPagoDetalle(Long idPagoDetalle) {
        this.idPagoDetalle = idPagoDetalle;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public Pago getPago() {
        return pago;
    }
    
    public void setPago(Pago pago) {
        this.pago = pago;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPagoDetalle != null ? idPagoDetalle.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PagoDetalle)) {
            return false;
        }
        PagoDetalle other = (PagoDetalle) object;
        return !((this.idPagoDetalle == null && other.idPagoDetalle != null) || 
                (this.idPagoDetalle != null && !this.idPagoDetalle.equals(other.idPagoDetalle)));
    }

    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.PagoDetalle[ idPagoDetalle=" + idPagoDetalle + " ]";
    }
}