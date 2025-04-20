package sv.edu.ues.fmocc.tpi135.repository;

import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.entity.PagoDetalle;

/**
 * Interfaz que define las operaciones de persistencia para la entidad PagoDetalle
 */
public interface PagoDetalleRepository {
    
    /**
     * Guarda un nuevo detalle de pago en la base de datos
     * @param pagoDetalle Entidad a guardar
     * @return PagoDetalle guardado con su ID generado
     */
    PagoDetalle crear(PagoDetalle pagoDetalle);
    
    /**
     * Actualiza un detalle de pago existente
     * @param pagoDetalle PagoDetalle con los datos actualizados
     * @return PagoDetalle actualizado
     */
    PagoDetalle actualizar(PagoDetalle pagoDetalle);
    
    /**
     * Busca un detalle de pago por su ID
     * @param id ID del detalle de pago a buscar
     * @return Optional con el detalle de pago encontrado o vacío si no existe
     */
    Optional<PagoDetalle> encontrarPorId(Long id);
    
    /**
     * Lista todos los detalles de pagos
     * @return Lista de detalles de pagos
     */
    List<PagoDetalle> listarTodos();
    
    /**
     * Busca detalles de pagos por ID de pago
     * @param idPago ID del pago a buscar
     * @return Lista de detalles de pagos según el pago
     */
    List<PagoDetalle> buscarPorIdPago(Long idPago);
    
    /**
     * Elimina un detalle de pago por su ID
     * @param id ID del detalle de pago a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminar(Long id);
    
    /**
     * Elimina todos los detalles de un pago
     * @param idPago ID del pago
     * @return Cantidad de detalles eliminados
     */
    int eliminarPorIdPago(Long idPago);
}