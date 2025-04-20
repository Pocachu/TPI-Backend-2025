package test.java.sv.edu.ues.fmocc.tpi135.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.controller.PagoController;
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDetalleDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.Pago;
import sv.edu.ues.fmocc.tpi135.entity.PagoDetalle;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoRepository;
import sv.edu.ues.fmocc.tpi135.service.PagoService;
import sv.edu.ues.fmocc.tpi135.service.PagoServiceImpl;

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

/**
 * Pruebas de integración para validar la interacción entre controladores,
 * servicios y repositorios en el flujo de operaciones CRUD de pagos.
 */
@ExtendWith(MockitoExtension.class)
public class PagoIntegrationIT {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoDetalleRepository pagoDetalleRepository;

    @Mock
    private OrdenRepository ordenRepository;

    private PagoService pagoService;
    private PagoController pagoController;

    private Orden ordenEntity;
    private Pago pagoEntity;
    private PagoDTO pagoDTO;
    private PagoDetalle pagoDetalleEntity;
    private PagoDetalleDTO pagoDetalleDTO;
    private Date fecha;

    @BeforeEach
    void setUp() {
        // Configurar capa de servicio con los repositorios mock
        pagoService = new PagoServiceImpl();
        
        // Usamos reflexión para inyectar los repositorios mock en el servicio
        try {
            java.lang.reflect.Field field = PagoServiceImpl.class.getDeclaredField("pagoRepository");
            field.setAccessible(true);
            field.set(pagoService, pagoRepository);
            
            field = PagoServiceImpl.class.getDeclaredField("pagoDetalleRepository");
            field.setAccessible(true);
            field.set(pagoService, pagoDetalleRepository);
            
            field = PagoServiceImpl.class.getDeclaredField("ordenRepository");
            field.setAccessible(true);
            field.set(pagoService, ordenRepository);
        } catch (Exception e) {
            fail("Error al inyectar mocks: " + e.getMessage());
        }

        // Configurar controlador con el servicio
        pagoController = new PagoController();
        try {
            java.lang.reflect.Field field = PagoController.class.getDeclaredField("pagoService");
            field.setAccessible(true);
            field.set(pagoController, pagoService);
        } catch (Exception e) {
            fail("Error al inyectar servicio: " + e.getMessage());
        }
        
        // Datos de prueba para fecha
        fecha = new Date();
        
        // Datos de prueba para entidad Orden
        ordenEntity = new Orden();
        ordenEntity.setIdOrden(1L);
        ordenEntity.setFecha(fecha);
        ordenEntity.setSucursal("S001");
        ordenEntity.setAnulada(false);
        
        // Datos de prueba para entidad Pago
        pagoEntity = new Pago();
        pagoEntity.setIdPago(1L);
        pagoEntity.setIdOrden(1L);
        pagoEntity.setFecha(fecha);
        pagoEntity.setMetodoPago("EFECTIVO");
        pagoEntity.setReferencia("Referencia de prueba");
        
        // Datos de prueba para DTO Pago
        pagoDTO = new PagoDTO();
        pagoDTO.setIdPago(1L);
        pagoDTO.setIdOrden(1L);
        pagoDTO.setFecha(fecha);
        pagoDTO.setMetodoPago("EFECTIVO");
        pagoDTO.setReferencia("Referencia de prueba");
        
        // Datos de prueba para entidad PagoDetalle
        pagoDetalleEntity = new PagoDetalle();
        pagoDetalleEntity.setIdPagoDetalle(1L);
        pagoDetalleEntity.setIdPago(1L);
        pagoDetalleEntity.setMonto(new BigDecimal("100.00"));
        pagoDetalleEntity.setObservaciones("Detalle de prueba");
        
        // Datos de prueba para DTO PagoDetalle
        pagoDetalleDTO = new PagoDetalleDTO();
        pagoDetalleDTO.setIdPagoDetalle(1L);
        pagoDetalleDTO.setIdPago(1L);
        pagoDetalleDTO.setMonto(new BigDecimal("100.00"));
        pagoDetalleDTO.setObservaciones("Detalle de prueba");
    }

