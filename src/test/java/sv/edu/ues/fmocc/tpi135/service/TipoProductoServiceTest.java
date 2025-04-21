package sv.edu.ues.fmocc.tpi135.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.TipoProducto;
import sv.edu.ues.fmocc.tpi135.repository.TipoProductoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoProductoServiceTest {

    @Mock
    private TipoProductoRepository tipoProductoRepository;

    @InjectMocks
    private sv.edu.ues.fmocc.tpi135.service.TipoProductoServiceImpl tipoProductoService;

    private TipoProducto tipoProductoEntity;
    private TipoProductoDTO tipoProductoDTO;

    @BeforeEach
    void setUp() {
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
    void testCrearTipoProducto_Exitoso() {
        // Configuración del mock
        when(tipoProductoRepository.crear(any(TipoProducto.class))).thenReturn(tipoProductoEntity);

        // Ejecución del método
        TipoProductoDTO resultado = tipoProductoService.crearTipoProducto(tipoProductoDTO);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(tipoProductoDTO.getIdTipoProducto(), resultado.getIdTipoProducto());
        assertEquals(tipoProductoDTO.getNombre(), resultado.getNombre());
        assertEquals(tipoProductoDTO.getActivo(), resultado.getActivo());
        assertEquals(tipoProductoDTO.getObservaciones(), resultado.getObservaciones());

        verify(tipoProductoRepository, times(1)).crear(any(TipoProducto.class));
    }

    @Test
    void testCrearTipoProducto_NombreVacio() {
        // Datos de prueba
        TipoProductoDTO dtoInvalido = new TipoProductoDTO();
        dtoInvalido.setNombre("");

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> tipoProductoService.crearTipoProducto(dtoInvalido));

        verify(tipoProductoRepository, never()).crear(any());
    }

    @Test
    void testActualizarTipoProducto_Exitoso() {
        // Configuración del mock
        when(tipoProductoRepository.encontrarPorId(1)).thenReturn(Optional.of(tipoProductoEntity));
        when(tipoProductoRepository.actualizar(any(TipoProducto.class))).thenReturn(tipoProductoEntity);

        // Datos de prueba
        TipoProductoDTO dtoActualizado = new TipoProductoDTO();
        dtoActualizado.setNombre("Bebida Actualizada");
        dtoActualizado.setActivo(false);
        dtoActualizado.setObservaciones("Nuevas observaciones");

        // Ejecución del método
        TipoProductoDTO resultado = tipoProductoService.actualizarTipoProducto(1, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdTipoProducto());
        assertEquals("Bebida Actualizada", resultado.getNombre());
        assertEquals(false, resultado.getActivo());
        assertEquals("Nuevas observaciones", resultado.getObservaciones());

        verify(tipoProductoRepository, times(1)).encontrarPorId(1);
        verify(tipoProductoRepository, times(1)).actualizar(any(TipoProducto.class));
    }

    @Test
    void testActualizarTipoProducto_NoExiste() {
        // Configuración del mock
        when(tipoProductoRepository.encontrarPorId(1)).thenReturn(Optional.empty());

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> tipoProductoService.actualizarTipoProducto(1, tipoProductoDTO));

        verify(tipoProductoRepository, times(1)).encontrarPorId(1);
        verify(tipoProductoRepository, never()).actualizar(any());
    }

    @Test
    void testObtenerTipoProductoPorId_Existente() {
        // Configuración del mock
        when(tipoProductoRepository.encontrarPorId(1)).thenReturn(Optional.of(tipoProductoEntity));

        // Ejecución del método
        Optional<TipoProductoDTO> resultado = tipoProductoService.obtenerTipoProductoPorId(1);

        // Verificaciones
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getIdTipoProducto());
        assertEquals("Bebida", resultado.get().getNombre());

        verify(tipoProductoRepository, times(1)).encontrarPorId(1);
    }

    @Test
    void testObtenerTipoProductoPorId_NoExistente() {
        // Configuración del mock
        when(tipoProductoRepository.encontrarPorId(999)).thenReturn(Optional.empty());

        // Ejecución del método
        Optional<TipoProductoDTO> resultado = tipoProductoService.obtenerTipoProductoPorId(999);

        // Verificaciones
        assertFalse(resultado.isPresent());

        verify(tipoProductoRepository, times(1)).encontrarPorId(999);
    }

    @Test
    void testListarTiposProductos() {
        // Datos de prueba
        TipoProducto tipoProducto2 = new TipoProducto(2, "Comida", true, "Obs 2");
        List<TipoProducto> tiposProductos = Arrays.asList(tipoProductoEntity, tipoProducto2);

        // Configuración del mock
        when(tipoProductoRepository.listarTodos()).thenReturn(tiposProductos);

        // Ejecución del método
        List<TipoProductoDTO> resultado = tipoProductoService.listarTiposProductos();

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getIdTipoProducto());
        assertEquals(2, resultado.get(1).getIdTipoProducto());

        verify(tipoProductoRepository, times(1)).listarTodos();
    }

    @Test
    void testBuscarTiposProductosPorNombre() {
        // Datos de prueba
        List<TipoProducto> tiposProductos = List.of(tipoProductoEntity);

        // Configuración del mock
        when(tipoProductoRepository.buscarPorNombre("Bebida")).thenReturn(tiposProductos);

        // Ejecución del método
        List<TipoProductoDTO> resultado = tipoProductoService.buscarTiposProductosPorNombre("Bebida");

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Bebida", resultado.get(0).getNombre());

        verify(tipoProductoRepository, times(1)).buscarPorNombre("Bebida");
    }

    @Test
    void testBuscarTiposProductosPorEstado() {
        // Datos de prueba
        List<TipoProducto> tiposProductosActivos = List.of(tipoProductoEntity);

        // Configuración del mock
        when(tipoProductoRepository.buscarPorEstado(true)).thenReturn(tiposProductosActivos);

        // Ejecución del método
        List<TipoProductoDTO> resultado = tipoProductoService.buscarTiposProductosPorEstado(true);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(tipoProductoRepository, times(1)).buscarPorEstado(true);
    }

    @Test
    void testEliminarTipoProducto_Exitoso() {
        // Configuración del mock
        when(tipoProductoRepository.eliminar(1)).thenReturn(true);

        // Ejecución del método
        boolean resultado = tipoProductoService.eliminarTipoProducto(1);

        // Verificaciones
        assertTrue(resultado);

        verify(tipoProductoRepository, times(1)).eliminar(1);
    }

    @Test
    void testEliminarTipoProducto_NoExiste() {
        // Configuración del mock
        when(tipoProductoRepository.eliminar(999)).thenReturn(false);

        // Ejecución del método
        boolean resultado = tipoProductoService.eliminarTipoProducto(999);

        // Verificaciones
        assertFalse(resultado);

        verify(tipoProductoRepository, times(1)).eliminar(999);
    }}