package sv.edu.ues.fmocc.tpi135.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.service.ProductoService;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        // Datos de prueba
        productoDTO = new ProductoDTO();
        productoDTO.setIdProducto(1L);
        productoDTO.setNombre("Producto Test");
        productoDTO.setActivo(true);
        productoDTO.setObservaciones("Observaciones de prueba");
    }

    @Test
    void testCrearProducto_Exitoso() {
        // Configuración del mock
        when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoDTO);

        // Ejecución del método
        Response response = productoController.crearProducto(productoDTO);

        // Verificaciones
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(productoDTO, response.getEntity());

        verify(productoService, times(1)).crearProducto(any(ProductoDTO.class));
    }

    @Test
    void testCrearProducto_ErrorValidacion() {
        // Configuración del mock
        when(productoService.crearProducto(any(ProductoDTO.class)))
                .thenThrow(new IllegalArgumentException("El nombre del producto es obligatorio"));

        // Ejecución del método
        Response response = productoController.crearProducto(productoDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("El nombre del producto es obligatorio", response.getEntity());

        verify(productoService, times(1)).crearProducto(any(ProductoDTO.class));
    }

    @Test
    void testActualizarProducto_Exitoso() {
        // Configuración del mock
        when(productoService.actualizarProducto(eq(1L), any(ProductoDTO.class))).thenReturn(productoDTO);

        // Ejecución del método
        Response response = productoController.actualizarProducto(1L, productoDTO);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(productoDTO, response.getEntity());

        verify(productoService, times(1)).actualizarProducto(eq(1L), any(ProductoDTO.class));
    }

    @Test
    void testActualizarProducto_NoExiste() {
        // Configuración del mock
        when(productoService.actualizarProducto(eq(999L), any(ProductoDTO.class)))
                .thenThrow(new IllegalArgumentException("No existe un producto con el ID 999"));

        // Ejecución del método
        Response response = productoController.actualizarProducto(999L, productoDTO);

        // Verificaciones
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("No existe un producto con el ID 999", response.getEntity());

        verify(productoService, times(1)).actualizarProducto(eq(999L), any(ProductoDTO.class));
    }

    @Test
    void testObtenerProductoPorId_Existente() {
        // Configuración del mock
        when(productoService.obtenerProductoPorId(1L)).thenReturn(Optional.of(productoDTO));

        // Ejecución del método
        Response response = productoController.obtenerProductoPorId(1L);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(productoDTO, response.getEntity());

        verify(productoService, times(1)).obtenerProductoPorId(1L);
    }

    @Test
    void testObtenerProductoPorId_NoExistente() {
        // Configuración del mock
        when(productoService.obtenerProductoPorId(999L)).thenReturn(Optional.empty());

        // Ejecución del método
        Response response = productoController.obtenerProductoPorId(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Producto no encontrado con ID: 999", response.getEntity());

        verify(productoService, times(1)).obtenerProductoPorId(999L);
    }

    @Test
    void testListarProductos_TodosLosProductos() {
        // Datos de prueba
        ProductoDTO producto2 = new ProductoDTO(2L, "Producto 2", true, "Obs 2");
        List<ProductoDTO> productos = Arrays.asList(productoDTO, producto2);

        // Configuración del mock
        when(productoService.listarProductos()).thenReturn(productos);

        // Ejecución del método
        Response response = productoController.listarProductos(null, null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<ProductoDTO> resultado = (List<ProductoDTO>) response.getEntity();
        assertEquals(2, resultado.size());

        verify(productoService, times(1)).listarProductos();
        verify(productoService, never()).buscarProductosPorNombre(any());
        verify(productoService, never()).buscarProductosPorEstado(any());
    }

    @Test
    void testListarProductos_FiltrarPorNombre() {
        // Datos de prueba
        List<ProductoDTO> productos = List.of(productoDTO);

        // Configuración del mock
        when(productoService.buscarProductosPorNombre("Test")).thenReturn(productos);

        // Ejecución del método
        Response response = productoController.listarProductos("Test", null);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<ProductoDTO> resultado = (List<ProductoDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("Producto Test", resultado.get(0).getNombre());

        verify(productoService, times(1)).buscarProductosPorNombre("Test");
        verify(productoService, never()).listarProductos();
        verify(productoService, never()).buscarProductosPorEstado(any());
    }

    @Test
    void testListarProductos_FiltrarPorEstado() {
        // Datos de prueba
        List<ProductoDTO> productos = List.of(productoDTO);

        // Configuración del mock
        when(productoService.buscarProductosPorEstado(true)).thenReturn(productos);

        // Ejecución del método
        Response response = productoController.listarProductos(null, true);

        // Verificaciones
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        List<ProductoDTO> resultado = (List<ProductoDTO>) response.getEntity();
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(productoService, times(1)).buscarProductosPorEstado(true);
        verify(productoService, never()).listarProductos();
        verify(productoService, never()).buscarProductosPorNombre(any());
    }

    @Test
    void testEliminarProducto_Exitoso() {
        // Configuración del mock
        when(productoService.eliminarProducto(1L)).thenReturn(true);

        // Ejecución del método
        Response response = productoController.eliminarProducto(1L);

        // Verificaciones
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());

        verify(productoService, times(1)).eliminarProducto(1L);
    }

    @Test
    void testEliminarProducto_NoExiste() {
        // Configuración del mock
        when(productoService.eliminarProducto(999L)).thenReturn(false);

        // Ejecución del método
        Response response = productoController.eliminarProducto(999L);

        // Verificaciones
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Producto no encontrado con ID: 999", response.getEntity());

        verify(productoService, times(1)).eliminarProducto(999L);
    }
}