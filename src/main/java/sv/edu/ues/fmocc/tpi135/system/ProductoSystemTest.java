package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de sistema que validan el funcionamiento completo de la API
 * en un entorno real o similar a producción.
 * 
 * Nota: Estas pruebas requieren que la aplicación esté desplegada en un servidor.
 * Para la ejecución en un pipeline, se debe configurar un contenedor Docker con
 * la aplicación y la base de datos.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class ProductoSystemTest {

    private final String BASE_URL = System.getProperty("api.url", "http://localhost:9080/tipicos-tpi135/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private Long productoIdCreado;

    @BeforeAll
    void setUp() throws Exception {
        // Esperar a que la aplicación esté lista
        waitForApplicationReady();
        
        // Limpiar datos previos si es necesario
        cleanupTestData();
    }
    
    private void waitForApplicationReady() throws Exception {
        int maxRetries = 30;
        int retryCount = 0;
        boolean isReady = false;
        
        System.out.println("Esperando a que la aplicación esté disponible...");
        
        while (!isReady && retryCount < maxRetries) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(BASE_URL + "/productos"))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    isReady = true;
                    System.out.println("Aplicación disponible!");
                }
            } catch (Exception e) {
                System.out.println("La aplicación aún no está disponible. Reintentando...");
            }
            
            if (!isReady) {
                retryCount++;
                TimeUnit.SECONDS.sleep(2);
            }
        }
        
        if (!isReady) {
            fail("La aplicación no está disponible después de " + maxRetries + " intentos");
        }
    }
    
    private void cleanupTestData() {
        // Aquí se podría implementar la limpieza de datos de prueba previos
        // Por simplicidad, no implementamos esto para este ejemplo
    }

    @Test
    void testCicloCompletoCRUD() throws Exception {
        // 1. Crear un producto
        testCrearProducto();
        
        // 2. Obtener el producto creado
        testObtenerProducto();
        
        // 3. Actualizar el producto
        testActualizarProducto();
        
        // 4. Listar productos con diferentes criterios
        testListarProductos();
        
        // 5. Eliminar el producto
        testEliminarProducto();
        
        // 6. Verificar que ya no existe
        testProductoNoExisteDespuesDeEliminar();
    }
    
    void testCrearProducto() throws Exception {
        // Preparar datos para la creación
        ProductoDTO nuevoProducto = new ProductoDTO();
        nuevoProducto.setNombre("Pupusa de Queso");
        nuevoProducto.setActivo(true);
        nuevoProducto.setObservaciones("Producto para prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(nuevoProducto);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/productos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "El producto debería crearse correctamente");
        
        // Extraer el producto creado
        ProductoDTO productoCreado = objectMapper.readValue(response.body(), ProductoDTO.class);
        
        assertNotNull(productoCreado.getIdProducto(), "El producto debería tener un ID asignado");
        assertEquals("Pupusa de Queso", productoCreado.getNombre(), "El nombre del producto debería coincidir");
        assertTrue(productoCreado.getActivo(), "El producto debería estar activo");
        
        // Guardar el ID para pruebas posteriores
        productoIdCreado = productoCreado.getIdProducto();
        
        System.out.println("Producto creado con ID: " + productoIdCreado);
    }
    
    void testObtenerProducto() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El producto debería encontrarse");
        
        // Extraer el producto
        ProductoDTO producto = objectMapper.readValue(response.body(), ProductoDTO.class);
        
        assertEquals(productoIdCreado, producto.getIdProducto(), "El ID del producto debería coincidir");
        assertEquals("Pupusa de Queso", producto.getNombre(), "El nombre del producto debería coincidir");
        assertTrue(producto.getActivo(), "El producto debería estar activo");
        
        System.out.println("Producto obtenido correctamente: " + producto.getNombre());
    }
    
    void testActualizarProducto() throws Exception {
        // Preparar datos para la actualización
        ProductoDTO productoActualizado = new ProductoDTO();
        productoActualizado.setNombre("Pupusa de Queso con Loroco");
        productoActualizado.setActivo(true);
        productoActualizado.setObservaciones("Producto actualizado en prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(productoActualizado);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El producto debería actualizarse correctamente");
        
        // Extraer el producto actualizado
        ProductoDTO producto = objectMapper.readValue(response.body(), ProductoDTO.class);
        
        assertEquals(productoIdCreado, producto.getIdProducto(), "El ID del producto debería mantenerse");
        assertEquals("Pupusa de Queso con Loroco", producto.getNombre(), "El nombre del producto debería actualizarse");
        
        System.out.println("Producto actualizado correctamente a: " + producto.getNombre());
    }
    
    void testListarProductos() throws Exception {
        // 1. Listar todos los productos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos"))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseTodos.statusCode(), "La lista de productos debería obtenerse correctamente");
        
        List<ProductoDTO> productos = objectMapper.readValue(
                responseTodos.body(), 
                new TypeReference<List<ProductoDTO>>() {});
        
        assertFalse(productos.isEmpty(), "Debería haber al menos un producto en la lista");
        System.out.println("Total de productos listados: " + productos.size());
        
        // 2. Buscar por nombre
        HttpRequest requestNombre = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos?nombre=Loroco"))
                .build();
        
        HttpResponse<String> responseNombre = httpClient.send(requestNombre, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseNombre.statusCode(), "La búsqueda por nombre debería funcionar");
        
        List<ProductoDTO> productosPorNombre = objectMapper.readValue(
                responseNombre.body(), 
                new TypeReference<List<ProductoDTO>>() {});
        
        assertFalse(productosPorNombre.isEmpty(), "Debería encontrarse al menos un producto con el nombre buscado");
        System.out.println("Productos encontrados por nombre: " + productosPorNombre.size());
        
        // 3. Buscar por estado activo
        HttpRequest requestActivos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos?activo=true"))
                .build();
        
        HttpResponse<String> responseActivos = httpClient.send(requestActivos, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseActivos.statusCode(), "La búsqueda por estado debería funcionar");
        
        List<ProductoDTO> productosActivos = objectMapper.readValue(
                responseActivos.body(), 
                new TypeReference<List<ProductoDTO>>() {});
        
        assertFalse(productosActivos.isEmpty(), "Debería haber al menos un producto activo");
        System.out.println("Productos activos: " + productosActivos.size());
    }
    
    void testEliminarProducto() throws Exception {
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "El producto debería eliminarse correctamente");
        
        System.out.println("Producto eliminado correctamente");
    }
    
    void testProductoNoExisteDespuesDeEliminar() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El producto no debería existir después de ser eliminado");
        
        System.out.println("Verificado: el producto ya no existe después de eliminar");
    }}