package sv.edu.ues.fmocc.tpi135.service;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;

/**
 * Interfaz que define los servicios para la gestión de combos
 */
public interface ComboService {
    
    /**
     * Crea un nuevo combo
     * @param comboDTO DTO con los datos del combo a crear
     * @return DTO del combo creado con su ID generado
     */
    ComboDTO crearCombo(ComboDTO comboDTO);
    
    /**
     * Actualiza un combo existente
     * @param id ID del combo a actualizar
     * @param comboDTO DTO con los datos actualizados
     * @return DTO del combo actualizado
     * @throws IllegalArgumentException si el combo no existe
     */
    ComboDTO actualizarCombo(Long id, ComboDTO comboDTO) throws IllegalArgumentException;
    
    /**
     * Busca un combo por su ID
     * @param id ID del combo a buscar
     * @return Optional con el DTO del combo o vacío si no existe
     */
    Optional<ComboDTO> obtenerComboPorId(Long id);
    
    /**
     * Lista todos los combos
     * @return Lista de DTOs de combos
     */
    List<ComboDTO> listarCombos();
    
    /**
     * Busca combos por su nombre
     * @param nombre Nombre a buscar
     * @return Lista de DTOs de combos que coinciden con el nombre
     */
    List<ComboDTO> buscarCombosPorNombre(String nombre);
    
    /**
     * Busca combos por su estado activo/inactivo
     * @param activo Estado a buscar
     * @return Lista de DTOs de combos según su estado
     */
    List<ComboDTO> buscarCombosPorEstado(Boolean activo);
    
    /**
     * Elimina un combo por su ID
     * @param id ID del combo a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminarCombo(Long id);
}