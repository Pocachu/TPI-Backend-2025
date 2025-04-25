package sv.edu.ues.fmocc.tpi135.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;
import sv.edu.ues.fmocc.tpi135.service.ComboService;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComboControllerTest {

    @Mock
    private ComboService comboService;

    @InjectMocks
    private sv.edu.ues.fmocc.tpi135.controller.ComboController comboController;

    private ComboDTO comboDTO;

    @BeforeEach
    void setUp() {
        // Datos de prueba
        comboDTO = new ComboDTO();
        comboDTO.setIdCombo(1L);
        comboDTO.setNombre("Super Combo");
        comboDTO.setActivo(true);
        comboDTO.setDescripcionPublica("Descripción del combo");
    }

    @Test
    void testCrearCombo_Exitoso() {
        // Configuración del mock
        when(comboService.crearCombo(any(ComboDTO.class))).thenReturn(comboDTO);

        // Ejecución del método
        Response response = comboController.crearCombo(comboDTO);

        // Verificaciones
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(comboDTO, response.getEntity());

        verify(comboService, times(1)).crearCombo(any(ComboDTO.class));
    }

    @Test
    void testCrearCombo_ErrorValidacion() {
        // Configuración del mock
        when(comboService.crearCombo(any(ComboDTO.class)))
                .thenThrow(new IllegalArgumentException("El nombre del combo es obligatorio"));

        // Ejecución del método
        Response response = comboController.crearCombo(comboDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("El nombre del combo es obligatorio", response.getEntity());

        verify(comboService, times(1)).crearCombo(any(ComboDTO.class));
    }

    @Test
    void testActualizarCombo_Exitoso() {
        // Configuración del mock
        when(comboService.actualizarCombo(eq(1L), any(ComboDTO.class))).thenReturn(comboDTO);

        // Ejecución del método
        Response response = comboController.actualizarCombo(1L, comboDTO);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(comboDTO, response.getEntity());

        verify(comboService, times(1)).actualizarCombo(eq(1L), any(ComboDTO.class));
    }

    @Test
    void testActualizarCombo_NoExiste() {
        // Configuración del mock
        when(comboService.actualizarCombo(eq(999L), any(ComboDTO.class)))
                .thenThrow(new IllegalArgumentException("No existe un combo con el ID 999"));

        // Ejecución del método
        Response response = comboController.actualizarCombo(999L, comboDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("No existe un combo con el ID 999", response.getEntity());

        verify(comboService, times(1)).actualizarCombo(eq(999L), any(ComboDTO.class));
    }

    @Test
    void testObtenerComboPorId_Existente() {
        // Configuración del mock
        when(comboService.obtenerComboPorId(1L)).thenReturn(Optional.of(comboDTO));

        // Ejecución del método
        Response response = comboController.obtenerComboPorId(1L);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(comboDTO, response.getEntity());

        verify(comboService, times(1)).obtenerComboPorId(1L);
    }

    @Test
    void testObtenerComboPorId_NoExistente() {
        // Configuración del mock
        when(comboService.obtenerComboPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Response response = comboController.obtenerComboPorId(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Combo no encontrado con ID: 999", response.getEntity());

        verify(comboService, times(1)).obtenerComboPorId(999L);
    }

    @Test
    void testListarCombos_TodosLosCombos() {
        // Datos de prueba
        ComboDTO combo2 = new ComboDTO(2L, "Combo Familiar", true, "Obs 2");
        List<ComboDTO> combos = Arrays.asList(comboDTO, combo2);

        // Configuración del mock
        when(comboService.listarCombos()).thenReturn(combos);

        // Ejecución del método
        Response response = comboController.listarCombos(null, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<ComboDTO> resultado = (List<ComboDTO>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(comboService, times(1)).listarCombos();
        verify(comboService, never()).buscarCombosPorNombre(any());
        verify(comboService, never()).buscarCombosPorEstado(any());
    }

    @Test
    void testListarCombos_FiltrarPorNombre() {
        // Datos de prueba
        List<ComboDTO> combos = List.of(comboDTO);

        // Configuración del mock
        when(comboService.buscarCombosPorNombre("Super")).thenReturn(combos);

        // Ejecución del método
        Response response = comboController.listarCombos("Super", null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<ComboDTO> resultado = (List<ComboDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("Super Combo", resultado.get(0).getNombre());

        verify(comboService, times(1)).buscarCombosPorNombre("Super");
        verify(comboService, never()).listarCombos();
        verify(comboService, never()).buscarCombosPorEstado(any());
    }

    @Test
    void testListarCombos_FiltrarPorEstado() {
        // Datos de prueba
        List<ComboDTO> combos = List.of(comboDTO);

        // Configuración del mock
        when(comboService.buscarCombosPorEstado(true)).thenReturn(combos);

        // Ejecución del método
        Response response = comboController.listarCombos(null, true);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<ComboDTO> resultado = (List<ComboDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(comboService, times(1)).buscarCombosPorEstado(true);
        verify(comboService, never()).listarCombos();
        verify(comboService, never()).buscarCombosPorNombre(any());
    }

    @Test
    void testEliminarCombo_Exitoso() {
        // Configuración del mock
        when(comboService.eliminarCombo(1L)).thenReturn(true);

        // Ejecución del método
        Response response = comboController.eliminarCombo(1L);

        // Verificaciones
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());

        verify(comboService, times(1)).eliminarCombo(1L);
    }

    @Test
    void testEliminarCombo_NoExiste() {
        // Configuración del mock
        when(comboService.eliminarCombo(999L)).thenReturn(false);

        // Ejecución del método
        Response response = comboController.eliminarCombo(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Combo no encontrado con ID: 999", response.getEntity());

        verify(comboService, times(1)).eliminarCombo(999L);
    }
}