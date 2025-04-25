package sv.edu.ues.fmocc.tpi135.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDetalleDTO;
import sv.edu.ues.fmocc.tpi135.dto.ProductoPrecioDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetalle;
import sv.edu.ues.fmocc.tpi135.entity.ProductoPrecio;
import sv.edu.ues.fmocc.tpi135.repository.OrdenDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.ProductoPrecioRepository;

/**
 * Implementación de OrdenService que gestiona la lógica de negocio para órdenes
 */
@ApplicationScoped
public class OrdenServiceImpl implements OrdenService {
    
    @Inject
    private OrdenRepository ordenRepository;
    
    @Inject
    private OrdenDetalleRepository ordenDetalleRepository;
    
    @Inject
    private ProductoPrecioRepository productoPrecioRepository;
    
    /**
     * Convierte una entidad Orden a DTO
     */
    private OrdenDTO mapToDTO(Orden orden) {
        if (orden == null) {
            return null;
        }
        
        OrdenDTO dto = new OrdenDTO(
                orden.getIdOrden(),
                orden.getFecha(),
                orden.getSucursal(),
                orden.getAnulada()
        );
        
        // Cargar los detalles de la orden si es necesario
        List<OrdenDetalle> detalles = ordenDetalleRepository.buscarPorIdOrden(orden.getIdOrden());
        
        if (detalles != null && !detalles.isEmpty()) {
            List<OrdenDetalleDTO> detallesDTO = detalles.stream()
                    .map(this::mapToDetalleDTO)
                    .collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }
        
        return dto;
    }
    
    /**
     * Convierte un DTO a entidad Orden
     */
    private Orden mapToEntity(OrdenDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Orden orden = new Orden();
        orden.setIdOrden(dto.getIdOrden());
        orden.setFecha(dto.getFecha() != null ? dto.getFecha() : new Date()); // Fecha actual por defecto
        orden.setSucursal(dto.getSucursal());
        orden.setAnulada(dto.getAnulada() != null ? dto.getAnulada() : Boolean.FALSE); // No anulada por defecto
        
        return orden;
    }
    
    /**
     * Convierte una entidad OrdenDetalle a DTO
     */
    private OrdenDetalleDTO mapToDetalleDTO(OrdenDetalle detalle) {
        if (detalle == null) {
            return null;
        }
        
        OrdenDetalleDTO dto = new OrdenDetalleDTO(
                detalle.getIdOrden(),
                detalle.getIdProductoPrecio(),
                detalle.getCantidad(),
                detalle.getPrecio(),
                detalle.getObservaciones()
        );
        
        // Cargar el producto-precio relacionado si está disponible
        if (detalle.getProductoPrecio() != null) {
            ProductoPrecioDTO productoPrecioDTO = mapToProductoPrecioDTO(detalle.getProductoPrecio());
            dto.setProductoPrecio(productoPrecioDTO);
        } else {
            // Intentar cargar el producto-precio desde el repositorio
            Optional<ProductoPrecio> productoPrecioOpt = productoPrecioRepository.encontrarPorId(detalle.getIdProductoPrecio());
            productoPrecioOpt.ifPresent(pp -> dto.setProductoPrecio(mapToProductoPrecioDTO(pp)));
        }
        
        return dto;
    }
    
    /**
     * Convierte una entidad ProductoPrecio a DTO
     */
    private ProductoPrecioDTO mapToProductoPrecioDTO(ProductoPrecio productoPrecio) {
        if (productoPrecio == null) {
            return null;
        }
        
        return new ProductoPrecioDTO(
                productoPrecio.getIdProductoPrecio(),
                productoPrecio.getIdProducto(),
                productoPrecio.getFechaDesde(),
                productoPrecio.getFechaHasta(),
                productoPrecio.getPrecioSugerido()
        );
    }
    
    /**
     * Convierte un DTO a entidad OrdenDetalle
     */
    private OrdenDetalle mapToDetalleEntity(OrdenDetalleDTO dto, Long idOrden) {
        if (dto == null) {
            return null;
        }
        
        OrdenDetalle detalle = new OrdenDetalle();
        detalle.setIdOrden(idOrden);
        detalle.setIdProductoPrecio(dto.getIdProductoPrecio());
        detalle.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : 1); // Cantidad 1 por defecto
        
        // Si no se proporciona un precio, obtener el precio sugerido del producto
        if (dto.getPrecio() == null) {
            Optional<ProductoPrecio> productoPrecioOpt = productoPrecioRepository.encontrarPorId(dto.getIdProductoPrecio());
            if (productoPrecioOpt.isPresent()) {
                detalle.setPrecio(productoPrecioOpt.get().getPrecioSugerido());
            } else {
                detalle.setPrecio(BigDecimal.ZERO);
            }
        } else {
            detalle.setPrecio(dto.getPrecio());
        }
        
