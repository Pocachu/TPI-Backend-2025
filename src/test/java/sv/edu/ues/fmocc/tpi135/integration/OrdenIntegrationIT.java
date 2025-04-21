package sv.edu.ues.fmocc.tpi135.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.controller.OrdenController;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDetalleDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetalle;
import sv.edu.ues.fmocc.tpi135.repository.OrdenDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.OrdenDetalleRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.repository.ProductoPrecioRepository;
import sv.edu.ues.fmocc.tpi135.service.OrdenService;
import sv.edu.ues.fmocc.tpi135.service.OrdenServiceImpl;

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
 * servicios y repositorios en el flujo de operaciones CRUD de órdenes.
 */
@ExtendWith(MockitoExtension.class)
public class OrdenIntegrationIT {

    @Mock
    private OrdenRepositoryImpl ordenRepository;

    @Mock
    private OrdenDetalleRepositoryImpl ordenDetalleRepository;
    
    @Mock
    private ProductoPrecioRepository productoPrecioRepository;

    private OrdenService ordenService;
    private OrdenController ordenController;

    private Orden ordenEntity;
    private OrdenDTO ordenDTO;
    private OrdenDetalle ordenDetalleEntity;
    private OrdenDetalleDTO ordenDetalleDTO;
    private Date fecha;

