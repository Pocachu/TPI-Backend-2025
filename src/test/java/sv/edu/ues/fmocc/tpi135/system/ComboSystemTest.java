package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Timeout;
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;

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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de sistema que validan el funcionamiento completo de la API
 * para la entidad Combo en un entorno real o similar a producción.
 * 
 * Nota: Estas pruebas requieren que la aplicación esté desplegada en un servidor.
 * Para la ejecución en un pipeline, se debe configurar un contenedor Docker con
 * la aplicación y la base de datos.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class ComboSystemTest {

    private final String BASE_URL = System.getProperty("api.url", "http://localhost:9080/tipicos-api/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(30)) // Aumentado el tiempo de conexión
            .build();
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .build();
    
    private Long comboIdCreado;

    @BeforeAll
    @Timeout(value = 2, unit = TimeUnit.MINUTES) // Timeout más generoso para la configuración inicial
    void setUp() throws Exception {
        System.out.println("URL de la API configurada: " + BASE_URL);
        
        // Esperar a que la aplicación esté lista
        waitForApplicationReady();
        
        // Limpiar datos previos si es necesario
        cleanupTestData();
    }
    
    private void waitForApplicationReady() throws Exception {
        int maxRetries = 30;
        int retryCount = 0;
        boolean isReady = false;
        Exception lastException = null;
        
        System.out.println("Esperando a que la aplicación esté disponible...");
        
        while (!isReady && retryCount < maxRetries) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(BASE_URL + "/combos"))
                        .timeout(Duration.ofSeconds(10))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                int statusCode = response.statusCode();
                System.out.println("Intento " + (retryCount + 1) + ": Código de estado " + statusCode);
                
                if (statusCode == 200) {
                    isReady = true;
                    System.out.println("¡Aplicación disponible!");
                } else {
                    System.out.println("La aplicación respondió pero con código: " + statusCode);
                }
            } catch (Exception e) {
                lastException = e;
                System.out.println("Intento " + (retryCount + 1) + ": La aplicación aún no está disponible. Error: " + e.getMessage());
            }
            
            if (!isReady) {
                retryCount++;
                // Espera exponencial con un máximo de 10 segundos
                long waitTime = Math.min(5000, 500 * (long)Math.pow(1.5, retryCount));
                System.out.println("Esperando " + waitTime + "ms antes del siguiente intento...");
                Thread.sleep(waitTime);
            }
        }
        
        if (!isReady) {
            System.err.println("Detalles completos del último error:");
            if (lastException != null) {
                lastException.printStackTrace();
            }
            fail("La aplicación no está disponible después de " + maxRetries + " intentos");
        }
    }
    
    private void cleanupTestData() {
        System.out.println("Limpiando datos de prueba...");
        
        try {
            // Intentar encontrar y eliminar cualquier combo existente con nombre similar
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/combos?nombre=Test"))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                List<ComboDTO> combos = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<ComboDTO>>() {});
                
                for (ComboDTO combo : combos) {
                    if (combo.getNombre() != null && combo.getNombre().contains("Test")) {
                        try {
                            HttpRequest deleteRequest = HttpRequest.newBuilder()
                                    .DELETE()
                                    .uri(URI.create(BASE_URL + "/combos/" + combo.getIdCombo()))
                                    .build();
                            
                            httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                            System.out.println("Eliminado combo existente con ID: " + combo.getIdCombo());
                        } catch (Exception e) {
                            System.out.println("Error al eliminar combo previo: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error durante la limpieza de datos: " + e.getMessage());
        }
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testCicloCompletoCRUD() throws Exception {
        try {
            // 1. Crear un combo
            testCrearCombo();
            
            // 2. Obtener el combo creado
            testObtenerCombo();
            
            // 3. Actualizar el combo
            testActualizarCombo();
            
            // 4. Listar combos con diferentes criterios
            testListarCombos();
            
            // 5. Eliminar el combo
            testEliminarCombo();
            
            // 6. Verificar que ya no existe
            testComboNoExisteDespuesDeEliminar();
        } catch (Exception e) {
            System.err.println("Error durante las pruebas CRUD: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    void testCrearCombo() throws Exception {
        System.out.println("Iniciando prueba de creación de combo...");
        
        // Preparar datos para la creación
        ComboDTO nuevoCombo = new ComboDTO();
        nuevoCombo.setNombre("Test Super Combo Familiar");
        nuevoCombo.setActivo(true);
        nuevoCombo.setDescripcionPublica("Combo especial para prueba de sistema");
        
        String requestBody = objectMapper.writeValueAsString(nuevoCombo);
        System.out.println("Cuerpo de la solicitud: " + requestBody);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/combos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "El combo debería crearse correctamente");
        
        // Extraer el combo creado
        ComboDTO comboCreado = objectMapper.readValue(response.body(), ComboDTO.class);
        
        assertNotNull(comboCreado.getIdCombo(), "El combo debería tener un ID asignado");
        assertEquals("Test Super Combo Familiar", comboCreado.getNombre(), "El nombre del combo debería coincidir");
        assertTrue(comboCreado.getActivo(), "El combo debería estar activo");
        
        // Guardar el ID para pruebas posteriores
        comboIdCreado = comboCreado.getIdCombo();
        
        System.out.println("Combo creado con ID: " + comboIdCreado);
    }
    
    void testObtenerCombo() throws Exception {
        System.out.println("Iniciando prueba de obtención de combo...");
        System.out.println("Buscando combo con ID: " + comboIdCreado);
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El combo debería encontrarse");
        
        // Extraer el combo
        ComboDTO combo = objectMapper.readValue(response.body(), ComboDTO.class);
        
        assertEquals(comboIdCreado, combo.getIdCombo(), "El ID del combo debería coincidir");
        assertEquals("Test Super Combo Familiar", combo.getNombre(), "El nombre del combo debería coincidir");
        assertTrue(combo.getActivo(), "El combo debería estar activo");
        
        System.out.println("Combo obtenido correctamente: " + combo.getNombre());
    }
    
    void testActualizarCombo() throws Exception {
        System.out.println("Iniciando prueba de actualización de combo...");
        
        // Preparar datos para la actualización
        ComboDTO comboActualizado = new ComboDTO();
        comboActualizado.setNombre("Test Super Combo Familiar Actualizado");
        comboActualizado.setActivo(true);
        comboActualizado.setDescripcionPublica("Combo especial para prueba de sistema - Actualizado");
        
        String requestBody = objectMapper.writeValueAsString(comboActualizado);
        System.out.println("Cuerpo de la solicitud: " + requestBody);
        
        // Esperar un poco antes de actualizar para evitar problemas de concurrencia
        Thread.sleep(1000);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El combo debería actualizarse correctamente");
        
        // Extraer el combo actualizado
        ComboDTO combo = objectMapper.readValue(response.body(), ComboDTO.class);
        
        assertEquals(comboIdCreado, combo.getIdCombo(), "El ID del combo debería mantenerse");
        assertEquals("Test Super Combo Familiar Actualizado", combo.getNombre(), "El nombre del combo debería actualizarse");
        
        System.out.println("Combo actualizado correctamente a: " + combo.getNombre());
    }
    
    void testListarCombos() throws Exception {
        System.out.println("Iniciando prueba de listado de combos...");
        
        // Esperar un poco antes de consultar para asegurar que los cambios se han guardado
        Thread.sleep(1000);
        
        // 1. Listar todos los combos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos"))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado (listar todos): " + responseTodos.statusCode());
        
        assertEquals(200, responseTodos.statusCode(), "La lista de combos debería obtenerse correctamente");
        
        List<ComboDTO> combos = objectMapper.readValue(
                responseTodos.body(), 
                new TypeReference<List<ComboDTO>>() {});
        
        assertFalse(combos.isEmpty(), "Debería haber al menos un combo en la lista");
        System.out.println("Total de combos listados: " + combos.size());
        
        // 2. Buscar por nombre
        HttpRequest requestNombre = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos?nombre=Test"))
                .build();
        
        HttpResponse<String> responseNombre = httpClient.send(requestNombre, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado (buscar por nombre): " + responseNombre.statusCode());
        System.out.println("Respuesta: " + responseNombre.body());
        
        assertEquals(200, responseNombre.statusCode(), "La búsqueda por nombre debería funcionar");
        
        List<ComboDTO> combosPorNombre = objectMapper.readValue(
                responseNombre.body(), 
                new TypeReference<List<ComboDTO>>() {});
        
        assertFalse(combosPorNombre.isEmpty(), "Debería encontrarse al menos un combo con el nombre buscado");
        System.out.println("Combos encontrados por nombre: " + combosPorNombre.size());
        
        // 3. Buscar por estado activo
        HttpRequest requestActivos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos?activo=true"))
                .build();
        
        HttpResponse<String> responseActivos = httpClient.send(requestActivos, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado (buscar por estado): " + responseActivos.statusCode());
        
        assertEquals(200, responseActivos.statusCode(), "La búsqueda por estado debería funcionar");
        
        List<ComboDTO> combosActivos = objectMapper.readValue(
                responseActivos.body(), 
                new TypeReference<List<ComboDTO>>() {});
        
        assertFalse(combosActivos.isEmpty(), "Debería haber al menos un combo activo");
        System.out.println("Combos activos: " + combosActivos.size());
    }
    
    void testEliminarCombo() throws Exception {
        System.out.println("Iniciando prueba de eliminación de combo...");
        System.out.println("Eliminando combo con ID: " + comboIdCreado);
        
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado: " + response.statusCode());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "El combo debería eliminarse correctamente");
        
        System.out.println("Combo eliminado correctamente");
    }
    
    void testComboNoExisteDespuesDeEliminar() throws Exception {
        System.out.println("Iniciando prueba de verificación de eliminación...");
        
        // Esperar un poco para asegurar que los cambios se han guardado
        Thread.sleep(1000);
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Código de estado: " + response.statusCode());
        System.out.println("Respuesta: " + response.body());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El combo no debería existir después de ser eliminado");
        
        System.out.println("Verificado: el combo ya no existe después de eliminar");
    }
}