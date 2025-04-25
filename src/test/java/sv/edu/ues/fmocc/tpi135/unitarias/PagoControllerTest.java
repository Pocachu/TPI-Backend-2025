package sv.edu.ues.fmocc.tpi135.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDetalleDTO;
import sv.edu.ues.fmocc.tpi135.service.PagoService;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PagoControllerTest {

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private sv.edu.ues.fmocc.tpi135.controller.PagoController pagoController;

    private PagoDTO pagoDTO;
    private PagoDetalleDTO pagoDetalleDTO;
    private Date fecha;

    @BeforeEach
    void setUp() {
        // Datos de prueba para fecha
        fecha = new Date();
        
        // Datos de prueba para detalle
        pagoDetalleDTO = new PagoDetalleDTO();
        pagoDetalleDTO.setIdPagoDetalle(1L);
        pagoDetalleDTO.setIdPago(1L);
        pagoDetalleDTO.setMonto(new BigDecimal("100.00"));
        pagoDetalleDTO.setObservaciones("Detalle de prueba");
        
        // Datos de prueba para pago
        pagoDTO = new PagoDTO();
        pagoDTO.setIdPago(1L);
        pagoDTO.setIdOrden(1L);
        pagoDTO.setFecha(fecha);
        pagoDTO.setMetodoPago("EFECTIVO");
        pagoDTO.setReferencia("Referencia de prueba");
        pagoDTO.setDetalles(List.of(pagoDetalleDTO));
    }

    @Test
    void testCrearPago_Exitoso() {
        // Configuración del mock
        when(pagoService.crearPago(any(PagoDTO.class))).thenReturn(pagoDTO);

        // Ejecución del método
        Response response = pagoController.crearPago(pagoDTO);

        // Verificaciones
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(pagoDTO, response.getEntity());

        verify(pagoService, times(1)).crearPago(any(PagoDTO.class));
    }

    @Test
    void testCrearPago_ErrorValidacion() {
        // Configuración del mock
        when(pagoService.crearPago(any(PagoDTO.class)))
                .thenThrow(new IllegalArgumentException("Error de validación"));

        // Ejecución del método
        Response response = pagoController.crearPago(pagoDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error de validación", response.getEntity());

        verify(pagoService, times(1)).crearPago(any(PagoDTO.class));
    }

    @Test
    void testActualizarPago_Exitoso() {
        // Configuración del mock
        when(pagoService.actualizarPago(eq(1L), any(PagoDTO.class))).thenReturn(pagoDTO);

        // Ejecución del método
        Response response = pagoController.actualizarPago(1L, pagoDTO);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(pagoDTO, response.getEntity());

        verify(pagoService, times(1)).actualizarPago(eq(1L), any(PagoDTO.class));
    }

    @Test
    void testActualizarPago_NoExiste() {
        // Configuración del mock
        when(pagoService.actualizarPago(eq(999L), any(PagoDTO.class)))
                .thenThrow(new IllegalArgumentException("No existe un pago con el ID 999"));

        // Ejecución del método
        Response response = pagoController.actualizarPago(999L, pagoDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("No existe un pago con el ID 999", response.getEntity());

        verify(pagoService, times(1)).actualizarPago(eq(999L), any(PagoDTO.class));
    }

    @Test
    void testObtenerPagoPorId_Existente() {
        // Configuración del mock
        when(pagoService.obtenerPagoPorId(1L)).thenReturn(Optional.of(pagoDTO));

        // Ejecución del método
        Response response = pagoController.obtenerPagoPorId(1L);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(pagoDTO, response.getEntity());

        verify(pagoService, times(1)).obtenerPagoPorId(1L);
    }

    @Test
    void testObtenerPagoPorId_NoExistente() {
        // Configuración del mock
        when(pagoService.obtenerPagoPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Response response = pagoController.obtenerPagoPorId(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Pago no encontrado con ID: 999", response.getEntity());

        verify(pagoService, times(1)).obtenerPagoPorId(999L);
    }

    @Test
    void testListarPagos_TodosLosPagos() {
        // Datos de prueba
        PagoDTO pago2 = new PagoDTO(2L, 2L, new Date(), "TARJETA", "Ref2");
        List<PagoDTO> pagos = Arrays.asList(pagoDTO, pago2);

        // Configuración del mock
        when(pagoService.listarPagos()).thenReturn(pagos);

        // Ejecución del método
        Response response = pagoController.listarPagos(null, null, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<PagoDTO> resultado = (List<PagoDTO>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(pagoService, times(1)).listarPagos();
        verify(pagoService, never()).buscarPagosPorIdOrden(any());
        verify(pagoService, never()).buscarPagosPorFecha(any());
        verify(pagoService, never()).buscarPagosPorMetodoPago(any());
    }

    @Test
    void testListarPagos_FiltrarPorOrden() {
        // Datos de prueba
        List<PagoDTO> pagos = List.of(pagoDTO);

        // Configuración del mock
        when(pagoService.buscarPagosPorIdOrden(1L)).thenReturn(pagos);

        // Ejecución del método
        Response response = pagoController.listarPagos(1L, null, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<PagoDTO> resultado = (List<PagoDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getIdOrden());

        verify(pagoService, times(1)).buscarPagosPorIdOrden(1L);
        verify(pagoService, never()).listarPagos();
        verify(pagoService, never()).buscarPagosPorFecha(any());
        verify(pagoService, never()).buscarPagosPorMetodoPago(any());
    }

    @Test
    void testListarPagos_FiltrarPorFecha() {
        // Datos de prueba
        List<PagoDTO> pagos = List.of(pagoDTO);

        // Configuración del mock
        when(pagoService.buscarPagosPorFecha(eq(fecha))).thenReturn(pagos);

        // Ejecución del método
        Response response = pagoController.listarPagos(null, fecha, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<PagoDTO> resultado = (List<PagoDTO>) response.getEntity();
        assertEquals(1, resultado.size());

        verify(pagoService, times(1)).buscarPagosPorFecha(eq(fecha));
        verify(pagoService, never()).listarPagos();
        verify(pagoService, never()).buscarPagosPorIdOrden(any());
        verify(pagoService, never()).buscarPagosPorMetodoPago(any());
    }

    @Test
    void testListarPagos_FiltrarPorMetodoPago() {
        // Datos de prueba
        List<PagoDTO> pagos = List.of(pagoDTO);

        // Configuración del mock
        when(pagoService.buscarPagosPorMetodoPago("EFECTIVO")).thenReturn(pagos);

        // Ejecución del método
        Response response = pagoController.listarPagos(null, null, "EFECTIVO");

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<PagoDTO> resultado = (List<PagoDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("EFECTIVO", resultado.get(0).getMetodoPago());

        verify(pagoService, times(1)).buscarPagosPorMetodoPago("EFECTIVO");
        verify(pagoService, never()).listarPagos();
        verify(pagoService, never()).buscarPagosPorIdOrden(any());
        verify(pagoService, never()).buscarPagosPorFecha(any());
    }

    @Test
    void testEliminarPago_Exitoso() {
        // Configuración del mock
        when(pagoService.eliminarPago(1L)).thenReturn(true);

        // Ejecución del método
        Response response = pagoController.eliminarPago(1L);

        // Verificaciones
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());

        verify(pagoService, times(1)).eliminarPago(1L);
    }

    @Test
    void testEliminarPago_NoExiste() {
        // Configuración del mock
        when(pagoService.eliminarPago(999L)).thenReturn(false);

        // Ejecución del método
        Response response = pagoController.eliminarPago(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Pago no encontrado con ID: 999", response.getEntity());

        verify(pagoService, times(1)).eliminarPago(999L);
    }

    @Test
    void testObtenerPagosPorOrden() {
        // Datos de prueba
        List<PagoDTO> pagos = List.of(pagoDTO);

        // Configuración del mock
        when(pagoService.buscarPagosPorIdOrden(1L)).thenReturn(pagos);

        // Ejecución del método
        Response response = pagoController.obtenerPagosPorOrden(1L);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<PagoDTO> resultado = (List<PagoDTO>) response.getEntity();
        assertEquals(1, resultado.size());

        verify(pagoService, times(1)).buscarPagosPorIdOrden(1L);
    }
}