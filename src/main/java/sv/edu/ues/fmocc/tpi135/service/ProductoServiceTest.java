package sv.edu.ues.fmocc.tpi135.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.Producto;
import sv.edu.ues.fmocc.tpi135.repository.ProductoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoEntity;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        // Datos de prueba para entidad
        productoEntity = new Producto();
        productoEntity.setIdProducto(1L);
        productoEntity.setNombre("Producto Test");
        productoEntity.setActivo(true);
        productoEntity.setObservaciones("Observaciones de prueba");

        // Datos de prueba para DTO
        productoDTO = new ProductoDTO();
        productoDTO.setIdProducto(1L);
        productoDTO.setNombre("Producto Test");
        productoDTO.setActivo(true);
        productoDTO.setObservaciones("Observaciones de prueba");
    }

    @Test
    void testCrearProducto_Exitoso() {
        // Configuración del mock
        when(productoRepository.crear(any(Producto.class))).thenReturn(productoEntity);

        // Ejecución del método
        ProductoDTO resultado = productoService.crearProducto(productoDTO);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(productoDTO.getIdProducto(), resultado.getIdProducto());
        assertEquals(productoDTO.getNombre(), resultado.getNombre());
        assertEquals(productoDTO.getActivo(), resultado.getActivo());
        assertEquals(productoDTO.getObservaciones(), resultado.getObservaciones());

        verify(productoRepository, times(1)).crear(any(Producto.class));
    }

    @Test
    void testCrearProducto_NombreVacio() {
        // Datos de prueba
        ProductoDTO dtoInvalido = new ProductoDTO();
        dtoInvalido.setNombre("");

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(dtoInvalido));

        verify(productoRepository, never()).crear(any());
    }

    @Test
    void testActualizarProducto_Exitoso() {
        // Configuración del mock
        when(productoRepository.encontrarPorId(1L)).thenReturn(Optional.of(productoEntity));
        when(productoRepository.actualizar(any(Producto.class))).thenReturn(productoEntity);

        // Datos de prueba
        ProductoDTO dtoActualizado = new ProductoDTO();
        dtoActualizado.setNombre("Producto Actualizado");
        dtoActualizado.setActivo(false);
        dtoActualizado.setObservaciones("Nuevas observaciones");

        // Ejecución del método
        ProductoDTO resultado = productoService.actualizarProducto(1L, dtoActualizado);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdProducto());
        assertEquals("Producto Actualizado", resultado.getNombre());
        assertEquals(false, resultado.getActivo());
        assertEquals("Nuevas observaciones", resultado.getObservaciones());

        verify(productoRepository, times(1)).encontrarPorId(1L);
        verify(productoRepository, times(1)).actualizar(any(Producto.class));
    }

    @Test
    void testActualizarProducto_NoExiste() {
        // Configuración del mock
        when(productoRepository.encontrarPorId(1L)).thenReturn(Optional.empty());

        // Verificaciones
        assertThrows(IllegalArgumentException.class, () -> productoService.actualizarProducto(1L, productoDTO));

        verify(productoRepository, times(1)).encontrarPorId(1L);
        verify(productoRepository, never()).actualizar(any());
    }

    @Test
    void testObtenerProductoPorId_Existente() {
        // Configuración del mock
        when(productoRepository.encontrarPorId(1L)).thenReturn(Optional.of(productoEntity));

        // Ejecución del método
        Optional<ProductoDTO> resultado = productoService.obtenerProductoPorId(1L);

        // Verificaciones
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdProducto());
        assertEquals("Producto Test", resultado.get().getNombre());

        verify(productoRepository, times(1)).encontrarPorId(1L);
    }

    @Test
    void testObtenerProductoPorId_NoExistente() {
        // Configuración del mock
        when(productoRepository.encontrarPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Optional<ProductoDTO> resultado = productoService.obtenerProductoPorId(999L);

        // Verificaciones
        assertFalse(resultado.isPresent());

        verify(productoRepository, times(1)).encontrarPorId(999L);
    }

    @Test
    void testListarProductos() {
        // Datos de prueba
        Producto producto2 = new Producto(2L, "Producto 2", true, "Obs 2");
        List<Producto> productos = Arrays.asList(productoEntity, producto2);

        // Configuración del mock
        when(productoRepository.listarTodos()).thenReturn(productos);

        // Ejecución del método
        List<ProductoDTO> resultado = productoService.listarProductos();

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdProducto());
        assertEquals(2L, resultado.get(1).getIdProducto());

        verify(productoRepository, times(1)).listarTodos();
    }

    @Test
    void testBuscarProductosPorNombre() {
        // Datos de prueba
        List<Producto> productos = List.of(productoEntity);

        // Configuración del mock
        when(productoRepository.buscarPorNombre("Test")).thenReturn(productos);

        // Ejecución del método
        List<ProductoDTO> resultado = productoService.buscarProductosPorNombre("Test");

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Producto Test", resultado.get(0).getNombre());

        verify(productoRepository, times(1)).buscarPorNombre("Test");
    }

    @Test
    void testBuscarProductosPorEstado() {
        // Datos de prueba
        List<Producto> productosActivos = List.of(productoEntity);

        // Configuración del mock
        when(productoRepository.buscarPorEstado(true)).thenReturn(productosActivos);

        // Ejecución del método
        List<ProductoDTO> resultado = productoService.buscarProductosPorEstado(true);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(productoRepository, times(1)).buscarPorEstado(true);
    }

    @Test
    void testEliminarProducto_Exitoso() {
        // Configuración del mock
        when(productoRepository.eliminar(1L)).thenReturn(true);

        // Ejecución del método
        boolean resultado = productoService.eliminarProducto(1L);

        // Verificaciones
        assertTrue(resultado);

        verify(productoRepository, times(1)).eliminar(1L);
    }

    @Test
    void testEliminarProducto_NoExiste() {
        // Configuración del mock
        when(productoRepository.eliminar(999L)).thenReturn(false);

        // Ejecución del método
        boolean resultado = productoService.eliminarProducto(999L);

        // Verificaciones
        assertFalse(resultado);

        verify(productoRepository, times(1)).eliminar(999L);
    }
}