package sv.edu.ues.fmocc.tpi135.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDetalleDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetalle;
import sv.edu.ues.fmocc.tpi135.entity.OrdenDetallePK;
import sv.edu.ues.fmocc.tpi135.entity.ProductoPrecio;
import sv.edu.ues.fmocc.tpi135.repository.OrdenDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.ProductoPrecioRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private OrdenDetalleRepository ordenDetalleRepository;

    @Mock
    private ProductoPrecioRepository productoPrecioRepository;

    @InjectMocks
    private sv.edu.ues.fmocc.tpi135.service.OrdenServiceImpl ordenService;

    private Orden ordenEntity;
    private OrdenDTO ordenDTO;
    private OrdenDetalle ordenDetalleEntity;
    private OrdenDetalleDTO ordenDetalleDTO;
    private ProductoPrecio productoPrecioEntity;

    @BeforeEach
    void setUp() {
        // Datos de prueba para fecha
        Date fecha = new Date();
        
        // Datos de prueba para entidad Orden
        ordenEntity = new Orden();
        ordenEntity.setIdOrden(1L);
        ordenEntity.setFecha(fecha);
        ordenEntity.setSucursal("S001");
        ordenEntity.setAnulada(false);

        // Datos de prueba para DTO Orden
        ordenDTO = new OrdenDTO();
        ordenDTO.setIdOrden(1L);
        ordenDTO.setFecha(fecha);
        ordenDTO.setSucursal("S001");
        ordenDTO.setAnulada(false);
        
        // Datos de prueba para ProductoPrecio
        productoPrecioEntity = new ProductoPrecio();
        productoPrecioEntity.setIdProductoPrecio(1L);
        productoPrecioEntity.setIdProducto(1L);
        productoPrecioEntity.setFechaDesde(fecha);
        productoPrecioEntity.setPrecioSugerido(new BigDecimal("5.00"));
        
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
    }

    @Test
    void testCrearOrden_Exitoso() {
        // Configuración de mocks para la orden
        when(ordenRepository.crear(any(Orden.class))).thenReturn(ordenEntity);
        
        // Configuración de mocks para los detalles
        when(ordenDetalleRepository.crear(any(OrdenDetalle.class))).thenReturn(ordenDetalleEntity);
        
        // Agregar detalles a la orden DTO
        ordenDTO.setDetalles(List.of(ordenDetalleDTO));
        
        // Ejecución del método
        OrdenDTO resultado = ordenService.crearOrden(ordenDTO);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(ordenDTO.getIdOrden(), resultado.getIdOrden());
        assertEquals(ordenDTO.getFecha(), resultado.getFecha());
        assertEquals(ordenDTO.getSucursal(), resultado.getSucursal());
        assertEquals(ordenDTO.getAnulada(), resultado.getAnulada());

        verify(ordenRepository, times(1)).crear(any(Orden.class));
        verify(ordenDetalleRepository, times(1)).crear(any(OrdenDetalle.class));
    }

    @Test
    void testCrearOrden_SinDetalles() {
        // Configuración de mocks
        when(ordenRepository.crear(any(Orden.class))).thenReturn(ordenEntity);
        
        // Ejecución del método
        OrdenDTO resultado = ordenService.crearOrden(ordenDTO);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(ordenDTO.getIdOrden(), resultado.getIdOrden());

        verify(ordenRepository, times(1)).crear(any(Orden.class));
        verify(ordenDetalleRepository, never()).crear(any(OrdenDetalle.class));
    }

    @Test
    void testActualizarOrden_Exitoso() {
        // Configuración de mocks para encontrar la orden existente
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        // Configuración de mocks para actualizar la orden
        when(ordenRepository.actualizar(any(Orden.class))).thenReturn(ordenEntity);
        
        // Datos para actualización
        OrdenDTO dtoActualizado = new OrdenDTO();
        dtoActualizado.setSucursal("S002");
        dtoActualizado.setAnulada(true);
        
        // Ejecución del método
        OrdenDTO resultado = ordenService.actualizarOrden(1L, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdOrden());
        assertEquals("S002", ordenEntity.getSucursal()); // Verificar que se actualizó el campo
        assertTrue(ordenEntity.getAnulada()); // Verificar que se actualizó el campo

        verify(ordenRepository, times(1)).encontrarPorId(1L);
        verify(ordenRepository, times(1)).actualizar(any(Orden.class));
    }

    @Test
    void testActualizarOrden_ConDetalles() {
        // Configuración de mocks para encontrar la orden existente
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        // Configuración de mocks para actualizar la orden
        when(ordenRepository.actualizar(any(Orden.class))).thenReturn(ordenEntity);
        
        // Configuración de mocks para eliminar detalles existentes
        when(ordenDetalleRepository.eliminarPorIdOrden(1L)).thenReturn(1);
        
        // Configuración de mocks para crear nuevos detalles
        when(ordenDetalleRepository.crear(any(OrdenDetalle.class))).thenReturn(ordenDetalleEntity);
        
        // Datos para actualización con nuevos detalles
        OrdenDTO dtoActualizado = new OrdenDTO();
        dtoActualizado.setSucursal("S002");
        dtoActualizado.setDetalles(List.of(ordenDetalleDTO));
        
        // Ejecución del método
        OrdenDTO resultado = ordenService.actualizarOrden(1L, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        
        verify(ordenRepository, times(1)).encontrarPorId(1L);
        verify(ordenRepository, times(1)).actualizar(any(Orden.class));
        verify(ordenDetalleRepository, times(1)).eliminarPorIdOrden(1L);
        verify(ordenDetalleRepository, times(1)).crear(any(OrdenDetalle.class));
    }

    @Test
    void testActualizarOrden_NoExiste() {
        // Configuración de mocks
        when(ordenRepository.encontrarPorId(999L)).thenReturn(Optional.empty());

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> ordenService.actualizarOrden(999L, ordenDTO));

        verify(ordenRepository, times(1)).encontrarPorId(999L);
        verify(ordenRepository, never()).actualizar(any());
    }

    @Test
    void testObtenerOrdenPorId_Existente() {
        // Configuración de mocks para la orden
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        // Configuración de mocks para los detalles
        when(ordenDetalleRepository.buscarPorIdOrden(1L)).thenReturn(List.of(ordenDetalleEntity));
        
        // Configuración de mocks para producto precio
        when(productoPrecioRepository.encontrarPorId(1L)).thenReturn(Optional.of(productoPrecioEntity));

        // Ejecución del método
        Optional<OrdenDTO> resultado = ordenService.obtenerOrdenPorId(1L);

        // Verificaciones
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdOrden());
        assertEquals("S001", resultado.get().getSucursal());
        
        // Verificar que se cargaron los detalles
        assertNotNull(resultado.get().getDetalles());
        assertEquals(1, resultado.get().getDetalles().size());

        verify(ordenRepository, times(1)).encontrarPorId(1L);
        verify(ordenDetalleRepository, times(1)).buscarPorIdOrden(1L);
    }

    @Test
    void testObtenerOrdenPorId_NoExistente() {
        // Configuración de mocks
        when(ordenRepository.encontrarPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Optional<OrdenDTO> resultado = ordenService.obtenerOrdenPorId(999L);

        // Verificaciones
        assertFalse(resultado.isPresent());

        verify(ordenRepository, times(1)).encontrarPorId(999L);
        verify(ordenDetalleRepository, never()).buscarPorIdOrden(anyLong());
    }

    @Test
    void testListarOrdenes() {
        // Datos de prueba
        Orden orden2 = new Orden(2L, new Date(), "S002", false);
        List<Orden> ordenes = Arrays.asList(ordenEntity, orden2);

        // Configuración de mocks
        when(ordenRepository.listarTodas()).thenReturn(ordenes);
        when(ordenDetalleRepository.buscarPorIdOrden(anyLong())).thenReturn(List.of());

        // Ejecución del método
        List<OrdenDTO> resultado = ordenService.listarOrdenes();

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdOrden());
        assertEquals(2L, resultado.get(1).getIdOrden());

        verify(ordenRepository, times(1)).listarTodas();
    }

    @Test
    void testBuscarOrdenesPorSucursal() {
        // Datos de prueba
        List<Orden> ordenes = List.of(ordenEntity);

        // Configuración de mocks
        when(ordenRepository.buscarPorSucursal("S001")).thenReturn(ordenes);
        when(ordenDetalleRepository.buscarPorIdOrden(anyLong())).thenReturn(List.of());

        // Ejecución del método
        List<OrdenDTO> resultado = ordenService.buscarOrdenesPorSucursal("S001");

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("S001", resultado.get(0).getSucursal());

        verify(ordenRepository, times(1)).buscarPorSucursal("S001");
    }

    @Test
    void testBuscarOrdenesPorAnulada() {
        // Datos de prueba
        List<Orden> ordenesNoAnuladas = List.of(ordenEntity);

        // Configuración de mocks
        when(ordenRepository.buscarPorAnulada(false)).thenReturn(ordenesNoAnuladas);
        when(ordenDetalleRepository.buscarPorIdOrden(anyLong())).thenReturn(List.of());

        // Ejecución del método
        List<OrdenDTO> resultado = ordenService.buscarOrdenesPorAnulada(false);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).getAnulada());

        verify(ordenRepository, times(1)).buscarPorAnulada(false);
    }

    @Test
    void testAnularOrden_Exitoso() {
        // Configuración de mocks
        when(ordenRepository.anular(1L)).thenReturn(true);

        // Ejecución del método
        boolean resultado = ordenService.anularOrden(1L);

        // Verificaciones
        assertTrue(resultado);

        verify(ordenRepository, times(1)).anular(1L);
    }

    @Test
    void testAnularOrden_NoExiste() {
        // Configuración de mocks
        when(ordenRepository.anular(999L)).thenReturn(false);

        // Ejecución del método
        boolean resultado = ordenService.anularOrden(999L);

        // Verificaciones
        assertFalse(resultado);

        verify(ordenRepository, times(1)).anular(999L);
    }

    @Test
    void testEliminarOrden_Exitoso() {
        // Configuración de mocks
        when(ordenDetalleRepository.eliminarPorIdOrden(1L)).thenReturn(2); // 2 detalles eliminados
        when(ordenRepository.eliminar(1L)).thenReturn(true);

        // Ejecución del método
        boolean resultado = ordenService.eliminarOrden(1L);

        // Verificaciones
        assertTrue(resultado);

        verify(ordenDetalleRepository, times(1)).eliminarPorIdOrden(1L);
        verify(ordenRepository, times(1)).eliminar(1L);
    }

    @Test
    void testEliminarOrden_NoExiste() {
        // Configuración de mocks
        when(ordenDetalleRepository.eliminarPorIdOrden(999L)).thenReturn(0); // No hay detalles
        when(ordenRepository.eliminar(999L)).thenReturn(false); // No existe la orden

        // Ejecución del método
        boolean resultado = ordenService.eliminarOrden(999L);

        // Verificaciones
        assertFalse(resultado);

        verify(ordenDetalleRepository, times(1)).eliminarPorIdOrden(999L);
        verify(ordenRepository, times(1)).eliminar(999L);
    }
}