        detalle.setObservaciones(dto.getObservaciones());
        
        return detalle;
    }
    
    @Override
    @Transactional
    public OrdenDTO crearOrden(OrdenDTO ordenDTO) {
        // Validación básica
        if (ordenDTO == null) {
            throw new IllegalArgumentException("La orden no puede ser nula");
        }
        
        // Aseguramos valores por defecto
        if (ordenDTO.getFecha() == null) {
            ordenDTO.setFecha(new Date());
        }
        
        if (ordenDTO.getAnulada() == null) {
            ordenDTO.setAnulada(Boolean.FALSE);
        }
        
        // Convertimos DTO a entidad
        Orden orden = mapToEntity(ordenDTO);
        
        // Guardamos la orden en la base de datos
        Orden ordenCreada = ordenRepository.crear(orden);
        
        // Guardamos los detalles de la orden si existen
        if (ordenDTO.getDetalles() != null && !ordenDTO.getDetalles().isEmpty()) {
            for (OrdenDetalleDTO detalleDTO : ordenDTO.getDetalles()) {
                OrdenDetalle detalle = mapToDetalleEntity(detalleDTO, ordenCreada.getIdOrden());
                ordenDetalleRepository.crear(detalle);
            }
        }
        
        // Convertimos resultado a DTO
        return mapToDTO(ordenCreada);
    }
    
    @Override
    @Transactional
    public OrdenDTO actualizarOrden(Long id, OrdenDTO ordenDTO) throws IllegalArgumentException {
        // Validación básica
        if (id == null) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio");
        }
        
        if (ordenDTO == null) {
            throw new IllegalArgumentException("La orden no puede ser nula");
        }
        
        // Verificar si la orden existe
        Optional<Orden> existente = ordenRepository.encontrarPorId(id);
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("No existe una orden con el ID " + id);
        }
        
        // Actualizar los campos
        Orden orden = existente.get();
        
        // Solo actualizar la fecha si se proporciona una nueva
        if (ordenDTO.getFecha() != null) {
            orden.setFecha(ordenDTO.getFecha());
        }
        
        // Actualizar la sucursal si se proporciona
        if (ordenDTO.getSucursal() != null) {
            orden.setSucursal(ordenDTO.getSucursal());
        }
        
        // Actualizar el estado de anulación si se proporciona
        if (ordenDTO.getAnulada() != null) {
            orden.setAnulada(ordenDTO.getAnulada());
        }
        
        // Guardar cambios en la orden
        Orden actualizada = ordenRepository.actualizar(orden);
        
        // Manejar los detalles de la orden si se proporcionan
        if (ordenDTO.getDetalles() != null) {
            // Opción más sencilla: eliminar los detalles existentes y agregar los nuevos
            ordenDetalleRepository.eliminarPorIdOrden(id);
            
            // Agregar los nuevos detalles
            for (OrdenDetalleDTO detalleDTO : ordenDTO.getDetalles()) {
                OrdenDetalle detalle = mapToDetalleEntity(detalleDTO, id);
                ordenDetalleRepository.crear(detalle);
            }
        }
        
        // Convertir resultado a DTO
        return mapToDTO(actualizada);
    }
    
    @Override
    public Optional<OrdenDTO> obtenerOrdenPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return ordenRepository.encontrarPorId(id)
                .map(this::mapToDTO);
    }
    
    @Override
    public List<OrdenDTO> listarOrdenes() {
        return ordenRepository.listarTodas().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrdenDTO> buscarOrdenesPorFecha(Date fecha) {
        if (fecha == null) {
            return new ArrayList<>();
        }
        
        return ordenRepository.buscarPorFecha(fecha).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrdenDTO> buscarOrdenesPorSucursal(String sucursal) {
        if (sucursal == null || sucursal.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return ordenRepository.buscarPorSucursal(sucursal).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrdenDTO> buscarOrdenesPorAnulada(Boolean anulada) {
        if (anulada == null) {
            return listarOrdenes();
        }
        
        return ordenRepository.buscarPorAnulada(anulada).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean anularOrden(Long id) {
        if (id == null) {
            return false;
        }
        
        return ordenRepository.anular(id);
    }
    
    @Override
    @Transactional
    public boolean eliminarOrden(Long id) {
        if (id == null) {
            return false;
        }
        
        // Primero eliminar los detalles de la orden
        ordenDetalleRepository.eliminarPorIdOrden(id);
        
        // Luego eliminar la orden
        return ordenRepository.eliminar(id);
    }
}