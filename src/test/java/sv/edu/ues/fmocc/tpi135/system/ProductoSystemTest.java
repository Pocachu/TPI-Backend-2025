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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoSystemTest.class);
    private final String BASE_URL = System.getProperty("api.url", "http://localhost:8080/tipicos-api/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(30))  // Aumentado el timeout
            .build();
    
    // Configuramos ObjectMapper con módulos para manejar fechas correctamente
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    
    private Long productoIdCreado;
    private boolean appReady = false;

    @BeforeAll
    void setUp() {
        // Esperar a que la aplicación esté lista
        try {
            waitForApplicationReady();
            // Limpiar datos previos si es necesario
            cleanupTestData();
        } catch (Exception e) {
            logger.error("Error durante la configuración de las pruebas", e);
            appReady = false;
        }
    }
    
    private void waitForApplicationReady() {
        int maxRetries = 60;  // Aumentado el número de intentos
        int retryCount = 0;
        
        logger.info("Esperando a que la aplicación esté disponible en {}...", BASE_URL);
        
        while (!appReady && retryCount < maxRetries) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(BASE_URL + "/productos"))
                        .timeout(Duration.ofSeconds(10))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    appReady = true;
                    logger.info("Aplicación disponible después de {} intentos!", retryCount + 1);
                }
            } catch (Exception e) {
                logger.info("La aplicación aún no está disponible. Reintentando... ({}/{})", retryCount + 1, maxRetries);
                logger.debug("Detalle del error: {}", e.getMessage());
            }
            
            if (!appReady) {
                retryCount++;
                try {
                    TimeUnit.SECONDS.sleep(5);  // Aumentado el tiempo entre intentos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        if (!appReady) {
            logger.error("La aplicación no está disponible después de {} intentos. Saltando pruebas.", maxRetries);
        }
    }
    
    private void cleanupTestData() {
        logger.info("Limpiando datos de pruebas anteriores...");
        
        try {
            // Buscar productos de prueba previos para eliminarlos
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/productos?nombre=Prueba"))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                List<ProductoDTO> productos = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<ProductoDTO>>() {});
                
                for (ProductoDTO producto : productos) {
                    try {
                        HttpRequest deleteRequest = HttpRequest.newBuilder()
                                .DELETE()
                                .uri(URI.create(BASE_URL + "/productos/" + producto.getIdProducto()))
                                .build();
                        
                        httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                        logger.info("Eliminado producto de prueba con ID: {}", producto.getIdProducto());
                    } catch (Exception e) {
                        logger.warn("No se pudo eliminar el producto {}: {}", producto.getIdProducto(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error durante la limpieza de datos: {}", e.getMessage());
        }
    }

    @Test
    void testCicloCompletoCRUD() throws Exception {
        // Verificar que la aplicación esté lista antes de ejecutar las pruebas
        Assumptions.assumeTrue(appReady, "Aplicación no disponible, saltando pruebas");
        
        try {
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
        } catch (Exception e) {
            logger.error("Error durante la ejecución de las pruebas", e);
            throw e;
        }
    }
    
    void testCrearProducto() throws Exception {
        logger.info("Iniciando prueba de creación de producto");
        
        // Preparar datos para la creación con un nombre único para evitar conflictos
        ProductoDTO nuevoProducto = new ProductoDTO();
        String nombreUnico = "Pupusa de Queso Test " + System.currentTimeMillis();
        nuevoProducto.setNombre(nombreUnico);
        nuevoProducto.setActivo(true);
        nuevoProducto.setObservaciones("Producto para prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(nuevoProducto);
        logger.debug("Cuerpo de la solicitud: {}", requestBody);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/productos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta del servidor: {} - {}", response.statusCode(), response.body());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "El producto debería crearse correctamente");
        
        // Extraer el producto creado
        ProductoDTO productoCreado = objectMapper.readValue(response.body(), ProductoDTO.class);
        
        assertNotNull(productoCreado.getIdProducto(), "El producto debería tener un ID asignado");
        assertEquals(nombreUnico, productoCreado.getNombre(), "El nombre del producto debería coincidir");
        assertTrue(productoCreado.getActivo(), "El producto debería estar activo");
        
        // Guardar el ID para pruebas posteriores
        productoIdCreado = productoCreado.getIdProducto();
        
        logger.info("Producto creado con ID: {}", productoIdCreado);
    }
    
    void testObtenerProducto() throws Exception {
        logger.info("Iniciando prueba de obtención de producto");
        
        // Esperar un momento para asegurar que la operación anterior se haya completado
        TimeUnit.SECONDS.sleep(1);
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta del servidor: {} - {}", response.statusCode(), response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El producto debería encontrarse");
        
        // Extraer el producto
        ProductoDTO producto = objectMapper.readValue(response.body(), ProductoDTO.class);
        
        assertEquals(productoIdCreado, producto.getIdProducto(), "El ID del producto debería coincidir");
        assertNotNull(producto.getNombre(), "El producto debería tener un nombre");
        assertTrue(producto.getActivo(), "El producto debería estar activo");
        
        logger.info("Producto obtenido correctamente: {}", producto.getNombre());
    }
    
    void testActualizarProducto() throws Exception {
        logger.info("Iniciando prueba de actualización de producto");
        
        // Preparar datos para la actualización
        ProductoDTO productoActualizado = new ProductoDTO();
        String nombreActualizado = "Pupusa de Queso con Loroco Test " + System.currentTimeMillis();
        productoActualizado.setNombre(nombreActualizado);
        productoActualizado.setActivo(true);
        productoActualizado.setObservaciones("Producto actualizado en prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(productoActualizado);
        logger.debug("Cuerpo de la solicitud: {}", requestBody);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta del servidor: {} - {}", response.statusCode(), response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El producto debería actualizarse correctamente");
        
        // Extraer el producto actualizado
        ProductoDTO producto = objectMapper.readValue(response.body(), ProductoDTO.class);
        
        assertEquals(productoIdCreado, producto.getIdProducto(), "El ID del producto debería mantenerse");
        assertEquals(nombreActualizado, producto.getNombre(), "El nombre del producto debería actualizarse");
        
        logger.info("Producto actualizado correctamente a: {}", producto.getNombre());
    }
    
    void testListarProductos() throws Exception {
        logger.info("Iniciando prueba de listado de productos");
        
        // Esperar un momento para asegurar que las operaciones anteriores se hayan completado
        TimeUnit.SECONDS.sleep(1);
        
        // 1. Listar todos los productos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos"))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta de listado: {} - {}", responseTodos.statusCode(), responseTodos.body());
        
        assertEquals(200, responseTodos.statusCode(), "La lista de productos debería obtenerse correctamente");
        
        List<ProductoDTO> productos = objectMapper.readValue(
                responseTodos.body(), 
                new TypeReference<List<ProductoDTO>>() {});
        
        // No siempre asumimos que hay productos, pero debería haber al menos el que creamos
        assertFalse(productos.isEmpty(), "Debería haber al menos un producto en la lista");
        logger.info("Total de productos listados: {}", productos.size());
        
        // 2. Buscar por nombre (usando parte del nombre que sabemos existe)
        HttpRequest requestNombre = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos?nombre=Test"))
                .build();
        
        HttpResponse<String> responseNombre = httpClient.send(requestNombre, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta de búsqueda por nombre: {} - {}", responseNombre.statusCode(), responseNombre.body());
        
        assertEquals(200, responseNombre.statusCode(), "La búsqueda por nombre debería funcionar");
        
        List<ProductoDTO> productosPorNombre = objectMapper.readValue(
                responseNombre.body(), 
                new TypeReference<List<ProductoDTO>>() {});
        
        // Verificar que al menos se encuentra el producto que acabamos de crear
        boolean encontrado = productosPorNombre.stream()
                .anyMatch(p -> p.getIdProducto().equals(productoIdCreado));
        
        assertTrue(encontrado, "Debería encontrarse el producto recién creado por su nombre");
        logger.info("Productos encontrados por nombre: {}", productosPorNombre.size());
        
        // 3. Buscar por estado activo
        HttpRequest requestActivos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos?activo=true"))
                .build();
        
        HttpResponse<String> responseActivos = httpClient.send(requestActivos, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta de búsqueda por estado: {} - {}", responseActivos.statusCode(), responseActivos.body());
        
        assertEquals(200, responseActivos.statusCode(), "La búsqueda por estado debería funcionar");
        
        List<ProductoDTO> productosActivos = objectMapper.readValue(
                responseActivos.body(), 
                new TypeReference<List<ProductoDTO>>() {});
        
        // Verificar que al menos se encuentra el producto que acabamos de crear
        encontrado = productosActivos.stream()
                .anyMatch(p -> p.getIdProducto().equals(productoIdCreado));
        
        assertTrue(encontrado, "Debería encontrarse el producto recién creado por su estado activo");
        logger.info("Productos activos: {}", productosActivos.size());
    }
    
    void testEliminarProducto() throws Exception {
        logger.info("Iniciando prueba de eliminación de producto");
        
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta del servidor: {} - {}", response.statusCode(), response.body());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "El producto debería eliminarse correctamente");
        
        logger.info("Producto eliminado correctamente");
    }
    
    void testProductoNoExisteDespuesDeEliminar() throws Exception {
        logger.info("Iniciando prueba de verificación post-eliminación");
        
        // Esperar un momento para asegurar que la operación anterior se haya completado
        TimeUnit.SECONDS.sleep(1);
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/productos/" + productoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug("Respuesta del servidor: {} - {}", response.statusCode(), response.body());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El producto no debería existir después de ser eliminado");
        
        logger.info("Verificado: el producto ya no existe después de eliminar");
    }
}