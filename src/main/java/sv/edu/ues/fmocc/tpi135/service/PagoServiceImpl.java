package sv.edu.ues.fmocc.tpi135.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDetalleDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.Pago;
import sv.edu.ues.fmocc.tpi135.entity.PagoDetalle;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoRepository;

/**
 * Implementación de PagoService que gestiona la lógica de negocio para pagos
 */
@ApplicationScoped
public class PagoServiceImpl implements PagoService {
    
    @Inject
    private PagoRepository pagoRepository;
    
    @Inject
    private PagoDetalleRepository pagoDetalleRepository;
    
    @Inject
    private OrdenRepository ordenRepository;
    
    /**
     * Convierte una entidad Pago a DTO
     */
    private PagoDTO mapToDTO(Pago pago) {
        if (pago == null) {
            return null;
        }
        
        PagoDTO dto = new PagoDTO(
                pago.getIdPago(),
                pago.getIdOrden(),
                pago.getFecha(),
                pago.getMetodoPago(),
                pago.getReferencia()
        );
        
        // Cargar la orden relacionada si está disponible
        if (pago.getOrden() != null) {
            OrdenDTO ordenDTO = mapToOrdenDTO(pago.getOrden());
            dto.setOrden(ordenDTO);
        } else if (pago.getIdOrden() != null) {
            // Intentar cargar la orden desde el repositorio
            ordenRepository.encontrarPorId(pago.getIdOrden())
                    .ifPresent(orden -> dto.setOrden(mapToOrdenDTO(orden)));
        }
        
        // Cargar los detalles del pago si es necesario
        List<PagoDetalle> detalles = pagoDetalleRepository.buscarPorIdPago(pago.getIdPago());
        
        if (detalles != null && !detalles.isEmpty()) {
            List<PagoDetalleDTO> detallesDTO = detalles.stream()
                    .map(this::mapToDetalleDTO)
                    .collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }
        
        return dto;
    }
    
    /**
     * Convierte una entidad Orden a DTO (versión simplificada)
     */
    private OrdenDTO mapToOrdenDTO(Orden orden) {
        if (orden == null) {
            return null;
        }
        
        OrdenDTO dto = new OrdenDTO();
        dto.setIdOrden(orden.getIdOrden());
        dto.setFecha(orden.getFecha());
        dto.setSucursal(orden.getSucursal());
        dto.setAnulada(orden.getAnulada());
        
        return dto;
    }
    
    /**
     * Convierte un DTO a entidad Pago
     */
    private Pago mapToEntity(PagoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Pago pago = new Pago();
        pago.setIdPago(dto.getIdPago());
        pago.setIdOrden(dto.getIdOrden());
        pago.setFecha(dto.getFecha() != null ? dto.getFecha() : new Date()); // Fecha actual por defecto
        pago.setMetodoPago(dto.getMetodoPago() != null ? dto.getMetodoPago() : "EFECTIVO"); // Efectivo por defecto
        pago.setReferencia(dto.getReferencia());
        
        return pago;
    }
    
    /**
     * Convierte una entidad PagoDetalle a DTO
     */
    private PagoDetalleDTO mapToDetalleDTO(PagoDetalle detalle) {
        if (detalle == null) {
            return null;
        }
        
        return new PagoDetalleDTO(
                detalle.getIdPagoDetalle(),
                detalle.getIdPago(),
                detalle.getMonto(),
                detalle.getObservaciones()
        );
    }
    
    /**
     * Convierte un DTO a entidad PagoDetalle
     */
    private PagoDetalle mapToDetalleEntity(PagoDetalleDTO dto, Long idPago) {
        if (dto == null) {
            return null;
        }
        
        PagoDetalle detalle = new PagoDetalle();
        detalle.setIdPagoDetalle(dto.getIdPagoDetalle());
        detalle.setIdPago(idPago);
        detalle.setMonto(dto.getMonto() != null ? dto.getMonto() : BigDecimal.ZERO);
        detalle.setObservaciones(dto.getObservaciones());
        
        return detalle;
    }
    
    @Override
    @Transactional
    public PagoDTO crearPago(PagoDTO pagoDTO) {
        // Validación básica
        if (pagoDTO == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo");
        }
        
        if (pagoDTO.getIdOrden() == null) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio");
        }
        
        // Verificar que la orden existe
        if (!ordenRepository.encontrarPorId(pagoDTO.getIdOrden()).isPresent()) {
            throw new IllegalArgumentException("La orden con ID " + pagoDTO.getIdOrden() + " no existe");
        }
        
