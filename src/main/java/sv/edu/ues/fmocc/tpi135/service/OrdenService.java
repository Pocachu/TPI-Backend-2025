package sv.edu.ues.fmocc.tpi135.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;

/**
 * Interfaz que define los servicios para la gestión de órdenes
 */
public interface OrdenService {
    
    /**
     * Crea una nueva orden
     * @param ordenDTO DTO con los datos de la orden a crear
     * @return DTO de la orden creada con su ID generado
     */
    OrdenDTO crearOrden(OrdenDTO ordenDTO);
    
    /**
     * Actualiza una orden existente
     * @param id ID de la orden a actualizar
     * @param ordenDTO DTO con los datos actualizados
     * @return DTO de la orden actualizada
     * @throws IllegalArgumentException si la orden no existe
     */
    OrdenDTO actualizarOrden(Long id, OrdenDTO ordenDTO) throws IllegalArgumentException;
    
    /**
     * Busca una orden por su ID
     * @param id ID de la orden a buscar
     * @return Optional con el DTO de la orden o vacío si no existe
     */
    Optional<OrdenDTO> obtenerOrdenPorId(Long id);
    
    /**
     * Lista todas las órdenes
     * @return Lista de DTOs de órdenes
     */
    List<OrdenDTO> listarOrdenes();
    
    /**
     * Busca órdenes por fecha
     * @param fecha Fecha a buscar
     * @return Lista de DTOs de órdenes que coinciden con la fecha
     */
    List<OrdenDTO> buscarOrdenesPorFecha(Date fecha);
    
    /**
     * Busca órdenes por sucursal
     * @param sucursal Sucursal a buscar
     * @return Lista de DTOs de órdenes según la sucursal
     */
    List<OrdenDTO> buscarOrdenesPorSucursal(String sucursal);
    
    /**
     * Busca órdenes por estado de anulación
     * @param anulada Estado de anulación a buscar
     * @return Lista de DTOs de órdenes según su estado de anulación
     */
    List<OrdenDTO> buscarOrdenesPorAnulada(Boolean anulada);
    
    /**
     * Anula una orden por su ID
     * @param id ID de la orden a anular
     * @return true si se anuló correctamente, false si no se encontró
     */
    boolean anularOrden(Long id);
    
    /**
     * Elimina una orden por su ID
     * @param id ID de la orden a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    boolean eliminarOrden(Long id);
}