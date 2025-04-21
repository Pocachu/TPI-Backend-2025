package sv.edu.ues.fmocc.tpi135.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDetalleDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.Pago;
import sv.edu.ues.fmocc.tpi135.entity.PagoDetalle;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoDetalleRepository pagoDetalleRepository;

    @Mock
    private OrdenRepository ordenRepository;

    @InjectMocks
    private sv.edu.ues.fmocc.tpi135.service.PagoServiceImpl pagoService;

    private Pago pagoEntity;
    private PagoDTO pagoDTO;
    private PagoDetalle pagoDetalleEntity;
    private PagoDetalleDTO pagoDetalleDTO;
    private Orden ordenEntity;
    private Date fecha;

    @BeforeEach
    void setUp() {
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
    void testCrearPago_Exitoso() {
        // Configuración de mocks para verificar existencia de la orden
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        // Configuración de mocks para la creación del pago
        when(pagoRepository.crear(any(Pago.class))).thenReturn(pagoEntity);
        
        // Configuración de mocks para la creación de detalles
        when(pagoDetalleRepository.crear(any(PagoDetalle.class))).thenReturn(pagoDetalleEntity);
        
        // Agregar detalles al pago DTO
        pagoDTO.setDetalles(List.of(pagoDetalleDTO));
        
        // Ejecución del método
        PagoDTO resultado = pagoService.crearPago(pagoDTO);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(pagoDTO.getIdPago(), resultado.getIdPago());
        assertEquals(pagoDTO.getIdOrden(), resultado.getIdOrden());
        assertEquals(pagoDTO.getFecha(), resultado.getFecha());
        assertEquals(pagoDTO.getMetodoPago(), resultado.getMetodoPago());
        assertEquals(pagoDTO.getReferencia(), resultado.getReferencia());

        verify(ordenRepository, times(1)).encontrarPorId(1L);
        verify(pagoRepository, times(1)).crear(any(Pago.class));
        verify(pagoDetalleRepository, times(1)).crear(any(PagoDetalle.class));
    }

    @Test
    void testCrearPago_OrdenNoExiste() {
        // Configuración de mocks para verificar existencia de la orden
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.empty());
        
        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> pagoService.crearPago(pagoDTO));

        verify(ordenRepository, times(1)).encontrarPorId(1L);
        verify(pagoRepository, never()).crear(any());
    }
    
    @Test
    void testCrearPago_SinDetalles() {
        // Configuración de mocks para verificar existencia de la orden
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));
        
        // Configuración de mocks para la creación del pago
        when(pagoRepository.crear(any(Pago.class))).thenReturn(pagoEntity);
        
        // Ejecución del método
        PagoDTO resultado = pagoService.crearPago(pagoDTO);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(pagoDTO.getIdPago(), resultado.getIdPago());

        verify(ordenRepository, times(1)).encontrarPorId(1L);
        verify(pagoRepository, times(1)).crear(any(Pago.class));
        verify(pagoDetalleRepository, never()).crear(any(PagoDetalle.class));
    }

    @Test
    void testActualizarPago_Exitoso() {
        // Configuración de mocks para encontrar el pago existente
        when(pagoRepository.encontrarPorId(1L)).thenReturn(Optional.of(pagoEntity));
        
        // Configuración de mocks para actualizar el pago
        when(pagoRepository.actualizar(any(Pago.class))).thenReturn(pagoEntity);
        
        // Datos para actualización
        PagoDTO dtoActualizado = new PagoDTO();
        dtoActualizado.setMetodoPago("TARJETA");
        dtoActualizado.setReferencia("Nueva referencia");
        
        // Ejecución del método
        PagoDTO resultado = pagoService.actualizarPago(1L, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPago());
        assertEquals("TARJETA", pagoEntity.getMetodoPago()); // Verificar que se actualizó el campo
        assertEquals("Nueva referencia", pagoEntity.getReferencia()); // Verificar que se actualizó el campo

        verify(pagoRepository, times(1)).encontrarPorId(1L);
        verify(pagoRepository, times(1)).actualizar(any(Pago.class));
    }

    @Test
    void testActualizarPago_ConDetalles() {
        // Configuración de mocks para encontrar el pago existente
        when(pagoRepository.encontrarPorId(1L)).thenReturn(Optional.of(pagoEntity));
        
        // Configuración de mocks para actualizar el pago
        when(pagoRepository.actualizar(any(Pago.class))).thenReturn(pagoEntity);
        
        // Configuración de mocks para eliminar detalles existentes
        when(pagoDetalleRepository.eliminarPorIdPago(1L)).thenReturn(1);
        
        // Configuración de mocks para crear nuevos detalles
        when(pagoDetalleRepository.crear(any(PagoDetalle.class))).thenReturn(pagoDetalleEntity);
        
        // Datos para actualización con nuevos detalles
        PagoDTO dtoActualizado = new PagoDTO();
        dtoActualizado.setMetodoPago("TARJETA");
        dtoActualizado.setDetalles(List.of(pagoDetalleDTO));
        
        // Ejecución del método
        PagoDTO resultado = pagoService.actualizarPago(1L, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        
        verify(pagoRepository, times(1)).encontrarPorId(1L);
        verify(pagoRepository, times(1)).actualizar(any(Pago.class));
        verify(pagoDetalleRepository, times(1)).eliminarPorIdPago(1L);
        verify(pagoDetalleRepository, times(1)).crear(any(PagoDetalle.class));
    }

    @Test
    void testActualizarPago_CambioDeOrden() {
        // Configuración de mocks para encontrar el pago existente
        when(pagoRepository.encontrarPorId(1L)).thenReturn(Optional.of(pagoEntity));
        
        // Configuración de mocks para verificar existencia de la nueva orden
        when(ordenRepository.encontrarPorId(2L)).thenReturn(Optional.of(ordenEntity));
        
        // Configuración de mocks para actualizar el pago
        when(pagoRepository.actualizar(any(Pago.class))).thenReturn(pagoEntity);
        
        // Datos para actualización
        PagoDTO dtoActualizado = new PagoDTO();
        dtoActualizado.setIdOrden(2L);
        
        // Ejecución del método
        PagoDTO resultado = pagoService.actualizarPago(1L, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(2L, pagoEntity.getIdOrden()); // Verificar que se actualizó el campo

        verify(pagoRepository, times(1)).encontrarPorId(1L);
        verify(ordenRepository, times(1)).encontrarPorId(2L);
        verify(pagoRepository, times(1)).actualizar(any(Pago.class));
    }

    @Test
    void testActualizarPago_OrdenNoExiste() {
        // Configuración de mocks para encontrar el pago existente
        when(pagoRepository.encontrarPorId(1L)).thenReturn(Optional.of(pagoEntity));
        
        // Configuración de mocks para verificar existencia de la nueva orden
        when(ordenRepository.encontrarPorId(2L)).thenReturn(Optional.empty());
        
        // Datos para actualización
        PagoDTO dtoActualizado = new PagoDTO();
        dtoActualizado.setIdOrden(2L);
        
        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> pagoService.actualizarPago(1L, dtoActualizado));

        verify(pagoRepository, times(1)).encontrarPorId(1L);
        verify(ordenRepository, times(1)).encontrarPorId(2L);
        verify(pagoRepository, never()).actualizar(any());
    }

    @Test
    void testActualizarPago_NoExiste() {
        // Configuración de mocks
        when(pagoRepository.encontrarPorId(999L)).thenReturn(Optional.empty());

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> pagoService.actualizarPago(999L, pagoDTO));

        verify(pagoRepository, times(1)).encontrarPorId(999L);
        verify(pagoRepository, never()).actualizar(any());
    }

    @Test
    void testObtenerPagoPorId_Existente() {
        // Configuración de mocks para el pago
        when(pagoRepository.encontrarPorId(1L)).thenReturn(Optional.of(pagoEntity));
        
        // Configuración de mocks para los detalles
        when(pagoDetalleRepository.buscarPorIdPago(1L)).thenReturn(List.of(pagoDetalleEntity));
        
        // Configuración de mocks para orden relacionada
        when(ordenRepository.encontrarPorId(1L)).thenReturn(Optional.of(ordenEntity));

        // Ejecución del método
        Optional<PagoDTO> resultado = pagoService.obtenerPagoPorId(1L);

        // Verificaciones
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdPago());
        assertEquals("EFECTIVO", resultado.get().getMetodoPago());
        
        // Verificar que se cargaron los detalles
        assertNotNull(resultado.get().getDetalles());
        assertEquals(1, resultado.get().getDetalles().size());

        verify(pagoRepository, times(1)).encontrarPorId(1L);
        verify(pagoDetalleRepository, times(1)).buscarPorIdPago(1L);
        verify(ordenRepository, times(1)).encontrarPorId(1L);
    }

    @Test
    void testObtenerPagoPorId_NoExistente() {
        // Configuración de mocks
        when(pagoRepository.encontrarPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Optional<PagoDTO> resultado = pagoService.obtenerPagoPorId(999L);

        // Verificaciones
        assertFalse(resultado.isPresent());

        verify(pagoRepository, times(1)).encontrarPorId(999L);
        verify(pagoDetalleRepository, never()).buscarPorIdPago(anyLong());
    }

    @Test
    void testListarPagos() {
        // Datos de prueba
        Pago pago2 = new Pago(2L, 2L, new Date(), "TARJETA", "Ref2");
        List<Pago> pagos = Arrays.asList(pagoEntity, pago2);

        // Configuración de mocks
        when(pagoRepository.listarTodos()).thenReturn(pagos);
        when(pagoDetalleRepository.buscarPorIdPago(anyLong())).thenReturn(List.of());

        // Ejecución del método
        List<PagoDTO> resultado = pagoService.listarPagos();

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdPago());
        assertEquals(2L, resultado.get(1).getIdPago());

        verify(pagoRepository, times(1)).listarTodos();
    }

    @Test
    void testBuscarPagosPorIdOrden() {
        // Datos de prueba
        List<Pago> pagos = List.of(pagoEntity);

        // Configuración de mocks
        when(pagoRepository.buscarPorIdOrden(1L)).thenReturn(pagos);
        when(pagoDetalleRepository.buscarPorIdPago(anyLong())).thenReturn(List.of());

        // Ejecución del método
        List<PagoDTO> resultado = pagoService.buscarPagosPorIdOrden(1L);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getIdOrden());

        verify(pagoRepository, times(1)).buscarPorIdOrden(1L);
    }

    @Test
    void testBuscarPagosPorMetodoPago() {
        // Datos de prueba
        List<Pago> pagosEfectivo = List.of(pagoEntity);

        // Configuración de mocks
        when(pagoRepository.buscarPorMetodoPago("EFECTIVO")).thenReturn(pagosEfectivo);
        when(pagoDetalleRepository.buscarPorIdPago(anyLong())).thenReturn(List.of());

        // Ejecución del método
        List<PagoDTO> resultado = pagoService.buscarPagosPorMetodoPago("EFECTIVO");

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("EFECTIVO", resultado.get(0).getMetodoPago());

        verify(pagoRepository, times(1)).buscarPorMetodoPago("EFECTIVO");
    }

    @Test
    void testEliminarPago_Exitoso() {
        // Configuración de mocks
        when(pagoDetalleRepository.eliminarPorIdPago(1L)).thenReturn(2); // 2 detalles eliminados
        when(pagoRepository.eliminar(1L)).thenReturn(true);

        // Ejecución del método
        boolean resultado = pagoService.eliminarPago(1L);

        // Verificaciones
        assertTrue(resultado);

        verify(pagoDetalleRepository, times(1)).eliminarPorIdPago(1L);
        verify(pagoRepository, times(1)).eliminar(1L);
    }

    @Test
    void testEliminarPago_NoExiste() {
        // Configuración de mocks
        when(pagoDetalleRepository.eliminarPorIdPago(999L)).thenReturn(0); // No hay detalles
        when(pagoRepository.eliminar(999L)).thenReturn(false); // No existe el pago

        // Ejecución del método
        boolean resultado = pagoService.eliminarPago(999L);

        // Verificaciones
        assertFalse(resultado);

        verify(pagoDetalleRepository, times(1)).eliminarPorIdPago(999L);
        verify(pagoRepository, times(1)).eliminar(999L);
    }}