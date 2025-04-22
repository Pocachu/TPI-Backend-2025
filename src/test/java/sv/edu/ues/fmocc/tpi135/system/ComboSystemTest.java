package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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

    private final String BASE_URL = System.getProperty("api.url", "http://localhost:8080/tipicos-tpi135/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private Long comboIdCreado;

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
                        .uri(URI.create(BASE_URL + "/combos"))
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
    }
    
    void testCrearCombo() throws Exception {
        // Preparar datos para la creación
        ComboDTO nuevoCombo = new ComboDTO();
        nuevoCombo.setNombre("Super Combo Familiar");
        nuevoCombo.setActivo(true);
        nuevoCombo.setDescripcionPublica("Combo especial para compartir en familia");
        
        String requestBody = objectMapper.writeValueAsString(nuevoCombo);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/combos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "El combo debería crearse correctamente");
        
        // Extraer el combo creado
        ComboDTO comboCreado = objectMapper.readValue(response.body(), ComboDTO.class);
        
        assertNotNull(comboCreado.getIdCombo(), "El combo debería tener un ID asignado");
        assertEquals("Super Combo Familiar", comboCreado.getNombre(), "El nombre del combo debería coincidir");
        assertTrue(comboCreado.getActivo(), "El combo debería estar activo");
        
        // Guardar el ID para pruebas posteriores
        comboIdCreado = comboCreado.getIdCombo();
        
        System.out.println("Combo creado con ID: " + comboIdCreado);
    }
    
    void testObtenerCombo() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El combo debería encontrarse");
        
        // Extraer el combo
        ComboDTO combo = objectMapper.readValue(response.body(), ComboDTO.class);
        
        assertEquals(comboIdCreado, combo.getIdCombo(), "El ID del combo debería coincidir");
        assertEquals("Super Combo Familiar", combo.getNombre(), "El nombre del combo debería coincidir");
        assertTrue(combo.getActivo(), "El combo debería estar activo");
        
        System.out.println("Combo obtenido correctamente: " + combo.getNombre());
    }
    
    void testActualizarCombo() throws Exception {
        // Preparar datos para la actualización
        ComboDTO comboActualizado = new ComboDTO();
        comboActualizado.setNombre("Super Combo Familiar Actualizado");
        comboActualizado.setActivo(true);
        comboActualizado.setDescripcionPublica("Combo especial para compartir en familia - Versión actualizada");
        
        String requestBody = objectMapper.writeValueAsString(comboActualizado);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El combo debería actualizarse correctamente");
        
        // Extraer el combo actualizado
        ComboDTO combo = objectMapper.readValue(response.body(), ComboDTO.class);
        
        assertEquals(comboIdCreado, combo.getIdCombo(), "El ID del combo debería mantenerse");
        assertEquals("Super Combo Familiar Actualizado", combo.getNombre(), "El nombre del combo debería actualizarse");
        
        System.out.println("Combo actualizado correctamente a: " + combo.getNombre());
    }
    
    void testListarCombos() throws Exception {
        // 1. Listar todos los combos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos"))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseTodos.statusCode(), "La lista de combos debería obtenerse correctamente");
        
        List<ComboDTO> combos = objectMapper.readValue(
                responseTodos.body(), 
                new TypeReference<List<ComboDTO>>() {});
        
        assertFalse(combos.isEmpty(), "Debería haber al menos un combo en la lista");
        System.out.println("Total de combos listados: " + combos.size());
        
        // 2. Buscar por nombre
        HttpRequest requestNombre = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos?nombre=Familiar"))
                .build();
        
        HttpResponse<String> responseNombre = httpClient.send(requestNombre, HttpResponse.BodyHandlers.ofString());
        
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
        
        assertEquals(200, responseActivos.statusCode(), "La búsqueda por estado debería funcionar");
        
        List<ComboDTO> combosActivos = objectMapper.readValue(
                responseActivos.body(), 
                new TypeReference<List<ComboDTO>>() {});
        
        assertFalse(combosActivos.isEmpty(), "Debería haber al menos un combo activo");
        System.out.println("Combos activos: " + combosActivos.size());
    }
    
    void testEliminarCombo() throws Exception {
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "El combo debería eliminarse correctamente");
        
        System.out.println("Combo eliminado correctamente");
    }
    
    void testComboNoExisteDespuesDeEliminar() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/combos/" + comboIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El combo no debería existir después de ser eliminado");
        
        System.out.println("Verificado: el combo ya no existe después de eliminar");
    }
}