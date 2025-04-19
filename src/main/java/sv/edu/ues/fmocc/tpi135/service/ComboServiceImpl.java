package sv.edu.ues.fmocc.tpi135.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;
import sv.edu.ues.fmocc.tpi135.entity.Combo;
import sv.edu.ues.fmocc.tpi135.repository.ComboRepository;

/**
 * Implementación de ComboService que gestiona la lógica de negocio para combos
 */
@ApplicationScoped
public class ComboServiceImpl implements ComboService {
    
    @Inject
    private ComboRepository comboRepository;
    
    /**
     * Convierte una entidad Combo a DTO
     */
    private ComboDTO mapToDTO(Combo combo) {
        if (combo == null) {
            return null;
        }
        
        return new ComboDTO(
                combo.getIdCombo(),
                combo.getNombre(),
                combo.getActivo(),
                combo.getDescripcionPublica()
        );
    }
    
    /**
     * Convierte un DTO a entidad Combo
     */
    private Combo mapToEntity(ComboDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Combo combo = new Combo();
        combo.setIdCombo(dto.getIdCombo());
        combo.setNombre(dto.getNombre());
        combo.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        combo.setDescripcionPublica(dto.getDescripcionPublica());
        
        return combo;
    }
    
    @Override
    public ComboDTO crearCombo(ComboDTO comboDTO) {
        // Validación básica
        if (comboDTO == null || comboDTO.getNombre() == null || comboDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del combo es obligatorio");
        }
        
        // Aseguramos que se crea como activo por defecto
        if (comboDTO.getActivo() == null) {
            comboDTO.setActivo(Boolean.TRUE);
        }
        
        // Convertimos DTO a entidad
        Combo combo = mapToEntity(comboDTO);
        
        // Guardamos en la base de datos
        Combo creado = comboRepository.crear(combo);
        
        // Convertimos resultado a DTO
        return mapToDTO(creado);
    }
    
    @Override
    public ComboDTO actualizarCombo(Long id, ComboDTO comboDTO) throws IllegalArgumentException {
        // Validación básica
        if (id == null) {
            throw new IllegalArgumentException("El ID del combo es obligatorio");
        }
        
        if (comboDTO == null || comboDTO.getNombre() == null || comboDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del combo es obligatorio");
        }
        
        // Verificar si el combo existe
        Optional<Combo> existente = comboRepository.encontrarPorId(id);
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("No existe un combo con el ID " + id);
        }
        
        // Actualizar los campos
        Combo combo = existente.get();
        combo.setNombre(comboDTO.getNombre());
        combo.setActivo(comboDTO.getActivo() != null ? comboDTO.getActivo() : combo.getActivo());
        combo.setDescripcionPublica(comboDTO.getDescripcionPublica());
        
        // Guardar cambios
        Combo actualizado = comboRepository.actualizar(combo);
        
        // Convertir resultado a DTO
        return mapToDTO(actualizado);
    }
    
    @Override
    public Optional<ComboDTO> obtenerComboPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return comboRepository.encontrarPorId(id)
                .map(this::mapToDTO);
    }
    
    @Override
    public List<ComboDTO> listarCombos() {
        return comboRepository.listarTodos().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ComboDTO> buscarCombosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return List.of();
        }
        
        return comboRepository.buscarPorNombre(nombre).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ComboDTO> buscarCombosPorEstado(Boolean activo) {
        if (activo == null) {
            return listarCombos();
        }
        
        return comboRepository.buscarPorEstado(activo).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean eliminarCombo(Long id) {
        if (id == null) {
            return false;
        }
        
        return comboRepository.eliminar(id);
    }
}