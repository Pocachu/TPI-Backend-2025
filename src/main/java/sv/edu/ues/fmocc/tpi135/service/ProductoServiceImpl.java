package sv.edu.ues.fmocc.tpi135.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.Producto;
import sv.edu.ues.fmocc.tpi135.repository.ProductoRepository;

/**
 * Implementación de ProductoService que gestiona la lógica de negocio para productos
 */
@ApplicationScoped
public class ProductoServiceImpl implements ProductoService {
    
    @Inject
    private ProductoRepository productoRepository;
    
    /**
     * Convierte una entidad Producto a DTO
     */
    private ProductoDTO mapToDTO(Producto producto) {
        if (producto == null) {
            return null;
        }
        
        return new ProductoDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getActivo(),
                producto.getObservaciones()
        );
    }
    
    /**
     * Convierte un DTO a entidad Producto
     */
    private Producto mapToEntity(ProductoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Producto producto = new Producto();
        producto.setIdProducto(dto.getIdProducto());
        producto.setNombre(dto.getNombre());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        producto.setObservaciones(dto.getObservaciones());
        
        return producto;
    }
    
    @Override
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        // Validación básica
        if (productoDTO == null || productoDTO.getNombre() == null || productoDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        
        // Aseguramos que se crea como activo por defecto
        if (productoDTO.getActivo() == null) {
            productoDTO.setActivo(Boolean.TRUE);
        }
        
        // Convertimos DTO a entidad
        Producto producto = mapToEntity(productoDTO);
        
        // Guardamos en la base de datos
        Producto creado = productoRepository.crear(producto);
        
        // Convertimos resultado a DTO
        return mapToDTO(creado);
    }
    
    @Override
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) throws IllegalArgumentException {
        // Validación básica
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto es obligatorio");
        }
        
        if (productoDTO == null || productoDTO.getNombre() == null || productoDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        
        // Verificar si el producto existe
        Optional<Producto> existente = productoRepository.encontrarPorId(id);
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("No existe un producto con el ID " + id);
        }
        
        // Actualizar los campos
        Producto producto = existente.get();
        producto.setNombre(productoDTO.getNombre());
        producto.setActivo(productoDTO.getActivo() != null ? productoDTO.getActivo() : producto.getActivo());
        producto.setObservaciones(productoDTO.getObservaciones());
        
        // Guardar cambios
        Producto actualizado = productoRepository.actualizar(producto);
        
        // Convertir resultado a DTO
        return mapToDTO(actualizado);
    }
    
    @Override
    public Optional<ProductoDTO> obtenerProductoPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return productoRepository.encontrarPorId(id)
                .map(this::mapToDTO);
    }
    
    @Override
    public List<ProductoDTO> listarProductos() {
        return productoRepository.listarTodos().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductoDTO> buscarProductosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return List.of();
        }
        
        return productoRepository.buscarPorNombre(nombre).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductoDTO> buscarProductosPorEstado(Boolean activo) {
        if (activo == null) {
            return listarProductos();
        }
        
        return productoRepository.buscarPorEstado(activo).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean eliminarProducto(Long id) {
        if (id == null) {
            return false;
        }
        
        return productoRepository.eliminar(id);
    }
}