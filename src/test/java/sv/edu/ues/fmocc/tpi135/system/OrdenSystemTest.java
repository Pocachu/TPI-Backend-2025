package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDetalleDTO;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de sistema que validan el funcionamiento completo de la API
 * para la entidad Orden en un entorno real o similar a producción.
 * 
 * Nota: Estas pruebas requieren que la aplicación esté desplegada en un servidor.
 * Para la ejecución en un pipeline, se debe configurar un contenedor Docker con
 * la aplicación y la base de datos.
 */ 
@TestInstance(Lifecycle.PER_CLASS)
public class OrdenSystemTest {

    private final String BASE_URL = System.getProperty("api.url", "http://localhost:8080/tipicos-tpi135/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    
    private Long ordenIdCreada;

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
                        .uri(URI.create(BASE_URL + "/ordenes"))
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
        // 1. Crear una orden
        testCrearOrden();
        
        // 2. Obtener la orden creada
        testObtenerOrden();
        
        // 3. Actualizar la orden
        testActualizarOrden();
        
        // 4. Listar órdenes con diferentes criterios
        testListarOrdenes();
        
        // 5. Anular la orden
        testAnularOrden();
        
        // 6. Eliminar la orden
        testEliminarOrden();
        
        // 7. Verificar que ya no existe
        testOrdenNoExisteDespuesDeEliminar();
    }
    
    void testCrearOrden() throws Exception {
        // Crear los detalles de la orden
        List<OrdenDetalleDTO> detalles = new ArrayList<>();
        OrdenDetalleDTO detalle = new OrdenDetalleDTO();
        detalle.setIdProductoPrecio(1L); // Debe existir este registro en la base de datos
        detalle.setCantidad(2);
        detalle.setPrecio(new BigDecimal("5.00"));
        detalle.setObservaciones("Detalle de prueba en Sistema");
        detalles.add(detalle);
        
        // Preparar datos para la creación
        OrdenDTO nuevaOrden = new OrdenDTO();
        nuevaOrden.setFecha(new Date());
        nuevaOrden.setSucursal("S001");
        nuevaOrden.setAnulada(false);
        nuevaOrden.setDetalles(detalles);
        
        String requestBody = objectMapper.writeValueAsString(nuevaOrden);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/ordenes"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "La orden debería crearse correctamente");
        
        // Extraer la orden creada
        OrdenDTO ordenCreada = objectMapper.readValue(response.body(), OrdenDTO.class);
        
        assertNotNull(ordenCreada.getIdOrden(), "La orden debería tener un ID asignado");
        assertEquals("S001", ordenCreada.getSucursal(), "La sucursal de la orden debería coincidir");
        assertFalse(ordenCreada.getAnulada(), "La orden no debería estar anulada");
        
        // Guardar el ID para pruebas posteriores
        ordenIdCreada = ordenCreada.getIdOrden();
        
        System.out.println("Orden creada con ID: " + ordenIdCreada);
    }
    
