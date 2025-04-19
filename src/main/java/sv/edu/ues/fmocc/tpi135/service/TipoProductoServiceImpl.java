package sv.edu.ues.fmocc.tpi135.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.TipoProducto;
import sv.edu.ues.fmocc.tpi135.repository.TipoProductoRepository;

/**
 * Implementación de TipoProductoService que gestiona la lógica de negocio para tipos de productos
 */
@ApplicationScoped
public class TipoProductoServiceImpl implements TipoProductoService {
    
    @Inject
    private TipoProductoRepository tipoProductoRepository;
    
    /**
     * Convierte una entidad TipoProducto a DTO
     */
    private TipoProductoDTO mapToDTO(TipoProducto tipoProducto) {
        if (tipoProducto == null) {
            return null;
        }
        
        return new TipoProductoDTO(
                tipoProducto.getIdTipoProducto(),
                tipoProducto.getNombre(),
                tipoProducto.getActivo(),
                tipoProducto.getObservaciones()
        );
    }
    
    /**
     * Convierte un DTO a entidad TipoProducto
     */
    private TipoProducto mapToEntity(TipoProductoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        TipoProducto tipoProducto = new TipoProducto();
        tipoProducto.setIdTipoProducto(dto.getIdTipoProducto());
        tipoProducto.setNombre(dto.getNombre());
        tipoProducto.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        tipoProducto.setObservaciones(dto.getObservaciones());
        
        return tipoProducto;
    }
    
    @Override
    public TipoProductoDTO crearTipoProducto(TipoProductoDTO tipoProductoDTO) {
        // Validación básica
        if (tipoProductoDTO == null || tipoProductoDTO.getNombre() == null || tipoProductoDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo de producto es obligatorio");
        }
        
        // Aseguramos que se crea como activo por defecto
        if (tipoProductoDTO.getActivo() == null) {
            tipoProductoDTO.setActivo(Boolean.TRUE);
        }
        
        // Convertimos DTO a entidad
        TipoProducto tipoProducto = mapToEntity(tipoProductoDTO);
        
        // Guardamos en la base de datos
        TipoProducto creado = tipoProductoRepository.crear(tipoProducto);
        
        // Convertimos resultado a DTO
        return mapToDTO(creado);
    }
    
    @Override
    public TipoProductoDTO actualizarTipoProducto(Integer id, TipoProductoDTO tipoProductoDTO) throws IllegalArgumentException {
        // Validación básica
        if (id == null) {
            throw new IllegalArgumentException("El ID del tipo de producto es obligatorio");
        }
        
        if (tipoProductoDTO == null || tipoProductoDTO.getNombre() == null || tipoProductoDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo de producto es obligatorio");
        }
        
        // Verificar si el tipo de producto existe
        Optional<TipoProducto> existente = tipoProductoRepository.encontrarPorId(id);
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("No existe un tipo de producto con el ID " + id);
        }
        
        // Actualizar los campos
        TipoProducto tipoProducto = existente.get();
        tipoProducto.setNombre(tipoProductoDTO.getNombre());
        tipoProducto.setActivo(tipoProductoDTO.getActivo() != null ? tipoProductoDTO.getActivo() : tipoProducto.getActivo());
        tipoProducto.setObservaciones(tipoProductoDTO.getObservaciones());
        
        // Guardar cambios
        TipoProducto actualizado = tipoProductoRepository.actualizar(tipoProducto);
        
        // Convertir resultado a DTO
        return mapToDTO(actualizado);
    }
    
    @Override
    public Optional<TipoProductoDTO> obtenerTipoProductoPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return tipoProductoRepository.encontrarPorId(id)
                .map(this::mapToDTO);
    }
    
    @Override
    public List<TipoProductoDTO> listarTiposProductos() {
        return tipoProductoRepository.listarTodos().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TipoProductoDTO> buscarTiposProductosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return List.of();
        }
        
        return tipoProductoRepository.buscarPorNombre(nombre).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TipoProductoDTO> buscarTiposProductosPorEstado(Boolean activo) {
        if (activo == null) {
            return listarTiposProductos();
        }
        
        return tipoProductoRepository.buscarPorEstado(activo).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean eliminarTipoProducto(Integer id) {
        if (id == null) {
            return false;
        }
        
        return tipoProductoRepository.eliminar(id);
    }
}