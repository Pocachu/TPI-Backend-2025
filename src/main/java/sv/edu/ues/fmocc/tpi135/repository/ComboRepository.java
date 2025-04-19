package sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.Combo;

/**
 * Interfaz que define las operaciones de persistencia para la entidad Combo
 */
public interface ComboRepository {
    
    /**
     * Guarda un nuevo combo en la base de datos
     * @param combo Entidad a guardar
     * @return Combo guardado con su ID generado
     */
    Combo crear(Combo combo);
    
    /**
     * Actualiza un combo existente
     * @param combo Combo con los datos actualizados
     * @return Combo actualizado
     */
    Combo actualizar(Combo combo);
    
    /**
     * Busca un combo por su ID
     * @param id ID del combo a buscar
     * @return Optional con el combo encontrado o vacío si no existe
     */
    Optional<Combo> encontrarPorId(Long id);
    
    /**
     * Lista todos los combos
     * @return Lista de combos
     */
    List<Combo> listarTodos();
    
    /**
     * Busca combos por su nombre
     * @param nombre Nombre a buscar
     * @return Lista de combos que coinciden con el nombre
     */
    List<Combo> buscarPorNombre(String nombre);
    
    /**
     * Busca combos por su estado activo/inactivo
     * @param activo Estado a buscar
     * @return Lista de combos según su estado
     */
    List<Combo> buscarPorEstado(Boolean activo);
    
    /**
     * Elimina un combo por su ID
     * @param id ID del combo a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Long id);
}