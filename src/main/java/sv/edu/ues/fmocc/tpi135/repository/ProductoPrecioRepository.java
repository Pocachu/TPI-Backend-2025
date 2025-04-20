package sv.edu.ues.fmocc.tpi135.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.ProductoPrecio;

/**
 * Interfaz que define las operaciones de persistencia para la entidad ProductoPrecio
 */
public interface ProductoPrecioRepository {
    
    /**
     * Guarda un nuevo precio de producto en la base de datos
     * @param productoPrecio Entidad a guardar
     * @return ProductoPrecio guardado con su ID generado
     */
    ProductoPrecio crear(ProductoPrecio productoPrecio);
    
    /**
     * Actualiza un precio de producto existente
     * @param productoPrecio ProductoPrecio con los datos actualizados
     * @return ProductoPrecio actualizado
     */
    ProductoPrecio actualizar(ProductoPrecio productoPrecio);
    
    /**
     * Busca un precio de producto por su ID
     * @param id ID del precio de producto a buscar
     * @return Optional con el precio de producto encontrado o vacío si no existe
     */
    Optional<ProductoPrecio> encontrarPorId(Long id);
    
    /**
     * Lista todos los precios de productos
     * @return Lista de precios de productos
     */
    List<ProductoPrecio> listarTodos();
    
    /**
     * Busca precios de productos por ID de producto
     * @param idProducto ID del producto a buscar
     * @return Lista de precios de productos según el producto
     */
    List<ProductoPrecio> buscarPorIdProducto(Long idProducto);
    
    /**
     * Busca precios de productos vigentes en una fecha
     * @param fecha Fecha de vigencia a considerar
     * @return Lista de precios de productos vigentes en la fecha
     */
    List<ProductoPrecio> buscarVigentesPorFecha(Date fecha);
    
    /**
     * Busca el precio vigente para un producto en una fecha específica
     * @param idProducto ID del producto
     * @param fecha Fecha de vigencia a considerar
     * @return Optional con el precio vigente o vacío si no existe
     */
    Optional<ProductoPrecio> buscarPrecioVigente(Long idProducto, Date fecha);
    
    /**
     * Elimina un precio de producto por su ID
     * @param id ID del precio de producto a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Long id);
}