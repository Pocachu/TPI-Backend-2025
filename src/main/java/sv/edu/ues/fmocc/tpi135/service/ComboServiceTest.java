package sv.edu.ues.fmocc.tpi135.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;
import sv.edu.ues.fmocc.tpi135.entity.Combo;
import sv.edu.ues.fmocc.tpi135.repository.ComboRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComboServiceTest {

    @Mock
    private ComboRepository comboRepository;

    @InjectMocks
    private ComboServiceImpl comboService;

    private Combo comboEntity;
    private ComboDTO comboDTO;

    @BeforeEach
    void setUp() {
        // Datos de prueba para entidad
        comboEntity = new Combo();
        comboEntity.setIdCombo(1L);
        comboEntity.setNombre("Super Combo");
        comboEntity.setActivo(true);
        comboEntity.setDescripcionPublica("Descripción del combo");

        // Datos de prueba para DTO
        comboDTO = new ComboDTO();
        comboDTO.setIdCombo(1L);
        comboDTO.setNombre("Super Combo");
        comboDTO.setActivo(true);
        comboDTO.setDescripcionPublica("Descripción del combo");
    }

    @Test
    void testCrearCombo_Exitoso() {
        // Configuración del mock
        when(comboRepository.crear(any(Combo.class))).thenReturn(comboEntity);

        // Ejecución del método
        ComboDTO resultado = comboService.crearCombo(comboDTO);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(comboDTO.getIdCombo(), resultado.getIdCombo());
        assertEquals(comboDTO.getNombre(), resultado.getNombre());
        assertEquals(comboDTO.getActivo(), resultado.getActivo());
        assertEquals(comboDTO.getDescripcionPublica(), resultado.getDescripcionPublica());

        verify(comboRepository, times(1)).crear(any(Combo.class));
    }

    @Test
    void testCrearCombo_NombreVacio() {
        // Datos de prueba
        ComboDTO dtoInvalido = new ComboDTO();
        dtoInvalido.setNombre("");

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> comboService.crearCombo(dtoInvalido));

        verify(comboRepository, never()).crear(any());
    }

    @Test
    void testActualizarCombo_Exitoso() {
        // Configuración del mock
        when(comboRepository.encontrarPorId(1L)).thenReturn(Optional.of(comboEntity));
        when(comboRepository.actualizar(any(Combo.class))).thenReturn(comboEntity);

        // Datos de prueba
        ComboDTO dtoActualizado = new ComboDTO();
        dtoActualizado.setNombre("Super Combo Actualizado");
        dtoActualizado.setActivo(false);
        dtoActualizado.setDescripcionPublica("Nueva descripción");

        // Ejecución del método
        ComboDTO resultado = comboService.actualizarCombo(1L, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdCombo());
        assertEquals("Super Combo Actualizado", resultado.getNombre());
        assertEquals(false, resultado.getActivo());
        assertEquals("Nueva descripción", resultado.getDescripcionPublica());

        verify(comboRepository, times(1)).encontrarPorId(1L);
        verify(comboRepository, times(1)).actualizar(any(Combo.class));
    }
    
    @Test
    void testActualizarCombo_NoExiste() {
        // Configuración del mock
        when(comboRepository.encontrarPorId(1L)).thenReturn(Optional.empty());

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> comboService.actualizarCombo(1L, comboDTO));

        verify(comboRepository, times(1)).encontrarPorId(1L);
        verify(comboRepository, never()).actualizar(any());
    }

    @Test
    void testObtenerComboPorId_Existente() {
        // Configuración del mock
        when(comboRepository.encontrarPorId(1L)).thenReturn(Optional.of(comboEntity));

        // Ejecución del método
        Optional<ComboDTO> resultado = comboService.obtenerComboPorId(1L);

        // Verificaciones
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdCombo());
        assertEquals("Super Combo", resultado.get().getNombre());

        verify(comboRepository, times(1)).encontrarPorId(1L);
    }

    @Test
    void testObtenerComboPorId_NoExistente() {
        // Configuración del mock
        when(comboRepository.encontrarPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Optional<ComboDTO> resultado = comboService.obtenerComboPorId(999L);

        // Verificaciones
        assertFalse(resultado.isPresent());

        verify(comboRepository, times(1)).encontrarPorId(999L);
    }
    
    @Test
    void testListarCombos() {
        // Datos de prueba
        Combo combo2 = new Combo(2L, "Combo Familiar", true, "Obs 2");
        List<Combo> combos = Arrays.asList(comboEntity, combo2);

        // Configuración del mock
        when(comboRepository.listarTodos()).thenReturn(combos);

        // Ejecución del método
        List<ComboDTO> resultado = comboService.listarCombos();

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdCombo());
        assertEquals(2L, resultado.get(1).getIdCombo());

        verify(comboRepository, times(1)).listarTodos();
    }

    @Test
    void testBuscarCombosPorNombre() {
        // Datos de prueba
        List<Combo> combos = List.of(comboEntity);

        // Configuración del mock
        when(comboRepository.buscarPorNombre("Super")).thenReturn(combos);

        // Ejecución del método
        List<ComboDTO> resultado = comboService.buscarCombosPorNombre("Super");

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Super Combo", resultado.get(0).getNombre());

        verify(comboRepository, times(1)).buscarPorNombre("Super");
    }

    @Test
    void testBuscarCombosPorEstado() {
        // Datos de prueba
        List<Combo> combosActivos = List.of(comboEntity);

        // Configuración del mock
        when(comboRepository.buscarPorEstado(true)).thenReturn(combosActivos);

        // Ejecución del método
        List<ComboDTO> resultado = comboService.buscarCombosPorEstado(true);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(comboRepository, times(1)).buscarPorEstado(true);
    }
    
    @Test
    void testEliminarCombo_Exitoso() {
        // Configuración del mock
        when(comboRepository.eliminar(1L)).thenReturn(true);

        // Ejecución del método
        boolean resultado = comboService.eliminarCombo(1L);

        // Verificaciones
        assertTrue(resultado);

        verify(comboRepository, times(1)).eliminar(1L);
    }

    @Test
    void testEliminarCombo_NoExiste() {
        // Configuración del mock
        when(comboRepository.eliminar(999L)).thenReturn(false);

        // Ejecución del método
        boolean resultado = comboService.eliminarCombo(999L);

        // Verificaciones
        assertFalse(resultado);

        verify(comboRepository, times(1)).eliminar(999L);
    }
}