        // Aseguramos valores por defecto
        if (pagoDTO.getFecha() == null) {
            pagoDTO.setFecha(new Date());
        }
        
        if (pagoDTO.getMetodoPago() == null || pagoDTO.getMetodoPago().trim().isEmpty()) {
            pagoDTO.setMetodoPago("EFECTIVO");
        }
        
        // Convertimos DTO a entidad
        Pago pago = mapToEntity(pagoDTO);
        
        // Guardamos el pago en la base de datos
        Pago pagoCreado = pagoRepository.crear(pago);
        
        // Guardamos los detalles del pago si existen
        if (pagoDTO.getDetalles() != null && !pagoDTO.getDetalles().isEmpty()) {
            for (PagoDetalleDTO detalleDTO : pagoDTO.getDetalles()) {
                PagoDetalle detalle = mapToDetalleEntity(detalleDTO, pagoCreado.getIdPago());
                pagoDetalleRepository.crear(detalle);
            }
        }
        
        // Convertimos resultado a DTO
        return mapToDTO(pagoCreado);
    }
    
    @Override
    @Transactional
    public PagoDTO actualizarPago(Long id, PagoDTO pagoDTO) throws IllegalArgumentException {
        // Validación básica
        if (id == null) {
            throw new IllegalArgumentException("El ID del pago es obligatorio");
        }
        
        if (pagoDTO == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo");
        }
        
        // Verificar si el pago existe
        Optional<Pago> existente = pagoRepository.encontrarPorId(id);
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("No existe un pago con el ID " + id);
        }
        
        // Actualizar los campos
        Pago pago = existente.get();
        
        // Solo actualizar la orden si se proporciona una nueva
        if (pagoDTO.getIdOrden() != null) {
            // Verificar que la nueva orden existe
            if (!ordenRepository.encontrarPorId(pagoDTO.getIdOrden()).isPresent()) {
                throw new IllegalArgumentException("La orden con ID " + pagoDTO.getIdOrden() + " no existe");
            }
            pago.setIdOrden(pagoDTO.getIdOrden());
        }
        
        // Actualizar la fecha si se proporciona
        if (pagoDTO.getFecha() != null) {
            pago.setFecha(pagoDTO.getFecha());
        }
        
        // Actualizar el método de pago si se proporciona
        if (pagoDTO.getMetodoPago() != null && !pagoDTO.getMetodoPago().trim().isEmpty()) {
            pago.setMetodoPago(pagoDTO.getMetodoPago());
        }
        
        // Actualizar la referencia si se proporciona
        if (pagoDTO.getReferencia() != null) {
            pago.setReferencia(pagoDTO.getReferencia());
        }
        
        // Guardar cambios en el pago
        Pago actualizado = pagoRepository.actualizar(pago);
        
        // Manejar los detalles del pago si se proporcionan
        if (pagoDTO.getDetalles() != null) {
            // Opción más sencilla: eliminar los detalles existentes y agregar los nuevos
            pagoDetalleRepository.eliminarPorIdPago(id);
            
            // Agregar los nuevos detalles
            for (PagoDetalleDTO detalleDTO : pagoDTO.getDetalles()) {
                PagoDetalle detalle = mapToDetalleEntity(detalleDTO, id);
                pagoDetalleRepository.crear(detalle);
            }
        }
        
        // Convertir resultado a DTO
        return mapToDTO(actualizado);
    }
    
    @Override
    public Optional<PagoDTO> obtenerPagoPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return pagoRepository.encontrarPorId(id)
                .map(this::mapToDTO);
    }
    
    @Override
    public List<PagoDTO> listarPagos() {
        return pagoRepository.listarTodos().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PagoDTO> buscarPagosPorIdOrden(Long idOrden) {
        if (idOrden == null) {
            return new ArrayList<>();
        }
        
        return pagoRepository.buscarPorIdOrden(idOrden).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PagoDTO> buscarPagosPorFecha(Date fecha) {
        if (fecha == null) {
            return new ArrayList<>();
        }
        
        return pagoRepository.buscarPorFecha(fecha).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PagoDTO> buscarPagosPorMetodoPago(String metodoPago) {
        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return pagoRepository.buscarPorMetodoPago(metodoPago).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean eliminarPago(Long id) {
        if (id == null) {
            return false;
        }
        
        // Primero eliminar los detalles del pago
        pagoDetalleRepository.eliminarPorIdPago(id);
        
        // Luego eliminar el pago
        return pagoRepository.eliminar(id);
    }
}