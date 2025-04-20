package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO para transferir datos de detalles de pagos entre capas
 */
public class PagoDetalleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idPagoDetalle;
    private Long idPago;
    private BigDecimal monto;
    private String observaciones;
    
    public PagoDetalleDTO() {
    }
    
    public PagoDetalleDTO(Long idPagoDetalle, Long idPago, BigDecimal monto, String observaciones) {
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
    
    @Override
    public String toString() {
        return "PagoDetalleDTO{" +
                "idPagoDetalle=" + idPagoDetalle +
                ", idPago=" + idPago +
                ", monto=" + monto +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}