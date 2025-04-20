package test.java.sv.edu.ues.fmocc.tpi135.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.controller.ComboController;
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;
import sv.edu.ues.fmocc.tpi135.entity.Combo;
import sv.edu.ues.fmocc.tpi135.repository.ComboRepository;
import sv.edu.ues.fmocc.tpi135.repository.ComboRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.ComboService;
import sv.edu.ues.fmocc.tpi135.service.ComboServiceImpl;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas de integración para validar la interacción entre controladores,
 * servicios y repositorios en el flujo de operaciones CRUD de combos.
 */
@ExtendWith(MockitoExtension.class)
public class ComboIntegrationIT {

    @Mock
    private ComboRepositoryImpl comboRepository;

    private ComboService comboService;
    private ComboController comboController;

    private Combo comboEntity;
    private ComboDTO comboDTO;

    @BeforeEach
    void setUp() {
        // Configurar capa de servicio con el repositorio mock
        comboService = new ComboServiceImpl();
        // Usamos reflexión para inyectar el repositorio mock en el servicio
        try {
            java.lang.reflect.Field field = ComboServiceImpl.class.getDeclaredField("comboRepository");
            field.setAccessible(true);
            field.set(comboService, comboRepository);
        } catch (Exception e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Configurar controlador con el servicio
        comboController = new ComboController();
        try {
            java.lang.reflect.Field field = ComboController.class.getDeclaredField("comboService");
            field.setAccessible(true);
            field.set(comboController, comboService);
        } catch (Exception e) {
            fail("Error al inyectar servicio: " + e.getMessage());
        }

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
    void testFlujoCrearObtenerCombo() {
        // Configuración de mocks
        when(comboRepository.crear(any(Combo.class))).thenAnswer(invocation -> {
            Combo c = invocation.getArgument(0);
            c.setIdCombo(1L); // Simulamos generación de ID
            return c;
        });

        when(comboRepository.encontrarPorId(1L)).thenReturn(Optional.of(comboEntity));

        // Ejecutar flujo: crear combo
        ComboDTO inputDTO = new ComboDTO();
        inputDTO.setNombre("Super Combo");
        inputDTO.setActivo(true);
        inputDTO.setDescripcionPublica("Descripción del combo");

        Response createResponse = comboController.crearCombo(inputDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());

        ComboDTO createdDTO = (ComboDTO) createResponse.getEntity();
        assertNotNull(createdDTO.getIdCombo());
        assertEquals("Super Combo", createdDTO.getNombre());

        // Ejecutar flujo: obtener combo creado
        Response getResponse = comboController.obtenerComboPorId(1L);
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

        ComboDTO retrievedDTO = (ComboDTO) getResponse.getEntity();
        assertEquals(1L, retrievedDTO.getIdCombo());
        assertEquals("Super Combo", retrievedDTO.getNombre());

        // Verificar interacciones entre capas
        verify(comboRepository).crear(any(Combo.class));
        verify(comboRepository).encontrarPorId(1L);
    }

    @Test
    void testFlujoActualizarCombo() {
        // Configuración de mocks
        when(comboRepository.encontrarPorId(1L)).thenReturn(Optional.of(comboEntity));
        when(comboRepository.actualizar(any(Combo.class))).thenAnswer(invocation -> {
            Combo c = invocation.getArgument(0);
            return c; // Devolvemos el mismo objeto actualizado
        });

        // Ejecutar flujo: actualizar combo
        ComboDTO updateDTO = new ComboDTO();
        updateDTO.setNombre("Super Combo Actualizado");
        updateDTO.setActivo(false);
        updateDTO.setDescripcionPublica("Nueva descripción");

        Response updateResponse = comboController.actualizarCombo(1L, updateDTO);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        ComboDTO updatedDTO = (ComboDTO) updateResponse.getEntity();
        assertEquals(1L, updatedDTO.getIdCombo());
        assertEquals("Super Combo Actualizado", updatedDTO.getNombre());
        assertFalse(updatedDTO.getActivo());

        // Verificar interacciones entre capas
        verify(comboRepository).encontrarPorId(1L);
        verify(comboRepository).actualizar(any(Combo.class));
    }

    @Test
    void testFlujoBuscarCombosPorCriterios() {
        // Datos de prueba
        Combo combo1 = new Combo(1L, "Super Combo", true, "Obs 1");
        Combo combo2 = new Combo(2L, "Combo Familiar", true, "Obs 2");
        List<Combo> combos = Arrays.asList(combo1, combo2);
        List<Combo> combosActivos = Arrays.asList(combo1, combo2);
        List<Combo> combosFiltrados = List.of(combo1);

        // Configuración de mocks
        when(comboRepository.listarTodos()).thenReturn(combos);
        when(comboRepository.buscarPorEstado(true)).thenReturn(combosActivos);
        when(comboRepository.buscarPorNombre("Super")).thenReturn(combosFiltrados);

        // Ejecutar flujo: listar todos
        Response listResponse = comboController.listarCombos(null, null);
        assertEquals(Response.Status.OK.getStatusCode(), listResponse.getStatus());
        List<ComboDTO> listedDTOs = (List<ComboDTO>) listResponse.getEntity();
        assertEquals(2, listedDTOs.size());

        // Ejecutar flujo: buscar por estado
        Response activeResponse = comboController.listarCombos(null, true);
        assertEquals(Response.Status.OK.getStatusCode(), activeResponse.getStatus());
        List<ComboDTO> activeDTOs = (List<ComboDTO>) activeResponse.getEntity();
        assertEquals(2, activeDTOs.size());

        // Ejecutar flujo: buscar por nombre
        Response searchResponse = comboController.listarCombos("Super", null);
        assertEquals(Response.Status.OK.getStatusCode(), searchResponse.getStatus());
        List<ComboDTO> searchDTOs = (List<ComboDTO>) searchResponse.getEntity();
        assertEquals(1, searchDTOs.size());
        assertEquals("Super Combo", searchDTOs.get(0).getNombre());

        // Verificar interacciones entre capas
        verify(comboRepository).listarTodos();
        verify(comboRepository).buscarPorEstado(true);
        verify(comboRepository).buscarPorNombre("Super");
    }

    @Test
    void testFlujoEliminarCombo() {
        // Configuración de mocks
        when(comboRepository.eliminar(1L)).thenReturn(true);
        when(comboRepository.eliminar(999L)).thenReturn(false);

        // Ejecutar flujo: eliminar combo existente
        Response deleteResponse = comboController.eliminarCombo(1L);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // Ejecutar flujo: intentar eliminar combo inexistente
        Response deleteNotFoundResponse = comboController.eliminarCombo(999L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), deleteNotFoundResponse.getStatus());
        assertEquals("Combo no encontrado con ID: 999", deleteNotFoundResponse.getEntity());

        // Verificar interacciones entre capas
        verify(comboRepository).eliminar(1L);
        verify(comboRepository).eliminar(999L);
    }
}