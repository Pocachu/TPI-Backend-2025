package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * DTO para transferir datos de órdenes entre capas
 */
public class OrdenDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idOrden;
    private Date fecha;
    private String sucursal;
    private Boolean anulada;
    private List<OrdenDetalleDTO> detalles;
    
    public OrdenDTO() {
    }
    
    public OrdenDTO(Long idOrden, Date fecha, String sucursal, Boolean anulada) {
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
    
    public List<OrdenDetalleDTO> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<OrdenDetalleDTO> detalles) {
        this.detalles = detalles;
    }
    
    @Override
    public String toString() {
        return "OrdenDTO{" +
                "idOrden=" + idOrden +
                ", fecha=" + fecha +
                ", sucursal='" + sucursal + '\'' +
                ", anulada=" + anulada +
                '}';
    }
}