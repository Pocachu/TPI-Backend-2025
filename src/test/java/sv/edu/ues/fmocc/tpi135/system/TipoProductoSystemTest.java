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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

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

    // Obtenemos la URL base de una propiedad del sistema o usamos un valor por defecto
    private final String BASE_URL = System.getProperty("api.url", "http://localhost:8080/tipicos-api/api");
    
    // Configuramos un cliente HTTP con timeout más largo para entornos de CI/CD
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    // Configuración mejorada del ObjectMapper para manejar las fechas y ser más flexible
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();
    
    private Integer tipoProductoIdCreado;

    @BeforeAll
    void setUp() throws Exception {
        System.out.println("Iniciando pruebas de sistema para TipoProducto en URL: " + BASE_URL);
        
        // Esperar a que la aplicación esté lista - versión mejorada con más tiempo
        waitForApplicationReady();
        
        // Intentar limpiar datos previos
        try {
            cleanupTestData();
        } catch (Exception e) {
            System.out.println("Advertencia: No se pudieron limpiar datos previos. Continuando con las pruebas.");
            e.printStackTrace();
        }
    }
    
    private void waitForApplicationReady() throws Exception {
        // Aumentamos los reintentos para dar más tiempo a los contenedores en CI/CD
        int maxRetries = 60; // 2 minutos con 2 segundos entre intentos
        int retryCount = 0;
        boolean isReady = false;
        
        System.out.println("Esperando a que la aplicación esté disponible...");
        
        while (!isReady && retryCount < maxRetries) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(BASE_URL + "/tipos-productos"))
                        .timeout(Duration.ofSeconds(10))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    isReady = true;
                    System.out.println("¡Aplicación disponible! Código de estado: " + response.statusCode());
                    System.out.println("Respuesta: " + response.body().substring(0, Math.min(100, response.body().length())) + "...");
                } else {
                    System.out.println("La aplicación respondió con estado: " + response.statusCode());
                }
            } catch (Exception e) {
                System.out.println("Intento " + (retryCount+1) + "/" + maxRetries + 
                                  ": La aplicación aún no está disponible. Error: " + e.getMessage());
            }
            
            if (!isReady) {
                retryCount++;
                // Agregamos un retraso progresivo para dar más tiempo entre intentos
                TimeUnit.SECONDS.sleep(2 + (retryCount / 10));
            }
        }
        
        if (!isReady) {
            System.err.println("ERROR: La aplicación no está disponible después de " + maxRetries + " intentos");
            System.err.println("URL utilizada: " + BASE_URL + "/tipos-productos");
            throw new RuntimeException("Aplicación no disponible para pruebas");
        }
    }
    
    private void cleanupTestData() {
        // Intentamos eliminar tipos de producto con nombres de prueba que pudieron quedar de ejecuciones anteriores
        try {
            // Primero obtenemos la lista de tipos existentes
            HttpRequest requestList = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/tipos-productos"))
                    .build();
            
            HttpResponse<String> responseList = httpClient.send(requestList, HttpResponse.BodyHandlers.ofString());
            
            if (responseList.statusCode() == 200) {
                List<TipoProductoDTO> tiposProductos = objectMapper.readValue(
                        responseList.body(), 
                        new TypeReference<List<TipoProductoDTO>>() {});
                
                // Eliminamos los que contienen "Postres" o "prueba" en su nombre
                for (TipoProductoDTO tipo : tiposProductos) {
                    if (tipo.getNombre() != null && 
                       (tipo.getNombre().contains("Postres") || 
                        tipo.getNombre().toLowerCase().contains("prueba"))) {
                        
                        HttpRequest deleteRequest = HttpRequest.newBuilder()
                                .DELETE()
                                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipo.getIdTipoProducto()))
                                .build();
                        
                        httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                        System.out.println("Limpieza: Eliminado tipo de producto previo: " + tipo.getNombre() + " (ID: " + tipo.getIdTipoProducto() + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error durante la limpieza de datos: " + e.getMessage());
            // Continuamos con las pruebas a pesar del error
        }
    }

    @Test
    void testCicloCompletoCRUD() throws Exception {
        try {
            System.out.println("\n===== INICIANDO PRUEBA COMPLETA CRUD =====");
            
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
            
            System.out.println("\n===== PRUEBA COMPLETA CRUD FINALIZADA CON ÉXITO =====");
        } catch (Exception e) {
            System.err.println("\n===== ERROR EN PRUEBA CRUD =====");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e; // Relanzamos la excepción para que la prueba falle
        }
    }
    
    void testCrearTipoProducto() throws Exception {
        System.out.println("\n----- Prueba: Crear tipo de producto -----");
        
        // Preparar datos para la creación
        TipoProductoDTO nuevoTipoProducto = new TipoProductoDTO();
        nuevoTipoProducto.setNombre("Postres-Test-" + System.currentTimeMillis()); // Nombre único
        nuevoTipoProducto.setActivo(true);
        nuevoTipoProducto.setObservaciones("Tipo de producto para prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(nuevoTipoProducto);
        System.out.println("Request body: " + requestBody);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/tipos-productos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Mostrar detalles de la respuesta para depuración
        System.out.println("Código de respuesta: " + response.statusCode());
        System.out.println("Cuerpo de respuesta: " + response.body());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "El tipo de producto debería crearse correctamente");
        
        // Extraer el tipo de producto creado
        TipoProductoDTO tipoProductoCreado = objectMapper.readValue(response.body(), TipoProductoDTO.class);
        
        assertNotNull(tipoProductoCreado.getIdTipoProducto(), "El tipo de producto debería tener un ID asignado");
        assertEquals(nuevoTipoProducto.getNombre(), tipoProductoCreado.getNombre(), "El nombre del tipo de producto debería coincidir");
        assertTrue(tipoProductoCreado.getActivo(), "El tipo de producto debería estar activo");
        
        // Guardar el ID para pruebas posteriores
        tipoProductoIdCreado = tipoProductoCreado.getIdTipoProducto();
        
        System.out.println("Tipo de producto creado con ID: " + tipoProductoIdCreado);
    }
    
    void testObtenerTipoProducto() throws Exception {
        System.out.println("\n----- Prueba: Obtener tipo de producto -----");
        
        // Esperar un momento para asegurar que los datos estén disponibles
        TimeUnit.SECONDS.sleep(1);
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Mostrar detalles de la respuesta para depuración
        System.out.println("Código de respuesta: " + response.statusCode());
        System.out.println("Cuerpo de respuesta: " + response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El tipo de producto debería encontrarse");
        
        // Extraer el tipo de producto
        TipoProductoDTO tipoProducto = objectMapper.readValue(response.body(), TipoProductoDTO.class);
        
        assertEquals(tipoProductoIdCreado, tipoProducto.getIdTipoProducto(), "El ID del tipo de producto debería coincidir");
        assertNotNull(tipoProducto.getNombre(), "El nombre no debería ser nulo");
        assertTrue(tipoProducto.getActivo(), "El tipo de producto debería estar activo");
        
        System.out.println("Tipo de producto obtenido correctamente: " + tipoProducto.getNombre());
    }
    
    void testActualizarTipoProducto() throws Exception {
        System.out.println("\n----- Prueba: Actualizar tipo de producto -----");
        
        // Preparar datos para la actualización
        TipoProductoDTO tipoProductoActualizado = new TipoProductoDTO();
        tipoProductoActualizado.setNombre("Postres y Dulces-" + System.currentTimeMillis());
        tipoProductoActualizado.setActivo(true);
        tipoProductoActualizado.setObservaciones("Tipo de producto actualizado en prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(tipoProductoActualizado);
        System.out.println("Request body: " + requestBody);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Mostrar detalles de la respuesta para depuración
        System.out.println("Código de respuesta: " + response.statusCode());
        System.out.println("Cuerpo de respuesta: " + response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El tipo de producto debería actualizarse correctamente");
        
        // Extraer el tipo de producto actualizado
        TipoProductoDTO tipoProducto = objectMapper.readValue(response.body(), TipoProductoDTO.class);
        
        assertEquals(tipoProductoIdCreado, tipoProducto.getIdTipoProducto(), "El ID del tipo de producto debería mantenerse");
        assertEquals(tipoProductoActualizado.getNombre(), tipoProducto.getNombre(), "El nombre del tipo de producto debería actualizarse");
        
        System.out.println("Tipo de producto actualizado correctamente a: " + tipoProducto.getNombre());
    }
    
    void testListarTiposProductos() throws Exception {
        System.out.println("\n----- Prueba: Listar tipos de productos -----");
        
        // Esperar un momento para asegurar que los datos estén disponibles
        TimeUnit.SECONDS.sleep(1);
        
        // 1. Listar todos los tipos de productos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos"))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        
        // Mostrar detalles de la respuesta para depuración
        System.out.println("Código de respuesta (listar todos): " + responseTodos.statusCode());
        
        assertEquals(200, responseTodos.statusCode(), "La lista de tipos de productos debería obtenerse correctamente");
        
        List<TipoProductoDTO> tiposProductos = objectMapper.readValue(
                responseTodos.body(), 
                new TypeReference<List<TipoProductoDTO>>() {});
        
        assertFalse(tiposProductos.isEmpty(), "Debería haber al menos un tipo de producto en la lista");
        System.out.println("Total de tipos de productos listados: " + tiposProductos.size());
        
        // Solo continuar con las pruebas de filtro si hay datos suficientes
        if (!tiposProductos.isEmpty()) {
            try {
                // 2. Buscar por nombre - usamos el nombre actualizado
                String nombreBusqueda = tiposProductos.get(0).getNombre();
                if (nombreBusqueda != null && !nombreBusqueda.trim().isEmpty()) {
                    // Tomamos solo la primera palabra para aumentar posibilidades de coincidencia
                    nombreBusqueda = nombreBusqueda.split(" ")[0];
                    
                    HttpRequest requestNombre = HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create(BASE_URL + "/tipos-productos?nombre=" + nombreBusqueda))
                            .build();
                    
                    HttpResponse<String> responseNombre = httpClient.send(requestNombre, HttpResponse.BodyHandlers.ofString());
                    
                    System.out.println("Código de respuesta (buscar por nombre '" + nombreBusqueda + "'): " + responseNombre.statusCode());
                    
                    assertEquals(200, responseNombre.statusCode(), "La búsqueda por nombre debería funcionar");
                    
                    // No verificamos que haya resultados porque depende de los datos existentes
                    List<TipoProductoDTO> tiposProductosPorNombre = objectMapper.readValue(
                            responseNombre.body(), 
                            new TypeReference<List<TipoProductoDTO>>() {});
                    
                    System.out.println("Tipos de productos encontrados por nombre '" + nombreBusqueda + "': " + tiposProductosPorNombre.size());
                }
            } catch (Exception e) {
                System.out.println("Advertencia: Error en prueba de búsqueda por nombre: " + e.getMessage());
                // Continuamos con las demás pruebas
            }
            
            try {
                // 3. Buscar por estado activo
                HttpRequest requestActivos = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(BASE_URL + "/tipos-productos?activo=true"))
                        .build();
                
                HttpResponse<String> responseActivos = httpClient.send(requestActivos, HttpResponse.BodyHandlers.ofString());
                
                System.out.println("Código de respuesta (buscar activos): " + responseActivos.statusCode());
                
                assertEquals(200, responseActivos.statusCode(), "La búsqueda por estado debería funcionar");
                
                List<TipoProductoDTO> tiposProductosActivos = objectMapper.readValue(
                        responseActivos.body(), 
                        new TypeReference<List<TipoProductoDTO>>() {});
                
                // No verificamos que haya resultados porque depende de los datos existentes
                System.out.println("Tipos de productos activos: " + tiposProductosActivos.size());
            } catch (Exception e) {
                System.out.println("Advertencia: Error en prueba de búsqueda por estado: " + e.getMessage());
                // Continuamos con las demás pruebas
            }
        }
    }
    
    void testEliminarTipoProducto() throws Exception {
        System.out.println("\n----- Prueba: Eliminar tipo de producto -----");
        
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Mostrar detalles de la respuesta para depuración
        System.out.println("Código de respuesta: " + response.statusCode());
        
        // Verificar respuesta - aceptamos 204 (sin contenido) o 200 (OK)
        assertTrue(response.statusCode() == 204 || response.statusCode() == 200, 
                "El tipo de producto debería eliminarse correctamente (código 204 o 200), recibido: " + response.statusCode());
        
        System.out.println("Tipo de producto eliminado correctamente");
    }
    
    void testTipoProductoNoExisteDespuesDeEliminar() throws Exception {
        System.out.println("\n----- Prueba: Verificar que el tipo de producto no existe -----");
        
        // Esperar un momento para asegurar que los datos se actualicen
        TimeUnit.SECONDS.sleep(1);
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/tipos-productos/" + tipoProductoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Mostrar detalles de la respuesta para depuración
        System.out.println("Código de respuesta: " + response.statusCode());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El tipo de producto no debería existir después de ser eliminado");
        
        System.out.println("Verificado: el tipo de producto ya no existe después de eliminar");
    }
}