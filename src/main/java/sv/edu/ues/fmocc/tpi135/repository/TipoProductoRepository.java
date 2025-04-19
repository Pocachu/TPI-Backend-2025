package sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.TipoProducto;

/**
 * Interfaz que define las operaciones de persistencia para la entidad TipoProducto
 */
public interface TipoProductoRepository {
    
    /**
     * Guarda un nuevo tipo de producto en la base de datos
     * @param tipoProducto Entidad a guardar
     * @return TipoProducto guardado con su ID generado
     */
    TipoProducto crear(TipoProducto tipoProducto);
    
    /**
     * Actualiza un tipo de producto existente
     * @param tipoProducto TipoProducto con los datos actualizados
     * @return TipoProducto actualizado
     */
    TipoProducto actualizar(TipoProducto tipoProducto);
    
    /**
     * Busca un tipo de producto por su ID
     * @param id ID del tipo de producto a buscar
     * @return Optional con el tipo de producto encontrado o vacío si no existe
     */
    Optional<TipoProducto> encontrarPorId(Integer id);
    
    /**
     * Lista todos los tipos de productos
     * @return Lista de tipos de productos
     */
    List<TipoProducto> listarTodos();
    
    /**
     * Busca tipos de productos por su nombre
     * @param nombre Nombre a buscar
     * @return Lista de tipos de productos que coinciden con el nombre
     */
    List<TipoProducto> buscarPorNombre(String nombre);
    
    /**
     * Busca tipos de productos por su estado activo/inactivo
     * @param activo Estado a buscar
     * @return Lista de tipos de productos según su estado
     */
    List<TipoProducto> buscarPorEstado(Boolean activo);
    
    /**
     * Elimina un tipo de producto por su ID
     * @param id ID del tipo de producto a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Integer id);
}