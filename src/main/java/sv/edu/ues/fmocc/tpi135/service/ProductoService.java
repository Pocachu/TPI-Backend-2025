package sv.edu.ues.fmocc.tpi135.service;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;

/**
 * Interfaz que define los servicios para la gestión de productos
 */
public interface ProductoService {
    
    /**
     * Crea un nuevo producto
     * @param productoDTO DTO con los datos del producto a crear
     * @return DTO del producto creado con su ID generado
     */
    ProductoDTO crearProducto(ProductoDTO productoDTO);
    
    /**
     * Actualiza un producto existente
     * @param id ID del producto a actualizar
     * @param productoDTO DTO con los datos actualizados
     * @return DTO del producto actualizado
     * @throws IllegalArgumentException si el producto no existe
     */
    ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) throws IllegalArgumentException;
    
    /**
     * Busca un producto por su ID
     * @param id ID del producto a buscar
     * @return Optional con el DTO del producto o vacío si no existe
     */
    Optional<ProductoDTO> obtenerProductoPorId(Long id);
    
    /**
     * Lista todos los productos
     * @return Lista de DTOs de productos
     */
    List<ProductoDTO> listarProductos();
    
    /**
     * Busca productos por su nombre
     * @param nombre Nombre a buscar
     * @return Lista de DTOs de productos que coinciden con el nombre
     */
    List<ProductoDTO> buscarProductosPorNombre(String nombre);
    
    /**
     * Busca productos por su estado activo/inactivo
     * @param activo Estado a buscar
     * @return Lista de DTOs de productos según su estado
     */
    List<ProductoDTO> buscarProductosPorEstado(Boolean activo);
    
    /**
     * Elimina un producto por su ID
     * @param id ID del producto a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminarProducto(Long id);
}