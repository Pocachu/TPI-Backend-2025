package sv.edu.ues.fmocc.tpi135.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;
import sv.edu.ues.fmocc.tpi135.service.TipoProductoService;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoProductoControllerTest {

    @Mock
    private TipoProductoService tipoProductoService;

    @InjectMocks
    private TipoProductoController tipoProductoController;

    private TipoProductoDTO tipoProductoDTO;

    @BeforeEach
    void setUp() {
        // Datos de prueba
        tipoProductoDTO = new TipoProductoDTO();
        tipoProductoDTO.setIdTipoProducto(1);
        tipoProductoDTO.setNombre("Bebida");
        tipoProductoDTO.setActivo(true);
        tipoProductoDTO.setObservaciones("Observaciones de prueba");
    }

    @Test
    void testCrearTipoProducto_Exitoso() {
        // Configuración del mock
        when(tipoProductoService.crearTipoProducto(any(TipoProductoDTO.class))).thenReturn(tipoProductoDTO);

        // Ejecución del método
        Response response = tipoProductoController.crearTipoProducto(tipoProductoDTO);

        // Verificaciones
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(tipoProductoDTO, response.getEntity());

        verify(tipoProductoService, times(1)).crearTipoProducto(any(TipoProductoDTO.class));
    }

    @Test
    void testCrearTipoProducto_ErrorValidacion() {
        // Configuración del mock
        when(tipoProductoService.crearTipoProducto(any(TipoProductoDTO.class)))
                .thenThrow(new IllegalArgumentException("El nombre del tipo de producto es obligatorio"));

        // Ejecución del método
        Response response = tipoProductoController.crearTipoProducto(tipoProductoDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("El nombre del tipo de producto es obligatorio", response.getEntity());

        verify(tipoProductoService, times(1)).crearTipoProducto(any(TipoProductoDTO.class));
    }

    @Test
    void testActualizarTipoProducto_Exitoso() {
        // Configuración del mock
        when(tipoProductoService.actualizarTipoProducto(eq(1), any(TipoProductoDTO.class))).thenReturn(tipoProductoDTO);

        // Ejecución del método
        Response response = tipoProductoController.actualizarTipoProducto(1, tipoProductoDTO);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(tipoProductoDTO, response.getEntity());

        verify(tipoProductoService, times(1)).actualizarTipoProducto(eq(1), any(TipoProductoDTO.class));
    }

    @Test
    void testActualizarTipoProducto_NoExiste() {
        // Configuración del mock
        when(tipoProductoService.actualizarTipoProducto(eq(999), any(TipoProductoDTO.class)))
                .thenThrow(new IllegalArgumentException("No existe un tipo de producto con el ID 999"));

        // Ejecución del método
        Response response = tipoProductoController.actualizarTipoProducto(999, tipoProductoDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("No existe un tipo de producto con el ID 999", response.getEntity());

        verify(tipoProductoService, times(1)).actualizarTipoProducto(eq(999), any(TipoProductoDTO.class));
    }

    @Test
    void testObtenerTipoProductoPorId_Existente() {
        // Configuración del mock
        when(tipoProductoService.obtenerTipoProductoPorId(1)).thenReturn(Optional.of(tipoProductoDTO));

        // Ejecución del método
        Response response = tipoProductoController.obtenerTipoProductoPorId(1);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(tipoProductoDTO, response.getEntity());

        verify(tipoProductoService, times(1)).obtenerTipoProductoPorId(1);
    }

    @Test
    void testObtenerTipoProductoPorId_NoExistente() {
        // Configuración del mock
        when(tipoProductoService.obtenerTipoProductoPorId(999)).thenReturn(Optional.empty());

        // Ejecución del método
        Response response = tipoProductoController.obtenerTipoProductoPorId(999);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Tipo de producto no encontrado con ID: 999", response.getEntity());

        verify(tipoProductoService, times(1)).obtenerTipoProductoPorId(999);
    }

    @Test
    void testListarTiposProductos_TodosLosTiposProductos() {
        // Datos de prueba
        TipoProductoDTO tipoProducto2 = new TipoProductoDTO(2, "Comida", true, "Obs 2");
        List<TipoProductoDTO> tiposProductos = Arrays.asList(tipoProductoDTO, tipoProducto2);

        // Configuración del mock
        when(tipoProductoService.listarTiposProductos()).thenReturn(tiposProductos);

        // Ejecución del método
        Response response = tipoProductoController.listarTiposProductos(null, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<TipoProductoDTO> resultado = (List<TipoProductoDTO>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(tipoProductoService, times(1)).listarTiposProductos();
        verify(tipoProductoService, never()).buscarTiposProductosPorNombre(any());
        verify(tipoProductoService, never()).buscarTiposProductosPorEstado(any());
    }

    @Test
    void testListarTiposProductos_FiltrarPorNombre() {
        // Datos de prueba
        List<TipoProductoDTO> tiposProductos = List.of(tipoProductoDTO);

        // Configuración del mock
        when(tipoProductoService.buscarTiposProductosPorNombre("Bebida")).thenReturn(tiposProductos);

        // Ejecución del método
        Response response = tipoProductoController.listarTiposProductos("Bebida", null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<TipoProductoDTO> resultado = (List<TipoProductoDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("Bebida", resultado.get(0).getNombre());

        verify(tipoProductoService, times(1)).buscarTiposProductosPorNombre("Bebida");
        verify(tipoProductoService, never()).listarTiposProductos();
        verify(tipoProductoService, never()).buscarTiposProductosPorEstado(any());
    }

    @Test
    void testListarTiposProductos_FiltrarPorEstado() {
        // Datos de prueba
        List<TipoProductoDTO> tiposProductos = List.of(tipoProductoDTO);

        // Configuración del mock
        when(tipoProductoService.buscarTiposProductosPorEstado(true)).thenReturn(tiposProductos);

        // Ejecución del método
        Response response = tipoProductoController.listarTiposProductos(null, true);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<TipoProductoDTO> resultado = (List<TipoProductoDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(tipoProductoService, times(1)).buscarTiposProductosPorEstado(true);
        verify(tipoProductoService, never()).listarTiposProductos();
        verify(tipoProductoService, never()).buscarTiposProductosPorNombre(any());
    }

    @Test
    void testEliminarTipoProducto_Exitoso() {
        // Configuración del mock
        when(tipoProductoService.eliminarTipoProducto(1)).thenReturn(true);

        // Ejecución del método
        Response response = tipoProductoController.eliminarTipoProducto(1);

        // Verificaciones
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());

        verify(tipoProductoService, times(1)).eliminarTipoProducto(1);
    }

    @Test
    void testEliminarTipoProducto_NoExiste() {
        // Configuración del mock
        when(tipoProductoService.eliminarTipoProducto(999)).thenReturn(false);

        // Ejecución del método
        Response response = tipoProductoController.eliminarTipoProducto(999);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Tipo de producto no encontrado con ID: 999", response.getEntity());

        verify(tipoProductoService, times(1)).eliminarTipoProducto(999);
    }
}