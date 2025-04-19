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
 * Entidad JPA que representa un combo en el sistema.
 */
@Entity
@Table(name = "combo")
@NamedQueries({
    @NamedQuery(name = "Combo.findAll", query = "SELECT c FROM Combo c"),
    @NamedQuery(name = "Combo.findById", query = "SELECT c FROM Combo c WHERE c.idCombo = :idCombo"),
    @NamedQuery(name = "Combo.findByNombre", query = "SELECT c FROM Combo c WHERE c.nombre = :nombre"),
    @NamedQuery(name = "Combo.findByActivo", query = "SELECT c FROM Combo c WHERE c.activo = :activo")
})
public class Combo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_combo")
    private Long idCombo;
    
    @Column(name = "nombre", length = 155)
    private String nombre;
    
    @Column(name = "activo")
    private Boolean activo;
    
    @Column(name = "descripcion_publica")
    private String descripcionPublica;
    
    public Combo() {
    }
    
    public Combo(Long idCombo) {
        this.idCombo = idCombo;
    }
    
    public Combo(Long idCombo, String nombre, Boolean activo, String descripcionPublica) {
        this.idCombo = idCombo;
        this.nombre = nombre;
        this.activo = activo;
        this.descripcionPublica = descripcionPublica;
    }

    public Long getIdCombo() {
        return idCombo;
    }

    public void setIdCombo(Long idCombo) {
        this.idCombo = idCombo;
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

    public String getDescripcionPublica() {
        return descripcionPublica;
    }

    public void setDescripcionPublica(String descripcionPublica) {
        this.descripcionPublica = descripcionPublica;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idCombo != null ? idCombo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Combo)) {
            return false;
        }
        Combo other = (Combo) object;
        return !((this.idCombo == null && other.idCombo != null) || 
                (this.idCombo != null && !this.idCombo.equals(other.idCombo)));
    }

    @Override
    public String toString() {
        return "sv.edu.ues.fmocc.tpi135.entity.Combo[ idCombo=" + idCombo + " ]";
    }
}