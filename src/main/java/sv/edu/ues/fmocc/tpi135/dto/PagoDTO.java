package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * DTO para transferir datos de pagos entre capas
 */
public class PagoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idPago;
    private Long idOrden;
    private Date fecha;
    private String metodoPago;
    private String referencia;
    private OrdenDTO orden;
    private List<PagoDetalleDTO> detalles;
    
    public PagoDTO() {
    }
    
    public PagoDTO(Long idPago, Long idOrden, Date fecha, String metodoPago, String referencia) {
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
    
    public OrdenDTO getOrden() {
        return orden;
    }
    
    public void setOrden(OrdenDTO orden) {
        this.orden = orden;
    }
    
    public List<PagoDetalleDTO> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<PagoDetalleDTO> detalles) {
        this.detalles = detalles;
    }
    
    @Override
    public String toString() {
        return "PagoDTO{" +
                "idPago=" + idPago +
                ", idOrden=" + idOrden +
                ", fecha=" + fecha +
                ", metodoPago='" + metodoPago + '\'' +
                ", referencia='" + referencia + '\'' +
                '}';
    }
}