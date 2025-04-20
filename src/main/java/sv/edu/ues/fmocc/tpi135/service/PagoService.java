package sv.edu.ues.fmocc.tpi135.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;

/**
 * Interfaz que define los servicios para la gestión de pagos
 */
public interface PagoService {
    
    /**
     * Crea un nuevo pago
     * @param pagoDTO DTO con los datos del pago a crear
     * @return DTO del pago creado con su ID generado
     */
    PagoDTO crearPago(PagoDTO pagoDTO);
    
    /**
     * Actualiza un pago existente
     * @param id ID del pago a actualizar
     * @param pagoDTO DTO con los datos actualizados
     * @return DTO del pago actualizado
     * @throws IllegalArgumentException si el pago no existe
     */
    PagoDTO actualizarPago(Long id, PagoDTO pagoDTO) throws IllegalArgumentException;
    
    /**
     * Busca un pago por su ID
     * @param id ID del pago a buscar
     * @return Optional con el DTO del pago o vacío si no existe
     */
    Optional<PagoDTO> obtenerPagoPorId(Long id);
    
    /**
     * Lista todos los pagos
     * @return Lista de DTOs de pagos
     */
    List<PagoDTO> listarPagos();
    
    /**
     * Busca pagos por ID de orden
     * @param idOrden ID de la orden a buscar
     * @return Lista de DTOs de pagos según la orden
     */
    List<PagoDTO> buscarPagosPorIdOrden(Long idOrden);
    
    /**
     * Busca pagos por fecha
     * @param fecha Fecha a buscar
     * @return Lista de DTOs de pagos que coinciden con la fecha
     */
    List<PagoDTO> buscarPagosPorFecha(Date fecha);
    
    /**
     * Busca pagos por método de pago
     * @param metodoPago Método de pago a buscar
     * @return Lista de DTOs de pagos según el método de pago
     */
    List<PagoDTO> buscarPagosPorMetodoPago(String metodoPago);
    
    /**
     * Elimina un pago por su ID
     * @param id ID del pago a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminarPago(Long id);
}