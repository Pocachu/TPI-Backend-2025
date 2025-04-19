package sv.edu.ues.fmocc.tpi135.service;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;

/**
 * Interfaz que define los servicios para la gestión de tipos de productos
 */
public interface TipoProductoService {
    
    /**
     * Crea un nuevo tipo de producto
     * @param tipoProductoDTO DTO con los datos del tipo de producto a crear
     * @return DTO del tipo de producto creado con su ID generado
     */
    TipoProductoDTO crearTipoProducto(TipoProductoDTO tipoProductoDTO);
    
    /**
     * Actualiza un tipo de producto existente
     * @param id ID del tipo de producto a actualizar
     * @param tipoProductoDTO DTO con los datos actualizados
     * @return DTO del tipo de producto actualizado
     * @throws IllegalArgumentException si el tipo de producto no existe
     */
    TipoProductoDTO actualizarTipoProducto(Integer id, TipoProductoDTO tipoProductoDTO) throws IllegalArgumentException;
    
    /**
     * Busca un tipo de producto por su ID
     * @param id ID del tipo de producto a buscar
     * @return Optional con el DTO del tipo de producto o vacío si no existe
     */
    Optional<TipoProductoDTO> obtenerTipoProductoPorId(Integer id);
    
    /**
     * Lista todos los tipos de productos
     * @return Lista de DTOs de tipos de productos
     */
    List<TipoProductoDTO> listarTiposProductos();
    
    /**
     * Busca tipos de productos por su nombre
     * @param nombre Nombre a buscar
     * @return Lista de DTOs de tipos de productos que coinciden con el nombre
     */
    List<TipoProductoDTO> buscarTiposProductosPorNombre(String nombre);
    
    /**
     * Busca tipos de productos por su estado activo/inactivo
     * @param activo Estado a buscar
     * @return Lista de DTOs de tipos de productos según su estado
     */
    List<TipoProductoDTO> buscarTiposProductosPorEstado(Boolean activo);
    
    /**
     * Elimina un tipo de producto por su ID
     * @param id ID del tipo de producto a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminarTipoProducto(Integer id);
}