    @BeforeEach
    void setUp() {
        // Configurar capa de servicio con los repositorios mock
        ordenService = new OrdenServiceImpl();
        
        // Usamos reflexión para inyectar los repositorios mock en el servicio
        try {
            java.lang.reflect.Field field = OrdenServiceImpl.class.getDeclaredField("ordenRepository");
            field.setAccessible(true);
            field.set(ordenService, ordenRepository);
            
            field = OrdenServiceImpl.class.getDeclaredField("ordenDetalleRepository");
            field.setAccessible(true);
            field.set(ordenService, ordenDetalleRepository);
            
            field = OrdenServiceImpl.class.getDeclaredField("productoPrecioRepository");
            field.setAccessible(true);
            field.set(ordenService, productoPrecioRepository);
        } catch (Exception e) {
            fail("Error al inyectar mocks: " + e.getMessage());
        }

        // Configurar controlador con el servicio
        ordenController = new OrdenController();
        try {
            java.lang.reflect.Field field = OrdenController.class.getDeclaredField("ordenService");
            field.setAccessible(true);
            field.set(ordenController, ordenService);
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
        
        // Datos de prueba para entidad OrdenDetalle
        ordenDetalleEntity = new OrdenDetalle();
        ordenDetalleEntity.setIdOrden(1L);
        ordenDetalleEntity.setIdProductoPrecio(1L);
        ordenDetalleEntity.setCantidad(2);
        ordenDetalleEntity.setPrecio(new BigDecimal("5.00"));
        ordenDetalleEntity.setObservaciones("Detalle de prueba");

        // Datos de prueba para DTO OrdenDetalle
        ordenDetalleDTO = new OrdenDetalleDTO();
        ordenDetalleDTO.setIdOrden(1L);
        ordenDetalleDTO.setIdProductoPrecio(1L);
        ordenDetalleDTO.setCantidad(2);
        ordenDetalleDTO.setPrecio(new BigDecimal("5.00"));
        ordenDetalleDTO.setObservaciones("Detalle de prueba");
        
        // Datos de prueba para DTO Orden
        ordenDTO = new OrdenDTO();
        ordenDTO.setIdOrden(1L);
        ordenDTO.setFecha(fecha);
        ordenDTO.setSucursal("S001");
        ordenDTO.setAnulada(false);
        ordenDTO.setDetalles(List.of(ordenDetalleDTO));
    }

    @Test
    void testFlujoCrearObtenerOrden() {
        // Configuración de mocks
        when(ordenRepository.crear(any(Orden.class))).thenAnswer(invocation -> {
            Orden o = invocation.getArgument(0);
            o.setIdOrden(1L); // Simulamos generación de ID
            return o;
        });
        
        when(ordenDetalleRepository.crear(any(OrdenDetalle.class))).thenReturn(ordenDetalleEntity);
        
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        when(ordenDetalleRepository.buscarPorIdOrden(1L)).thenReturn(List.of(ordenDetalleEntity));

        // Ejecutar flujo: crear orden
        OrdenDTO inputDTO = new OrdenDTO();
        inputDTO.setFecha(fecha);
        inputDTO.setSucursal("S001");
        inputDTO.setDetalles(List.of(ordenDetalleDTO));

        Response createResponse = ordenController.crearOrden(inputDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());

        OrdenDTO createdDTO = (OrdenDTO) createResponse.getEntity();
        assertNotNull(createdDTO.getIdOrden());
        assertEquals("S001", createdDTO.getSucursal());

        // Ejecutar flujo: obtener orden creada
        Response getResponse = ordenController.obtenerOrdenPorId(1L);
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

        OrdenDTO retrievedDTO = (OrdenDTO) getResponse.getEntity();
        assertEquals(1L, retrievedDTO.getIdOrden());
        assertEquals("S001", retrievedDTO.getSucursal());
        
        // Verificar que se obtuvieron los detalles
        assertNotNull(retrievedDTO.getDetalles());
        assertEquals(1, retrievedDTO.getDetalles().size());
        assertEquals(1L, retrievedDTO.getDetalles().get(0).getIdProductoPrecio());

        // Verificar interacciones entre capas
        verify(ordenRepository).crear(any(Orden.class));
        verify(ordenDetalleRepository).crear(any(OrdenDetalle.class));
        verify(ordenRepository).encontrarPorId(1L);
        verify(ordenDetalleRepository).buscarPorIdOrden(1L);
    }

    @Test
    void testFlujoActualizarOrden() {
        // Configuración de mocks
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        when(ordenRepository.actualizar(any(Orden.class))).thenAnswer(invocation -> {
            Orden o = invocation.getArgument(0);
            return o; // Devolvemos el mismo objeto actualizado
        });
        
        when(ordenDetalleRepository.eliminarPorIdOrden(1L)).thenReturn(1); // 1 detalle eliminado
        
        when(ordenDetalleRepository.crear(any(OrdenDetalle.class))).thenReturn(ordenDetalleEntity);

        // Ejecutar flujo: actualizar orden
        OrdenDTO updateDTO = new OrdenDTO();
        updateDTO.setSucursal("S002");
        updateDTO.setAnulada(true);
        
        // Agregar un nuevo detalle para la actualización
        OrdenDetalleDTO nuevoDetalleDTO = new OrdenDetalleDTO();
        nuevoDetalleDTO.setIdProductoPrecio(2L);
        nuevoDetalleDTO.setCantidad(3);
        nuevoDetalleDTO.setPrecio(new BigDecimal("7.50"));
        
        updateDTO.setDetalles(List.of(nuevoDetalleDTO));

        Response updateResponse = ordenController.actualizarOrden(1L, updateDTO);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        OrdenDTO updatedDTO = (OrdenDTO) updateResponse.getEntity();
        assertEquals(1L, updatedDTO.getIdOrden());
        assertEquals("S002", updatedDTO.getSucursal());
        assertTrue(updatedDTO.getAnulada());

        // Verificar interacciones entre capas
        verify(ordenRepository).encontrarPorId(1L);
        verify(ordenRepository).actualizar(any(Orden.class));
        verify(ordenDetalleRepository).eliminarPorIdOrden(1L);
        verify(ordenDetalleRepository).crear(any(OrdenDetalle.class));
    }

    @Test
    void testFlujoBuscarOrdenesPorCriterios() {
        // Datos de prueba
        Orden orden1 = new Orden(1L, fecha, "S001", false);
        Orden orden2 = new Orden(2L, fecha, "S002", false);
        List<Orden> ordenes = Arrays.asList(orden1, orden2);
        List<Orden> ordenesPorSucursal = List.of(orden1);
        List<Orden> ordenesPorFecha = Arrays.asList(orden1, orden2);
        List<Orden> ordenesPorAnulada = Arrays.asList(orden1, orden2);

        // Configuración de mocks
        when(ordenRepository.listarTodas()).thenReturn(ordenes);
        when(ordenRepository.buscarPorSucursal("S001")).thenReturn(ordenesPorSucursal);
        when(ordenRepository.buscarPorFecha(eq(fecha))).thenReturn(ordenesPorFecha);
        when(ordenRepository.buscarPorAnulada(false)).thenReturn(ordenesPorAnulada);
        
        when(ordenDetalleRepository.buscarPorIdOrden(anyLong())).thenReturn(List.of());

        // Ejecutar flujo: listar todas
        Response listResponse = ordenController.listarOrdenes(null, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), listResponse.getStatus());
        List<OrdenDTO> listedDTOs = (List<OrdenDTO>) listResponse.getEntity();
        assertEquals(2, listedDTOs.size());

        // Ejecutar flujo: buscar por sucursal
        Response sucursalResponse = ordenController.listarOrdenes(null, "S001", null);
        assertEquals(Response.Status.OK.getStatusCode(), sucursalResponse.getStatus());
        List<OrdenDTO> sucursalDTOs = (List<OrdenDTO>) sucursalResponse.getEntity();
        assertEquals(1, sucursalDTOs.size());
        assertEquals("S001", sucursalDTOs.get(0).getSucursal());

        // Ejecutar flujo: buscar por fecha
        Response fechaResponse = ordenController.listarOrdenes(fecha, null, null);
        assertEquals(Response.Status.OK.getStatusCode(), fechaResponse.getStatus());
        List<OrdenDTO> fechaDTOs = (List<OrdenDTO>) fechaResponse.getEntity();
        assertEquals(2, fechaDTOs.size());

        // Ejecutar flujo: buscar por anulada
        Response anuladaResponse = ordenController.listarOrdenes(null, null, false);
        assertEquals(Response.Status.OK.getStatusCode(), anuladaResponse.getStatus());
        List<OrdenDTO> anuladaDTOs = (List<OrdenDTO>) anuladaResponse.getEntity();
        assertEquals(2, anuladaDTOs.size());
        assertFalse(anuladaDTOs.get(0).getAnulada());

        // Verificar interacciones entre capas
        verify(ordenRepository).listarTodas();
        verify(ordenRepository).buscarPorSucursal("S001");
        verify(ordenRepository).buscarPorFecha(eq(fecha));
        verify(ordenRepository).buscarPorAnulada(false);
    }

