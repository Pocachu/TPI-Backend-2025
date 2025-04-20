package test.java.sv.edu.ues.fmocc.tpi135.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.fmocc.tpi135.controller.ProductoController;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.Producto;
import sv.edu.ues.fmocc.tpi135.repository.ProductoRepository;
import test.java.sv.edu.ues.fmocc.tpi135.repository.ProductoRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.ProductoService;
import sv.edu.ues.fmocc.tpi135.service.ProductoServiceImpl;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas de integración para validar la interacción entre controladores,
 * servicios y repositorios en el flujo de operaciones CRUD de productos.
 */
@ExtendWith(MockitoExtension.class)
public class ProductoIntegrationIT {

    @Mock
    private ProductoRepositoryImpl productoRepository;

    private ProductoService productoService;
    private ProductoController productoController;

    private Producto productoEntity;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        // Configurar capa de servicio con el repositorio mock
        productoService = new ProductoServiceImpl();
        // Usamos reflexión para inyectar el repositorio mock en el servicio
        try {
            java.lang.reflect.Field field = ProductoServiceImpl.class.getDeclaredField("productoRepository");
            field.setAccessible(true);
            field.set(productoService, productoRepository);
        } catch (Exception e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Configurar controlador con el servicio
        productoController = new ProductoController();
        try {
            java.lang.reflect.Field field = ProductoController.class.getDeclaredField("productoService");
            field.setAccessible(true);
            field.set(productoController, productoService);
        } catch (Exception e) {
            fail("Error al inyectar servicio: " + e.getMessage());
        }

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
    void testFlujoCrearObtenerProducto() {
        // Configuración de mocks
        when(productoRepository.crear(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setIdProducto(1L); // Simulamos generación de ID
            return p;
        });

        when(productoRepository.encontrarPorId(1L)).thenReturn(Optional.of(productoEntity));

        // Ejecutar flujo: crear producto
        ProductoDTO inputDTO = new ProductoDTO();
        inputDTO.setNombre("Producto Test");
        inputDTO.setActivo(true);
        inputDTO.setObservaciones("Observaciones de prueba");

        Response createResponse = productoController.crearProducto(inputDTO);
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());

        ProductoDTO createdDTO = (ProductoDTO) createResponse.getEntity();
        assertNotNull(createdDTO.getIdProducto());
        assertEquals("Producto Test", createdDTO.getNombre());

        // Ejecutar flujo: obtener producto creado
        Response getResponse = productoController.obtenerProductoPorId(1L);
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

        ProductoDTO retrievedDTO = (ProductoDTO) getResponse.getEntity();
        assertEquals(1L, retrievedDTO.getIdProducto());
        assertEquals("Producto Test", retrievedDTO.getNombre());

        // Verificar interacciones entre capas
        verify(productoRepository).crear(any(Producto.class));
        verify(productoRepository).encontrarPorId(1L);
    }

    @Test
    void testFlujoActualizarProducto() {
        // Configuración de mocks
        when(productoRepository.encontrarPorId(1L)).thenReturn(Optional.of(productoEntity));
        when(productoRepository.actualizar(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            return p; // Devolvemos el mismo objeto actualizado
        });

        // Ejecutar flujo: actualizar producto
        ProductoDTO updateDTO = new ProductoDTO();
        updateDTO.setNombre("Producto Actualizado");
        updateDTO.setActivo(false);
        updateDTO.setObservaciones("Nueva descripción");

        Response updateResponse = productoController.actualizarProducto(1L, updateDTO);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        ProductoDTO updatedDTO = (ProductoDTO) updateResponse.getEntity();
        assertEquals(1L, updatedDTO.getIdProducto());
        assertEquals("Producto Actualizado", updatedDTO.getNombre());
        assertFalse(updatedDTO.getActivo());

        // Verificar interacciones entre capas
        verify(productoRepository).encontrarPorId(1L);
        verify(productoRepository).actualizar(any(Producto.class));
    }

    @Test
    void testFlujoBuscarProductosPorCriterios() {
        // Datos de prueba
        Producto producto1 = new Producto(1L, "Producto Test", true, "Obs 1");
        Producto producto2 = new Producto(2L, "Otro Producto", true, "Obs 2");
        List<Producto> productos = Arrays.asList(producto1, producto2);
        List<Producto> productosActivos = Arrays.asList(producto1, producto2);
        List<Producto> productosFiltrados = List.of(producto1);

        // Configuración de mocks
        when(productoRepository.listarTodos()).thenReturn(productos);
        when(productoRepository.buscarPorEstado(true)).thenReturn(productosActivos);
        when(productoRepository.buscarPorNombre("Test")).thenReturn(productosFiltrados);

        // Ejecutar flujo: listar todos
        Response listResponse = productoController.listarProductos(null, null);
        assertEquals(Response.Status.OK.getStatusCode(), listResponse.getStatus());
        List<ProductoDTO> listedDTOs = (List<ProductoDTO>) listResponse.getEntity();
        assertEquals(2, listedDTOs.size());

        // Ejecutar flujo: buscar por estado
        Response activeResponse = productoController.listarProductos(null, true);
        assertEquals(Response.Status.OK.getStatusCode(), activeResponse.getStatus());
        List<ProductoDTO> activeDTOs = (List<ProductoDTO>) activeResponse.getEntity();
        assertEquals(2, activeDTOs.size());

        // Ejecutar flujo: buscar por nombre
        Response searchResponse = productoController.listarProductos("Test", null);
        assertEquals(Response.Status.OK.getStatusCode(), searchResponse.getStatus());
        List<ProductoDTO> searchDTOs = (List<ProductoDTO>) searchResponse.getEntity();
        assertEquals(1, searchDTOs.size());
        assertEquals("Producto Test", searchDTOs.get(0).getNombre());

        // Verificar interacciones entre capas
        verify(productoRepository).listarTodos();
        verify(productoRepository).buscarPorEstado(true);
        verify(productoRepository).buscarPorNombre("Test");
    }

    @Test
    void testFlujoEliminarProducto() {
        // Configuración de mocks
        when(productoRepository.eliminar(1L)).thenReturn(true);
        when(productoRepository.eliminar(999L)).thenReturn(false);

        // Ejecutar flujo: eliminar producto existente
        Response deleteResponse = productoController.eliminarProducto(1L);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // Ejecutar flujo: intentar eliminar producto inexistente
        Response deleteNotFoundResponse = productoController.eliminarProducto(999L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), deleteNotFoundResponse.getStatus());
        assertEquals("Producto no encontrado con ID: 999", deleteNotFoundResponse.getEntity());

        // Verificar interacciones entre capas
        verify(productoRepository).eliminar(1L);
        verify(productoRepository).eliminar(999L);
    }
}