    @Test
    void testFlujoCrearObtenerPago() {
        // Configuración de mocks para verificar existencia de la orden
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        // Configuración de mocks para crear pago
        when(pagoRepository.crear(any(Pago.class))).thenAnswer(invocation -> {
            Pago p = invocation.getArgument(0);
            p.setIdPago(1L); // Simulamos generación de ID
            return p;
        });
        
        // Configuración de mocks para crear detalle
        when(pagoDetalleRepository.crear(any(PagoDetalle.class))).thenReturn(pagoDetalleEntity);
        
        // Configuración de mocks para obtener pago
        when(pagoRepository.encontrarPorId(1L)).thenReturn(Optional.of(pagoEntity));
        
        // Configuración de mocks para obtener detalles
        when(pagoDetalleRepository.buscarPorIdPago(1L)).thenReturn(List.of(pagoDetalleEntity));

        // Ejecutar flujo: crear pago
        PagoDTO inputDTO = new PagoDTO();
        inputDTO.setIdOrden(1L);
        inputDTO.setMetodoPago("EFECTIVO");
        inputDTO.setReferencia("Referencia de prueba");
        
        // Agregar detalle al pago DTO
        PagoDetalleDTO detalleDTO = new PagoDetalleDTO();
        detalleDTO.setMonto(new BigDecimal("100.00"));
        detalleDTO.setObservaciones("Detalle de prueba");
        inputDTO.setDetalles(List.of(detalleDTO));

        Response createResponse = pagoController.crearPago(inputDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());

        PagoDTO createdDTO = (PagoDTO) createResponse.getEntity();
        assertNotNull(createdDTO.getIdPago());
        assertEquals("EFECTIVO", createdDTO.getMetodoPago());

        // Ejecutar flujo: obtener pago creado
        Response getResponse = pagoController.obtenerPagoPorId(1L);
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

        PagoDTO retrievedDTO = (PagoDTO) getResponse.getEntity();
        assertEquals(1L, retrievedDTO.getIdPago());
        assertEquals("EFECTIVO", retrievedDTO.getMetodoPago());
        
        // Verificar que se obtuvieron los detalles
        assertNotNull(retrievedDTO.getDetalles());
        assertEquals(1, retrievedDTO.getDetalles().size());
        assertEquals(new BigDecimal("100.00"), retrievedDTO.getDetalles().get(0).getMonto());

        // Verificar interacciones entre capas
        verify(ordenRepository).encontrarPorId(1L);
        verify(pagoRepository).crear(any(Pago.class));
        verify(pagoDetalleRepository).crear(any(PagoDetalle.class));
        verify(pagoRepository).encontrarPorId(1L);
        verify(pagoDetalleRepository).buscarPorIdPago(1L);
    }

    @Test
    void testFlujoActualizarPago() {
        // Configuración de mocks para encontrar el pago existente
        when(pagoRepository.encontrarPorId(1L)).thenReturn(Optional.of(pagoEntity));
        
        // Configuración de mocks para actualizar el pago
        when(pagoRepository.actualizar(any(Pago.class))).thenAnswer(invocation -> {
            Pago p = invocation.getArgument(0);
            return p; // Devolvemos el mismo objeto actualizado
        });
        
        // Configuración de mocks para eliminar detalles existentes
        when(pagoDetalleRepository.eliminarPorIdPago(1L)).thenReturn(1);
        
        // Configuración de mocks para crear nuevos detalles
        when(pagoDetalleRepository.crear(any(PagoDetalle.class))).thenReturn(pagoDetalleEntity);
        
        // Configuración de mocks para obtener detalles
        when(pagoDetalleRepository.buscarPorIdPago(1L)).thenReturn(List.of(pagoDetalleEntity));

        // Ejecutar flujo: actualizar pago
        PagoDTO updateDTO = new PagoDTO();
        updateDTO.setMetodoPago("TARJETA");
        updateDTO.setReferencia("Nueva referencia");
        
        // Agregar nuevo detalle para la actualización
        PagoDetalleDTO nuevoDetalleDTO = new PagoDetalleDTO();
        nuevoDetalleDTO.setMonto(new BigDecimal("200.00"));
        nuevoDetalleDTO.setObservaciones("Nuevo detalle");
        updateDTO.setDetalles(List.of(nuevoDetalleDTO));

        Response updateResponse = pagoController.actualizarPago(1L, updateDTO);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        PagoDTO updatedDTO = (PagoDTO) updateResponse.getEntity();
        assertEquals(1L, updatedDTO.getIdPago());
        assertEquals("TARJETA", pagoEntity.getMetodoPago()); // Verificar que se actualizó el campo
        assertEquals("Nueva referencia", pagoEntity.getReferencia()); // Verificar que se actualizó el campo

        // Verificar interacciones entre capas
        verify(pagoRepository, times(2)).encontrarPorId(1L); // Una vez para actualizar y otra para obtener después
        verify(pagoRepository).actualizar(any(Pago.class));
        verify(pagoDetalleRepository).eliminarPorIdPago(1L);
        verify(pagoDetalleRepository).crear(any(PagoDetalle.class));
        verify(pagoDetalleRepository, times(2)).buscarPorIdPago(1L); // Una vez para actualizar y otra para obtener después
    }