    @Test
    void testFlujoAnularEliminarOrden() {
        // Configuración de mocks para anular
        when(ordenRepository.anular(1L)).thenReturn(true);
        when(ordenRepository.anular(999L)).thenReturn(false);
        
        // Configuración de mocks para eliminar
        when(ordenDetalleRepository.eliminarPorIdOrden(1L)).thenReturn(1);
        when(ordenRepository.eliminar(1L)).thenReturn(true);
        
        when(ordenDetalleRepository.eliminarPorIdOrden(999L)).thenReturn(0);
        when(ordenRepository.eliminar(999L)).thenReturn(false);

        // Ejecutar flujo: anular orden existente
        Response anularResponse = ordenController.anularOrden(1L);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), anularResponse.getStatus());

        // Ejecutar flujo: intentar anular orden inexistente
        Response anularNotFoundResponse = ordenController.anularOrden(999L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), anularNotFoundResponse.getStatus());
        assertEquals("Orden no encontrada con ID: 999", anularNotFoundResponse.getEntity());

        // Ejecutar flujo: eliminar orden existente
        Response deleteResponse = ordenController.eliminarOrden(1L);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // Ejecutar flujo: intentar eliminar orden inexistente
        Response deleteNotFoundResponse = ordenController.eliminarOrden(999L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), deleteNotFoundResponse.getStatus());
        assertEquals("Orden no encontrada con ID: 999", deleteNotFoundResponse.getEntity());

        // Verificar interacciones entre capas
        verify(ordenRepository).anular(1L);
        verify(ordenRepository).anular(999L);
        verify(ordenDetalleRepository).eliminarPorIdOrden(1L);
        verify(ordenRepository).eliminar(1L);
        verify(ordenDetalleRepository).eliminarPorIdOrden(999L);
        verify(ordenRepository).eliminar(999L);
    }
}