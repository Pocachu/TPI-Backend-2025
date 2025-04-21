package sv.edu.ues.fmocc.tpi135.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.controller.TipoProductoController;
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.TipoProducto;
import sv.edu.ues.fmocc.tpi135.repository.TipoProductoRepository;
import sv.edu.ues.fmocc.tpi135.repository.TipoProductoRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.TipoProductoService;
import sv.edu.ues.fmocc.tpi135.service.TipoProductoServiceImpl;

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
 * servicios y repositorios en el flujo de operaciones CRUD de tipos de productos.
 */
@ExtendWith(MockitoExtension.class)
public class TipoProductoIntegrationIT {

    @Mock
    private TipoProductoRepositoryImpl tipoProductoRepository;

    private TipoProductoService tipoProductoService;
    private TipoProductoController tipoProductoController;

    private TipoProducto tipoProductoEntity;
    private TipoProductoDTO tipoProductoDTO;

    @BeforeEach
    void setUp() {
        // Configurar capa de servicio con el repositorio mock
        tipoProductoService = new TipoProductoServiceImpl();
        // Usamos reflexión para inyectar el repositorio mock en el servicio
        try {
            java.lang.reflect.Field field = TipoProductoServiceImpl.class.getDeclaredField("tipoProductoRepository");
            field.setAccessible(true);
            field.set(tipoProductoService, tipoProductoRepository);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Configurar controlador con el servicio
        tipoProductoController = new TipoProductoController();
        try {
            java.lang.reflect.Field field = TipoProductoController.class.getDeclaredField("tipoProductoService");
            field.setAccessible(true);
            field.set(tipoProductoController, tipoProductoService);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            fail("Error al inyectar servicio: " + e.getMessage());
        }

        // Datos de prueba para entidad
        tipoProductoEntity = new TipoProducto();
        tipoProductoEntity.setIdTipoProducto(1);
        tipoProductoEntity.setNombre("Bebida");
        tipoProductoEntity.setActivo(true);
        tipoProductoEntity.setObservaciones("Observaciones de prueba");

        // Datos de prueba para DTO
        tipoProductoDTO = new TipoProductoDTO();
        tipoProductoDTO.setIdTipoProducto(1);
        tipoProductoDTO.setNombre("Bebida");
        tipoProductoDTO.setActivo(true);
        tipoProductoDTO.setObservaciones("Observaciones de prueba");
    }

    @Test
    void testFlujoCrearObtenerTipoProducto() {
        // Configuración de mocks
        when(tipoProductoRepository.crear(any(TipoProducto.class))).thenAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            tp.setIdTipoProducto(1); // Simulamos generación de ID
            return tp;
        });

        when(tipoProductoRepository.encontrarPorId(1)).thenReturn(Optional.of(tipoProductoEntity));

        // Ejecutar flujo: crear tipo de producto
        TipoProductoDTO inputDTO = new TipoProductoDTO();
        inputDTO.setNombre("Bebida");
        inputDTO.setActivo(true);
        inputDTO.setObservaciones("Observaciones de prueba");

        Response createResponse = tipoProductoController.crearTipoProducto(inputDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());

        TipoProductoDTO createdDTO = (TipoProductoDTO) createResponse.getEntity();
        assertNotNull(createdDTO.getIdTipoProducto());
        assertEquals("Bebida", createdDTO.getNombre());

        // Ejecutar flujo: obtener tipo de producto creado
        Response getResponse = tipoProductoController.obtenerTipoProductoPorId(1);
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

        TipoProductoDTO retrievedDTO = (TipoProductoDTO) getResponse.getEntity();
        assertEquals(1, retrievedDTO.getIdTipoProducto());
        assertEquals("Bebida", retrievedDTO.getNombre());