    @Test
    void testFlujoBuscarPagosPorCriterios() {
        // Datos de prueba
        Pago pago1 = new Pago(1L, 1L, fecha, "EFECTIVO", "Ref1");
        Pago pago2 = new Pago(2L, 2L, fecha, "TARJETA", "Ref2");
        List<Pago> pagos = Arrays.asList(pago1, pago2);
        List<Pago> pagosEfectivo = List.of(pago1);
        List<Pago> pagosTarjeta = List.of(pago2);
        List<Pago> pagosPorOrden = List.of(pago1);

        // Configuración de mocks
        when(pagoRepository.listarTodos()).thenReturn(pagos);
        when(pagoRepository.buscarPorMetodoPago("EFECTIVO")).thenReturn(pagosEfectivo);
        when(pagoRepository.buscarPorMetodoPago("TARJETA")).thenReturn(pagosTarjeta);
        when(pagoRepository.buscarPorIdOrden(1L)).thenReturn(pagosPorOrden);
        when(pagoRepository.buscarPorFecha(eq(fecha))).thenReturn(pagos);
        
        when(pagoDetalleRepository.buscarPorIdPago(anyLong())).thenReturn(List.of());

        // Ejecutar flujo: listar todos
        Response listResponse = pagoController.listarPagos(null, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), listResponse.getStatus());
        List<PagoDTO> listedDTOs = (List<PagoDTO>) listResponse.getEntity();
        assertEquals(2, listedDTOs.size());

        // Ejecutar flujo: buscar por método de pago
        Response efectivoResponse = pagoController.listarPagos(null, null, "EFECTIVO");
        assertEquals(Response.Status.OK.getStatusCode(), efectivoResponse.getStatus());
        List<PagoDTO> efectivoDTOs = (List<PagoDTO>) efectivoResponse.getEntity();
        assertEquals(1, efectivoDTOs.size());
        assertEquals("EFECTIVO", efectivoDTOs.get(0).getMetodoPago());

        // Ejecutar flujo: buscar por orden
        Response ordenResponse = pagoController.listarPagos(1L, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), ordenResponse.getStatus());
        List<PagoDTO> ordenDTOs = (List<PagoDTO>) ordenResponse.getEntity();
        assertEquals(1, ordenDTOs.size());
        assertEquals(1L, ordenDTOs.get(0).getIdOrden());

        // Ejecutar flujo: buscar por fecha
        Response fechaResponse = pagoController.listarPagos(null, fecha, null);
        assertEquals(Response.Status.OK.getStatusCode(), fechaResponse.getStatus());
        List<PagoDTO> fechaDTOs = (List<PagoDTO>) fechaResponse.getEntity();
        assertEquals(2, fechaDTOs.size());

        // Verificar interacciones entre capas
        verify(pagoRepository).listarTodos();
        verify(pagoRepository).buscarPorMetodoPago("EFECTIVO");
        verify(pagoRepository).buscarPorIdOrden(1L);
        verify(pagoRepository).buscarPorFecha(eq(fecha));
    }
    
    @Test
    void testFlujoEliminarPago() {
        // Configuración de mocks
        when(pagoDetalleRepository.eliminarPorIdPago(1L)).thenReturn(2); // 2 detalles eliminados
        when(pagoRepository.eliminar(1L)).thenReturn(true);
        
        when(pagoDetalleRepository.eliminarPorIdPago(999L)).thenReturn(0); // No hay detalles
        when(pagoRepository.eliminar(999L)).thenReturn(false);

        // Ejecutar flujo: eliminar pago existente
        Response deleteResponse = pagoController.eliminarPago(1L);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // Ejecutar flujo: intentar eliminar pago inexistente
        Response deleteNotFoundResponse = pagoController.eliminarPago(999L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), deleteNotFoundResponse.getStatus());
        assertEquals("Pago no encontrado con ID: 999", deleteNotFoundResponse.getEntity());

        // Verificar interacciones entre capas
        verify(pagoDetalleRepository).eliminarPorIdPago(1L);
        verify(pagoRepository).eliminar(1L);
        verify(pagoDetalleRepository).eliminarPorIdPago(999L);
        verify(pagoRepository).eliminar(999L);
    }
    
    @Test
    void testFlujoObtenerPagosPorOrden() {
        // Datos de prueba
        Pago pago1 = new Pago(1L, 1L, fecha, "EFECTIVO", "Ref1");
        Pago pago2 = new Pago(2L, 1L, fecha, "TARJETA", "Ref2");
        List<Pago> pagosPorOrden = Arrays.asList(pago1, pago2);

        // Configuración de mocks
        when(pagoRepository.buscarPorIdOrden(1L)).thenReturn(pagosPorOrden);
        when(pagoDetalleRepository.buscarPorIdPago(anyLong())).thenReturn(List.of());

        // Ejecutar flujo: obtener pagos por orden
        Response response = pagoController.obtenerPagosPorOrden(1L);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<PagoDTO> result = (List<PagoDTO>) response.getEntity();
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getIdOrden());
        assertEquals(1L, result.get(1).getIdOrden());

        // Verificar interacciones entre capas
        verify(pagoRepository).buscarPorIdOrden(1L);
    }
}