    void testObtenerOrden() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "La orden debería encontrarse");
        
        // Extraer la orden
        OrdenDTO orden = objectMapper.readValue(response.body(), OrdenDTO.class);
        
        assertEquals(ordenIdCreada, orden.getIdOrden(), "El ID de la orden debería coincidir");
        assertEquals("S001", orden.getSucursal(), "La sucursal de la orden debería coincidir");
        assertFalse(orden.getAnulada(), "La orden no debería estar anulada");
        
        // Verificar que existan detalles
        assertNotNull(orden.getDetalles(), "La orden debería tener detalles");
        assertTrue(orden.getDetalles().size() > 0, "La orden debería tener al menos un detalle");
        
        System.out.println("Orden obtenida correctamente: " + orden.getIdOrden() + " con " + 
                orden.getDetalles().size() + " detalles");
    }
    
    void testActualizarOrden() throws Exception {
        // Preparar datos para la actualización
        OrdenDTO ordenActualizada = new OrdenDTO();
        ordenActualizada.setSucursal("S002");
        
        // Mantener los mismos detalles para esta prueba
        List<OrdenDetalleDTO> detalles = new ArrayList<>();
        OrdenDetalleDTO detalle = new OrdenDetalleDTO();
        detalle.setIdProductoPrecio(1L);
        detalle.setCantidad(3); // Cambiar la cantidad
        detalle.setPrecio(new BigDecimal("5.50")); // Cambiar el precio
        detalle.setObservaciones("Detalle actualizado en prueba de sistema");
        detalles.add(detalle);
        
        ordenActualizada.setDetalles(detalles);
        
        String requestBody = objectMapper.writeValueAsString(ordenActualizada);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "La orden debería actualizarse correctamente");
        
        // Extraer la orden actualizada
        OrdenDTO orden = objectMapper.readValue(response.body(), OrdenDTO.class);
        
        assertEquals(ordenIdCreada, orden.getIdOrden(), "El ID de la orden debería mantenerse");
        assertEquals("S002", orden.getSucursal(), "La sucursal de la orden debería actualizarse");
        
        System.out.println("Orden actualizada correctamente a: Sucursal " + orden.getSucursal());
    }
    
    void testListarOrdenes() throws Exception {
        // 1. Listar todas las órdenes
        HttpRequest requestTodas = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes"))
                .build();
        
        HttpResponse<String> responseTodas = httpClient.send(requestTodas, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseTodas.statusCode(), "La lista de órdenes debería obtenerse correctamente");
        
        List<OrdenDTO> ordenes = objectMapper.readValue(
                responseTodas.body(), 
                new TypeReference<List<OrdenDTO>>() {});
        
        assertFalse(ordenes.isEmpty(), "Debería haber al menos una orden en la lista");
        System.out.println("Total de órdenes listadas: " + ordenes.size());
        
        // 2. Buscar por sucursal
        HttpRequest requestSucursal = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes?sucursal=S002"))
                .build();
        
        HttpResponse<String> responseSucursal = httpClient.send(requestSucursal, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseSucursal.statusCode(), "La búsqueda por sucursal debería funcionar");
        
        List<OrdenDTO> ordenesPorSucursal = objectMapper.readValue(
                responseSucursal.body(), 
                new TypeReference<List<OrdenDTO>>() {});
        
        assertFalse(ordenesPorSucursal.isEmpty(), "Debería encontrarse al menos una orden con la sucursal buscada");
        assertEquals("S002", ordenesPorSucursal.get(0).getSucursal(), "La sucursal debe coincidir con la búsqueda");
        System.out.println("Órdenes encontradas por sucursal: " + ordenesPorSucursal.size());
        
        // 3. Buscar por estado de anulación
        HttpRequest requestNoAnuladas = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes?anulada=false"))
                .build();
        
        HttpResponse<String> responseNoAnuladas = httpClient.send(requestNoAnuladas, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseNoAnuladas.statusCode(), "La búsqueda por estado de anulación debería funcionar");
        
        List<OrdenDTO> ordenesNoAnuladas = objectMapper.readValue(
                responseNoAnuladas.body(), 
                new TypeReference<List<OrdenDTO>>() {});
        
        assertFalse(ordenesNoAnuladas.isEmpty(), "Debería haber al menos una orden no anulada");
        System.out.println("Órdenes no anuladas: " + ordenesNoAnuladas.size());
    }
    
    void testAnularOrden() throws Exception {
        // Enviar solicitud PUT para anular
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada + "/anular"))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "La orden debería anularse correctamente");
        
        // Verificar que la orden ahora está anulada
        HttpRequest verifyRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> verifyResponse = httpClient.send(verifyRequest, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, verifyResponse.statusCode(), "La orden anulada debería encontrarse");
        
        OrdenDTO ordenAnulada = objectMapper.readValue(verifyResponse.body(), OrdenDTO.class);
        assertTrue(ordenAnulada.getAnulada(), "La orden debería estar marcada como anulada");
        
        System.out.println("Orden anulada correctamente");
    }
    
    void testEliminarOrden() throws Exception {
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "La orden debería eliminarse correctamente");
        
        System.out.println("Orden eliminada correctamente");
    }
    
    void testOrdenNoExisteDespuesDeEliminar() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "La orden no debería existir después de ser eliminada");
        
        System.out.println("Verificado: la orden ya no existe después de eliminar");
    }}