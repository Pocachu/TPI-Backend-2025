package sv.edu.ues.fmocc.tpi135.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.Orden;

/**
 * Interfaz que define las operaciones de persistencia para la entidad Orden
 */
public interface OrdenRepository {
    
    /**
     * Guarda una nueva orden en la base de datos
     * @param orden Entidad a guardar
     * @return Orden guardada con su ID generado
     */
    Orden crear(Orden orden);
    
    /**
     * Actualiza una orden existente
     * @param orden Orden con los datos actualizados
     * @return Orden actualizada
     */
    Orden actualizar(Orden orden);
    
    /**
     * Busca una orden por su ID
     * @param id ID de la orden a buscar
     * @return Optional con la orden encontrada o vacío si no existe
     */
    Optional<Orden> encontrarPorId(Long id);
    
    /**
     * Lista todas las órdenes
     * @return Lista de órdenes
     */
    List<Orden> listarTodas();
    
    /**
     * Busca órdenes por fecha
     * @param fecha Fecha a buscar
     * @return Lista de órdenes que coinciden con la fecha
     */
    List<Orden> buscarPorFecha(Date fecha);
    
    /**
     * Busca órdenes por sucursal
     * @param sucursal Sucursal a buscar
     * @return Lista de órdenes según la sucursal
     */
    List<Orden> buscarPorSucursal(String sucursal);
    
    /**
     * Busca órdenes por estado de anulación
     * @param anulada Estado de anulación a buscar
     * @return Lista de órdenes según su estado de anulación
     */
    List<Orden> buscarPorAnulada(Boolean anulada);
    
    /**
     * Anula una orden por su ID
     * @param id ID de la orden a anular
     * @return true si se anuló correctamente, false si no se encontró
     */
    boolean anular(Long id);
    
    /**
     * Elimina una orden por su ID
     * @param id ID de la orden a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Long id);
}