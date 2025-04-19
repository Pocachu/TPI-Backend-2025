package sv.edu.ues.fmocc.tpi135.dto;

import java.io.Serializable;

/**
 * DTO para transferir datos de combos entre capas
 */
public class ComboDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idCombo;
    private String nombre;
    private Boolean activo;
    private String descripcionPublica;
    
    public ComboDTO() {
    }
    
    public ComboDTO(Long idCombo, String nombre, Boolean activo, String descripcionPublica) {
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
    public String toString() {
        return "ComboDTO{" +
                "idCombo=" + idCombo +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                ", descripcionPublica='" + descripcionPublica + '\'' +
                '}';
    }
}