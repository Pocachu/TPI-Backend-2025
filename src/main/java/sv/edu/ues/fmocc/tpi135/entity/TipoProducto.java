package sv.edu.ues.fmocc.tpi135.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entidad JPA que representa un tipo de producto en el sistema.
 */
@Entity
@Table(name = "tipo_producto")
@NamedQueries({
    @NamedQuery(name = "TipoProducto.findAll", query = "SELECT tp FROM TipoProducto tp"),
    @NamedQuery(name = "TipoProducto.findById", query = "SELECT tp FROM TipoProducto tp WHERE tp.idTipoProducto = :idTipoProducto"),
    @NamedQuery(name = "TipoProducto.findByNombre", query = "SELECT tp FROM TipoProducto tp WHERE tp.nombre = :nombre"),
    @NamedQuery(name = "TipoProducto.findByActivo", query = "SELECT tp FROM TipoProducto tp WHERE tp.activo = :activo")
})
public class TipoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_producto")
    private Integer idTipoProducto;
    
    @Column(name = "nombre", length = 155, nullable = false)
    private String nombre;
    
    @Column(name = "activo")
    private Boolean activo;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    public TipoProducto() {
    }
    
    public TipoProducto(Integer idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }
    
    public TipoProducto(Integer idTipoProducto, String nombre, Boolean activo, String observaciones) {
        this.idTipoProducto = idTipoProducto;
        this.nombre = nombre;
        this.activo = activo;
        this.observaciones = observaciones;
    }

    public Integer getIdTipoProducto() {
        return idTipoProducto;
    }

    public void setIdTipoProducto(Integer idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoProducto != null ? idTipoProducto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TipoProducto)) {
            return false;
        }
        TipoProducto other = (TipoProducto) object;
        return !((this.idTipoProducto == null && other.idTipoProducto != null) || 
                (this.idTipoProducto != null && !this.idTipoProducto.equals(other.idTipoProducto)));
    }

    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.TipoProducto[ idTipoProducto=" + idTipoProducto + " ]";
    }
}