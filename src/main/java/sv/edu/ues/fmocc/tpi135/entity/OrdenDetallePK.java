package sv.edu.ues.fmocc.tpi135.entity;

import java.io.Serializable;

/**
 * Clase para representar la clave primaria compuesta de OrdenDetalle.
 */
public class OrdenDetallePK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idOrden;
    private Long idProductoPrecio;
    
    public OrdenDetallePK() {
    }
    
    public OrdenDetallePK(Long idOrden, Long idProductoPrecio) {
        this.idOrden = idOrden;
        this.idProductoPrecio = idProductoPrecio;
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
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrden != null ? idOrden.hashCode() : 0);
        hash += (idProductoPrecio != null ? idProductoPrecio.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof OrdenDetallePK)) {
            return false;
        }
        OrdenDetallePK other = (OrdenDetallePK) object;
        if ((this.idOrden == null && other.idOrden != null) || 
                (this.idOrden != null && !this.idOrden.equals(other.idOrden))) {
            return false;
        }
        return !((this.idProductoPrecio == null && other.idProductoPrecio != null) || 
                (this.idProductoPrecio != null && !this.idProductoPrecio.equals(other.idProductoPrecio)));
    }
    
    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.OrdenDetallePK[ idOrden=" + idOrden + ", idProductoPrecio=" + idProductoPrecio + " ]";
    }
}