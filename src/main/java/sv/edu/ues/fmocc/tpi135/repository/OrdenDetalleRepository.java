package sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetalle;
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetallePK;

/**
 * Interfaz que define las operaciones de persistencia para la entidad OrdenDetalle
 */
public interface OrdenDetalleRepository {
    
    /**
     * Guarda un nuevo detalle de orden en la base de datos
     * @param ordenDetalle Entidad a guardar
     * @return OrdenDetalle guardado
     */
    OrdenDetalle crear(OrdenDetalle ordenDetalle);
    
    /**
     * Actualiza un detalle de orden existente
     * @param ordenDetalle OrdenDetalle con los datos actualizados
     * @return OrdenDetalle actualizado
     */
    OrdenDetalle actualizar(OrdenDetalle ordenDetalle);
    
    /**
     * Busca un detalle de orden por su clave primaria
     * @param pk Clave primaria compuesta del detalle de orden a buscar
     * @return Optional con el detalle de orden encontrado o vacío si no existe
     */
    Optional<OrdenDetalle> encontrarPorId(OrdenDetallePK pk);
    
    /**
     * Busca un detalle de orden por su orden y producto-precio
     * @param idOrden ID de la orden
     * @param idProductoPrecio ID del producto-precio
     * @return Optional con el detalle de orden encontrado o vacío si no existe
     */
    Optional<OrdenDetalle> encontrarPorId(Long idOrden, Long idProductoPrecio);
    
    /**
     * Lista todos los detalles de órdenes
     * @return Lista de detalles de órdenes
     */
    List<OrdenDetalle> listarTodos();
    
    /**
     * Busca detalles de órdenes por ID de orden
     * @param idOrden ID de la orden a buscar
     * @return Lista de detalles de órdenes que pertenecen a la orden
     */
    List<OrdenDetalle> buscarPorIdOrden(Long idOrden);
    
    /**
     * Busca detalles de órdenes por ID de producto-precio
     * @param idProductoPrecio ID del producto-precio a buscar
     * @return Lista de detalles de órdenes según el producto-precio
     */
    List<OrdenDetalle> buscarPorIdProductoPrecio(Long idProductoPrecio);
    
    /**
     * Elimina un detalle de orden por su clave primaria
     * @param pk Clave primaria compuesta del detalle de orden a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(OrdenDetallePK pk);
    
    /**
     * Elimina un detalle de orden por su orden y producto-precio
     * @param idOrden ID de la orden
     * @param idProductoPrecio ID del producto-precio
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Long idOrden, Long idProductoPrecio);
    
    /**
     * Elimina todos los detalles de una orden
     * @param idOrden ID de la orden
     * @return Cantidad de detalles eliminados
     */
    int eliminarPorIdOrden(Long idOrden);
}