package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;

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
 * para la entidad TipoProducto en un entorno real o similar a producción.
 * 
 * Nota: Estas pruebas requieren que la aplicación esté desplegada en un servidor.
 * Para la ejecución en un pipeline, se debe configurar un contenedor Docker con
 * la aplicación y la base de datos.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class TipoProductoSystemTest {

    private final String BASE_URL = System.getProperty("api.url", "http://localhost:9080/tipicos-tpi135/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private Integer tipoProductoIdCreado;

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
                        .uri(URI.create(BASE_URL + "/tipos-productos"))
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
        // 1. Crear un tipo de producto
        testCrearTipoProducto();
        
        // 2. Obtener el tipo de producto creado
        testObtenerTipoProducto();
        
        // 3. Actualizar el tipo de producto
        testActualizarTipoProducto();
        
        // 4. Listar tipos de productos con diferentes criterios
        testListarTiposProductos();
        
        // 5. Eliminar el tipo de producto
        testEliminarTipoProducto();
        
        // 6. Verificar que ya no existe
        testTipoProductoNoExisteDespuesDeEliminar();
    }
    
    void testCrearTipoProducto() throws Exception {
        // Preparar datos para la creación
        TipoProductoDTO nuevoTipoProducto = new TipoProductoDTO();
        nuevoTipoProducto.setNombre("Postres");
        nuevoTipoProducto.setActivo(true);
        nuevoTipoProducto.setObservaciones("Tipo de producto para prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(nuevoTipoProducto);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/tipos-productos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "El tipo de producto debería crearse correctamente");
        
        // Extraer el tipo de producto creado
        TipoProductoDTO tipoProductoCreado = objectMapper.readValue(response.body(), TipoProductoDTO.class);
        
        assertNotNull(tipoProductoCreado.getIdTipoProducto(), "El tipo de producto debería tener un ID asignado");
        assertEquals("Postres", tipoProductoCreado.getNombre(), "El nombre del tipo de producto debería coincidir");
        assertTrue(tipoProductoCreado.getActivo(), "El tipo de producto debería estar activo");
        
        // Guardar el ID para pruebas posteriores
        tipoProductoIdCreado = tipoProductoCreado.getIdTipoProducto();
        
        System.out.println("Tipo de producto creado con ID: " + tipoProductoIdCreado);
    }
    
    void testObtenerTipoProducto() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El tipo de producto debería encontrarse");
        
        // Extraer el tipo de producto
        TipoProductoDTO tipoProducto = objectMapper.readValue(response.body(), TipoProductoDTO.class);
        
        assertEquals(tipoProductoIdCreado, tipoProducto.getIdTipoProducto(), "El ID del tipo de producto debería coincidir");
        assertEquals("Postres", tipoProducto.getNombre(), "El nombre del tipo de producto debería coincidir");
        assertTrue(tipoProducto.getActivo(), "El tipo de producto debería estar activo");
        
        System.out.println("Tipo de producto obtenido correctamente: " + tipoProducto.getNombre());
    }
    
    void testActualizarTipoProducto() throws Exception {
        // Preparar datos para la actualización
        TipoProductoDTO tipoProductoActualizado = new TipoProductoDTO();
        tipoProductoActualizado.setNombre("Postres y Dulces");
        tipoProductoActualizado.setActivo(true);
        tipoProductoActualizado.setObservaciones("Tipo de producto actualizado en prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(tipoProductoActualizado);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El tipo de producto debería actualizarse correctamente");
        
        // Extraer el tipo de producto actualizado
        TipoProductoDTO tipoProducto = objectMapper.readValue(response.body(), TipoProductoDTO.class);
        
        assertEquals(tipoProductoIdCreado, tipoProducto.getIdTipoProducto(), "El ID del tipo de producto debería mantenerse");
        assertEquals("Postres y Dulces", tipoProducto.getNombre(), "El nombre del tipo de producto debería actualizarse");
        
        System.out.println("Tipo de producto actualizado correctamente a: " + tipoProducto.getNombre());
    }
    
    void testListarTiposProductos() throws Exception {
        // 1. Listar todos los tipos de productos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos"))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseTodos.statusCode(), "La lista de tipos de productos debería obtenerse correctamente");
        
        List<TipoProductoDTO> tiposProductos = objectMapper.readValue(
                responseTodos.body(), 
                new TypeReference<List<TipoProductoDTO>>() {});
        
        assertFalse(tiposProductos.isEmpty(), "Debería haber al menos un tipo de producto en la lista");
        System.out.println("Total de tipos de productos listados: " + tiposProductos.size());
        
        // 2. Buscar por nombre
        HttpRequest requestNombre = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos?nombre=Postres"))
                .build();
        
        HttpResponse<String> responseNombre = httpClient.send(requestNombre, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseNombre.statusCode(), "La búsqueda por nombre debería funcionar");
        
        List<TipoProductoDTO> tiposProductosPorNombre = objectMapper.readValue(
                responseNombre.body(), 
                new TypeReference<List<TipoProductoDTO>>() {});
        
        assertFalse(tiposProductosPorNombre.isEmpty(), "Debería encontrarse al menos un tipo de producto con el nombre buscado");
        System.out.println("Tipos de productos encontrados por nombre: " + tiposProductosPorNombre.size());
        
        // 3. Buscar por estado activo
        HttpRequest requestActivos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos?activo=true"))
                .build();
        
        HttpResponse<String> responseActivos = httpClient.send(requestActivos, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseActivos.statusCode(), "La búsqueda por estado debería funcionar");
        
        List<TipoProductoDTO> tiposProductosActivos = objectMapper.readValue(
                responseActivos.body(), 
                new TypeReference<List<TipoProductoDTO>>() {});
        
        assertFalse(tiposProductosActivos.isEmpty(), "Debería haber al menos un tipo de producto activo");
        System.out.println("Tipos de productos activos: " + tiposProductosActivos.size());
    }
    
    void testEliminarTipoProducto() throws Exception {
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "El tipo de producto debería eliminarse correctamente");
        
        System.out.println("Tipo de producto eliminado correctamente");
    }
    
    void testTipoProductoNoExisteDespuesDeEliminar() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El tipo de producto no debería existir después de ser eliminado");
        
        System.out.println("Verificado: el tipo de producto ya no existe después de eliminar");
    }
}