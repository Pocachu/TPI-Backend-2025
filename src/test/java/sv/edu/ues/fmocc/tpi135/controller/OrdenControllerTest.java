package sv.edu.ues.fmocc.tpi135.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDetalleDTO;
import sv.edu.ues.fmocc.tpi135.service.OrdenService;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenControllerTest {

    @Mock
    private OrdenService ordenService;

    @InjectMocks
    private sv.edu.ues.fmocc.tpi135.controller.OrdenController ordenController;

    private OrdenDTO ordenDTO;
    private OrdenDetalleDTO detalleDTO;
    private Date fecha;

    @BeforeEach
    void setUp() {
        // Datos de prueba para fecha
        fecha = new Date();
        
        // Datos de prueba para detalle
        detalleDTO = new OrdenDetalleDTO();
        detalleDTO.setIdProductoPrecio(1L);
        detalleDTO.setCantidad(2);
        detalleDTO.setPrecio(new BigDecimal("5.00"));
        detalleDTO.setObservaciones("Detalle de prueba");
        
        // Datos de prueba para orden
        ordenDTO = new OrdenDTO();
        ordenDTO.setIdOrden(1L);
        ordenDTO.setFecha(fecha);
        ordenDTO.setSucursal("S001");
        ordenDTO.setAnulada(false);
        ordenDTO.setDetalles(List.of(detalleDTO));
    }

    @Test
    void testCrearOrden_Exitoso() {
        // Configuración del mock
        when(ordenService.crearOrden(any(OrdenDTO.class))).thenReturn(ordenDTO);

        // Ejecución del método
        Response response = ordenController.crearOrden(ordenDTO);

        // Verificaciones
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(ordenDTO, response.getEntity());

        verify(ordenService, times(1)).crearOrden(any(OrdenDTO.class));
    }

    @Test
    void testCrearOrden_ErrorValidacion() {
        // Configuración del mock
        when(ordenService.crearOrden(any(OrdenDTO.class)))
                .thenThrow(new IllegalArgumentException("Error de validación"));

        // Ejecución del método
        Response response = ordenController.crearOrden(ordenDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error de validación", response.getEntity());

        verify(ordenService, times(1)).crearOrden(any(OrdenDTO.class));
    }

    @Test
    void testActualizarOrden_Exitoso() {
        // Configuración del mock
        when(ordenService.actualizarOrden(eq(1L), any(OrdenDTO.class))).thenReturn(ordenDTO);

        // Ejecución del método
        Response response = ordenController.actualizarOrden(1L, ordenDTO);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(ordenDTO, response.getEntity());

        verify(ordenService, times(1)).actualizarOrden(eq(1L), any(OrdenDTO.class));
    }

    @Test
    void testActualizarOrden_NoExiste() {
        // Configuración del mock
        when(ordenService.actualizarOrden(eq(999L), any(OrdenDTO.class)))
                .thenThrow(new IllegalArgumentException("No existe una orden con el ID 999"));

        // Ejecución del método
        Response response = ordenController.actualizarOrden(999L, ordenDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("No existe una orden con el ID 999", response.getEntity());

        verify(ordenService, times(1)).actualizarOrden(eq(999L), any(OrdenDTO.class));
    }

    @Test
    void testObtenerOrdenPorId_Existente() {
        // Configuración del mock
        when(ordenService.obtenerOrdenPorId(1L)).thenReturn(Optional.of(ordenDTO));

        // Ejecución del método
        Response response = ordenController.obtenerOrdenPorId(1L);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(ordenDTO, response.getEntity());

        verify(ordenService, times(1)).obtenerOrdenPorId(1L);
    }

    @Test
    void testObtenerOrdenPorId_NoExistente() {
        // Configuración del mock
        when(ordenService.obtenerOrdenPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Response response = ordenController.obtenerOrdenPorId(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Orden no encontrada con ID: 999", response.getEntity());

        verify(ordenService, times(1)).obtenerOrdenPorId(999L);
    }

    @Test
    void testListarOrdenes_TodasLasOrdenes() {
        // Datos de prueba
        OrdenDTO orden2 = new OrdenDTO(2L, fecha, "S002", false);
        List<OrdenDTO> ordenes = Arrays.asList(ordenDTO, orden2);

        // Configuración del mock
        when(ordenService.listarOrdenes()).thenReturn(ordenes);

        // Ejecución del método
        Response response = ordenController.listarOrdenes(null, null, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<OrdenDTO> resultado = (List<OrdenDTO>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(ordenService, times(1)).listarOrdenes();
        verify(ordenService, never()).buscarOrdenesPorFecha(any());
        verify(ordenService, never()).buscarOrdenesPorSucursal(any());
        verify(ordenService, never()).buscarOrdenesPorAnulada(any());
    }

    @Test
    void testListarOrdenes_FiltrarPorFecha() {
        // Datos de prueba
        List<OrdenDTO> ordenes = List.of(ordenDTO);

        // Configuración del mock
        when(ordenService.buscarOrdenesPorFecha(fecha)).thenReturn(ordenes);

        // Ejecución del método
        Response response = ordenController.listarOrdenes(fecha, null, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<OrdenDTO> resultado = (List<OrdenDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals(fecha, resultado.get(0).getFecha());

        verify(ordenService, times(1)).buscarOrdenesPorFecha(fecha);
        verify(ordenService, never()).listarOrdenes();
        verify(ordenService, never()).buscarOrdenesPorSucursal(any());
        verify(ordenService, never()).buscarOrdenesPorAnulada(any());
    }

    @Test
    void testListarOrdenes_FiltrarPorSucursal() {
        // Datos de prueba
        List<OrdenDTO> ordenes = List.of(ordenDTO);

        // Configuración del mock
        when(ordenService.buscarOrdenesPorSucursal("S001")).thenReturn(ordenes);

        // Ejecución del método
        Response response = ordenController.listarOrdenes(null, "S001", null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<OrdenDTO> resultado = (List<OrdenDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("S001", resultado.get(0).getSucursal());

        verify(ordenService, times(1)).buscarOrdenesPorSucursal("S001");
        verify(ordenService, never()).listarOrdenes();
        verify(ordenService, never()).buscarOrdenesPorFecha(any());
        verify(ordenService, never()).buscarOrdenesPorAnulada(any());
    }

    @Test
    void testListarOrdenes_FiltrarPorAnulada() {
        // Datos de prueba
        List<OrdenDTO> ordenes = List.of(ordenDTO);

        // Configuración del mock
        when(ordenService.buscarOrdenesPorAnulada(false)).thenReturn(ordenes);

        // Ejecución del método
        Response response = ordenController.listarOrdenes(null, null, false);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<OrdenDTO> resultado = (List<OrdenDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).getAnulada());

        verify(ordenService, times(1)).buscarOrdenesPorAnulada(false);
        verify(ordenService, never()).listarOrdenes();
        verify(ordenService, never()).buscarOrdenesPorFecha(any());
        verify(ordenService, never()).buscarOrdenesPorSucursal(any());
    }

    @Test
    void testAnularOrden_Exitoso() {
        // Configuración del mock
        when(ordenService.anularOrden(1L)).thenReturn(true);

        // Ejecución del método
        Response response = ordenController.anularOrden(1L);

        // Verificaciones
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());

        verify(ordenService, times(1)).anularOrden(1L);
    }

    @Test
    void testAnularOrden_NoExiste() {
        // Configuración del mock
        when(ordenService.anularOrden(999L)).thenReturn(false);

        // Ejecución del método
        Response response = ordenController.anularOrden(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Orden no encontrada con ID: 999", response.getEntity());

        verify(ordenService, times(1)).anularOrden(999L);
    }

    @Test
    void testEliminarOrden_Exitoso() {
        // Configuración del mock
        when(ordenService.eliminarOrden(1L)).thenReturn(true);

        // Ejecución del método
        Response response = ordenController.eliminarOrden(1L);

        // Verificaciones
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());

        verify(ordenService, times(1)).eliminarOrden(1L);
    }

    @Test
    void testEliminarOrden_NoExiste() {
        // Configuración del mock
        when(ordenService.eliminarOrden(999L)).thenReturn(false);

        // Ejecución del método
        Response response = ordenController.eliminarOrden(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Orden no encontrada con ID: 999", response.getEntity());

        verify(ordenService, times(1)).eliminarOrden(999L);
    }}