package sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.Producto;

/**
 * Interfaz que define las operaciones de persistencia para la entidad Producto
 */
public interface ProductoRepository {
    
    /**
     * Guarda un nuevo producto en la base de datos
     * @param producto Entidad a guardar
     * @return Producto guardado con su ID generado
     */
    Producto crear(Producto producto);
    
    /**
     * Actualiza un producto existente
     * @param producto Producto con los datos actualizados
     * @return Producto actualizado
     */
    Producto actualizar(Producto producto);
    
    /**
     * Busca un producto por su ID
     * @param id ID del producto a buscar
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> encontrarPorId(Long id);
    
    /**
     * Lista todos los productos
     * @return Lista de productos
     */
    List<Producto> listarTodos();
    
    /**
     * Busca productos por su nombre
     * @param nombre Nombre a buscar
     * @return Lista de productos que coinciden con el nombre
     */
    List<Producto> buscarPorNombre(String nombre);
    
    /**
     * Busca productos por su estado activo/inactivo
     * @param activo Estado a buscar
     * @return Lista de productos según su estado
     */
    List<Producto> buscarPorEstado(Boolean activo);
    
    /**
     * Elimina un producto por su ID
     * @param id ID del producto a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Long id);
}