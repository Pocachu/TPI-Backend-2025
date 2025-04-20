package sv.edu.ues.fmocc.tpi135.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.Pago;

/**
 * Interfaz que define las operaciones de persistencia para la entidad Pago
 */
public interface PagoRepository {
    
    /**
     * Guarda un nuevo pago en la base de datos
     * @param pago Entidad a guardar
     * @return Pago guardado con su ID generado
     */
    Pago crear(Pago pago);
    
    /**
     * Actualiza un pago existente
     * @param pago Pago con los datos actualizados
     * @return Pago actualizado
     */
    Pago actualizar(Pago pago);
    
    /**
     * Busca un pago por su ID
     * @param id ID del pago a buscar
     * @return Optional con el pago encontrado o vacío si no existe
     */
    Optional<Pago> encontrarPorId(Long id);
    
    /**
     * Lista todos los pagos
     * @return Lista de pagos
     */
    List<Pago> listarTodos();
    
    /**
     * Busca pagos por ID de orden
     * @param idOrden ID de la orden a buscar
     * @return Lista de pagos según la orden
     */
    List<Pago> buscarPorIdOrden(Long idOrden);
    
    /**
     * Busca pagos por fecha
     * @param fecha Fecha a buscar
     * @return Lista de pagos que coinciden con la fecha
     */
    List<Pago> buscarPorFecha(Date fecha);
    
    /**
     * Busca pagos por método de pago
     * @param metodoPago Método de pago a buscar
     * @return Lista de pagos según el método de pago
     */
    List<Pago> buscarPorMetodoPago(String metodoPago);
    
    /**
     * Elimina un pago por su ID
     * @param id ID del pago a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Long id);
}