        // Verificar interacciones entre capas
        verify(tipoProductoRepository).crear(any(TipoProducto.class));
        verify(tipoProductoRepository).encontrarPorId(1);
    }

    @Test
    void testFlujoActualizarTipoProducto() {
        // Configuración de mocks
        when(tipoProductoRepository.encontrarPorId(1)).thenReturn(Optional.of(tipoProductoEntity));
        when(tipoProductoRepository.actualizar(any(TipoProducto.class))).thenAnswer(invocation -> {
            TipoProducto tp = invocation.getArgument(0);
            return tp; // Devolvemos el mismo objeto actualizado
        });

        // Ejecutar flujo: actualizar tipo de producto
        TipoProductoDTO updateDTO = new TipoProductoDTO();
        updateDTO.setNombre("Bebida Actualizada");
        updateDTO.setActivo(false);
        updateDTO.setObservaciones("Nueva descripción");

        Response updateResponse = tipoProductoController.actualizarTipoProducto(1, updateDTO);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        TipoProductoDTO updatedDTO = (TipoProductoDTO) updateResponse.getEntity();
        assertEquals(1, updatedDTO.getIdTipoProducto());
        assertEquals("Bebida Actualizada", updatedDTO.getNombre());
        assertFalse(updatedDTO.getActivo());

        // Verificar interacciones entre capas
        verify(tipoProductoRepository).encontrarPorId(1);
        verify(tipoProductoRepository).actualizar(any(TipoProducto.class));
    }

    @Test
    void testFlujoBuscarTiposProductosPorCriterios() {
        // Datos de prueba
        TipoProducto tipoProducto1 = new TipoProducto(1, "Bebida", true, "Obs 1");
        TipoProducto tipoProducto2 = new TipoProducto(2, "Comida", true, "Obs 2");
        List<TipoProducto> tiposProductos = Arrays.asList(tipoProducto1, tipoProducto2);
        List<TipoProducto> tiposProductosActivos = Arrays.asList(tipoProducto1, tipoProducto2);
        List<TipoProducto> tiposProductosFiltrados = List.of(tipoProducto1);

        // Configuración de mocks
        when(tipoProductoRepository.listarTodos()).thenReturn(tiposProductos);
        when(tipoProductoRepository.buscarPorEstado(true)).thenReturn(tiposProductosActivos);
        when(tipoProductoRepository.buscarPorNombre("Bebida")).thenReturn(tiposProductosFiltrados);

        // Ejecutar flujo: listar todos
        Response listResponse = tipoProductoController.listarTiposProductos(null, null);
        assertEquals(Response.Status.OK.getStatusCode(), listResponse.getStatus());
        List<TipoProductoDTO> listedDTOs = (List<TipoProductoDTO>) listResponse.getEntity();
        assertEquals(2, listedDTOs.size());

        // Ejecutar flujo: buscar por estado
        Response activeResponse = tipoProductoController.listarTiposProductos(null, true);
        assertEquals(Response.Status.OK.getStatusCode(), activeResponse.getStatus());
        List<TipoProductoDTO> activeDTOs = (List<TipoProductoDTO>) activeResponse.getEntity();
        assertEquals(2, activeDTOs.size());

        // Ejecutar flujo: buscar por nombre
        Response searchResponse = tipoProductoController.listarTiposProductos("Bebida", null);
        assertEquals(Response.Status.OK.getStatusCode(), searchResponse.getStatus());
        List<TipoProductoDTO> searchDTOs = (List<TipoProductoDTO>) searchResponse.getEntity();
        assertEquals(1, searchDTOs.size());
        assertEquals("Bebida", searchDTOs.get(0).getNombre());

        // Verificar interacciones entre capas
        verify(tipoProductoRepository).listarTodos();
        verify(tipoProductoRepository).buscarPorEstado(true);
        verify(tipoProductoRepository).buscarPorNombre("Bebida");
    }

    @Test
    void testFlujoEliminarTipoProducto() {
        // Configuración de mocks
        when(tipoProductoRepository.eliminar(1)).thenReturn(true);
        when(tipoProductoRepository.eliminar(999)).thenReturn(false);

        // Ejecutar flujo: eliminar tipo de producto existente
        Response deleteResponse = tipoProductoController.eliminarTipoProducto(1);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // Ejecutar flujo: intentar eliminar tipo de producto inexistente
        Response deleteNotFoundResponse = tipoProductoController.eliminarTipoProducto(999);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), deleteNotFoundResponse.getStatus());
        assertEquals("Tipo de producto no encontrado con ID: 999", deleteNotFoundResponse.getEntity());

        // Verificar interacciones entre capas
        verify(tipoProductoRepository).eliminar(1);
        verify(tipoProductoRepository).eliminar(999);
    }
}