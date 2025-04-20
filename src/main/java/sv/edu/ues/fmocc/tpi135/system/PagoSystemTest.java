package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDetalleDTO;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
 * para la entidad Pago en un entorno real o similar a producción.
 * 
 * Nota: Estas pruebas requieren que la aplicación esté desplegada en un servidor.
 * Para la ejecución en un pipeline, se debe configurar un contenedor Docker con
 * la aplicación y la base de datos.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class PagoSystemTest {

    private final String BASE_URL = System.getProperty("api.url", "http://localhost:9080/tipicos-tpi135/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    
    private Long pagoIdCreado;
    private Long ordenIdExistente;

    @BeforeAll
    void setUp() throws Exception {
        // Esperar a que la aplicación esté lista
        waitForApplicationReady();
        
        // Encontrar una orden existente para usar en las pruebas
        findExistingOrder();
        
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
                        .uri(URI.create(BASE_URL + "/pagos"))
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
    
    private void findExistingOrder() throws Exception {
        // Buscar una orden existente para usar en las pruebas
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/ordenes"))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                // Extraer la primera orden disponible
                List<?> ordenes = objectMapper.readValue(response.body(), List.class);
                if (!ordenes.isEmpty()) {
                    Object orden = ordenes.get(0);
                    ordenIdExistente = Long.valueOf(objectMapper.convertValue(orden, java.util.Map.class).get("idOrden").toString());
                    System.out.println("Orden existente encontrada con ID: " + ordenIdExistente);
                    return;
                }
            }
            
            // Si no hay órdenes, crear una para las pruebas
            ordenIdExistente = 1L; // Usar ID 1 por defecto, asumiendo que existe
            System.out.println("No se encontraron órdenes, usando ID por defecto: " + ordenIdExistente);
        } catch (Exception e) {
            ordenIdExistente = 1L; // Usar ID 1 por defecto en caso de error
            System.out.println("Error al buscar órdenes, usando ID por defecto: " + ordenIdExistente);
        }
    }
    
    private void cleanupTestData() {
        // Aquí se podría implementar la limpieza de datos de prueba previos
        // Por simplicidad, no implementamos esto para este ejemplo
    }

    @Test
    void testCicloCompletoCRUD() throws Exception {
        // 1. Crear un pago
        testCrearPago();
        
        // 2. Obtener el pago creado
        testObtenerPago();
        
        // 3. Actualizar el pago
        testActualizarPago();
        
        // 4. Listar pagos con diferentes criterios
        testListarPagos();
        
        // 5. Eliminar el pago
        testEliminarPago();
        
        // 6. Verificar que ya no existe
        testPagoNoExisteDespuesDeEliminar();
    }
    
    void testCrearPago() throws Exception {
        // Preparar datos para la creación
        PagoDTO nuevoPago = new PagoDTO();
        nuevoPago.setIdOrden(ordenIdExistente);
        nuevoPago.setMetodoPago("EFECTIVO");
        nuevoPago.setFecha(new Date());
        nuevoPago.setReferencia("Pago de prueba en sistema");
        
        // Crear detalles del pago
        PagoDetalleDTO detalle = new PagoDetalleDTO();
        detalle.setMonto(new BigDecimal("125.50"));
        detalle.setObservaciones("Detalle de prueba en sistema");
        
        nuevoPago.setDetalles(List.of(detalle));
        
        String requestBody = objectMapper.writeValueAsString(nuevoPago);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/pagos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(201, response.statusCode(), "El pago debería crearse correctamente");
        
        // Extraer el pago creado
        PagoDTO pagoCreado = objectMapper.readValue(response.body(), PagoDTO.class);
        
        assertNotNull(pagoCreado.getIdPago(), "El pago debería tener un ID asignado");
        assertEquals(ordenIdExistente, pagoCreado.getIdOrden(), "El ID de la orden debería coincidir");
        assertEquals("EFECTIVO", pagoCreado.getMetodoPago(), "El método de pago debería coincidir");
        
        // Guardar el ID para pruebas posteriores
        pagoIdCreado = pagoCreado.getIdPago();
        
        System.out.println("Pago creado con ID: " + pagoIdCreado);
    }
    
    void testObtenerPago() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El pago debería encontrarse");
        
        // Extraer el pago
        PagoDTO pago = objectMapper.readValue(response.body(), PagoDTO.class);
        
        assertEquals(pagoIdCreado, pago.getIdPago(), "El ID del pago debería coincidir");
        assertEquals(ordenIdExistente, pago.getIdOrden(), "El ID de la orden debería coincidir");
        assertEquals("EFECTIVO", pago.getMetodoPago(), "El método de pago debería coincidir");
        
        // Verificar que existan detalles
        assertNotNull(pago.getDetalles(), "El pago debería tener detalles");
        assertTrue(pago.getDetalles().size() > 0, "El pago debería tener al menos un detalle");
        
        System.out.println("Pago obtenido correctamente: " + pago.getIdPago() + " con " + 
                pago.getDetalles().size() + " detalles");
    }
    
    void testActualizarPago() throws Exception {
        // Primero obtenemos el pago actual para no perder los datos
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .build();
        
        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        PagoDTO pagoActual = objectMapper.readValue(getResponse.body(), PagoDTO.class);
        
        // Preparar datos para la actualización
        PagoDTO pagoActualizado = new PagoDTO();
        pagoActualizado.setMetodoPago("TARJETA");
        pagoActualizado.setReferencia("Pago actualizado en prueba de sistema");
        
        // Mantener los detalles pero actualizar el monto del primero
        if (pagoActual.getDetalles() != null && !pagoActual.getDetalles().isEmpty()) {
            List<PagoDetalleDTO> detallesActualizados = pagoActual.getDetalles();
            detallesActualizados.get(0).setMonto(new BigDecimal("150.75"));
            detallesActualizados.get(0).setObservaciones("Detalle actualizado en prueba");
            pagoActualizado.setDetalles(detallesActualizados);
        }
        
        String requestBody = objectMapper.writeValueAsString(pagoActualizado);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El pago debería actualizarse correctamente");
        
        // Extraer el pago actualizado
        PagoDTO pago = objectMapper.readValue(response.body(), PagoDTO.class);
        
        assertEquals(pagoIdCreado, pago.getIdPago(), "El ID del pago debería mantenerse");
        assertEquals("TARJETA", pago.getMetodoPago(), "El método de pago debería actualizarse");
        
        System.out.println("Pago actualizado correctamente a método: " + pago.getMetodoPago());
    }
    
    void testListarPagos() throws Exception {
        // 1. Listar todos los pagos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos"))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseTodos.statusCode(), "La lista de pagos debería obtenerse correctamente");
        
        List<PagoDTO> pagos = objectMapper.readValue(
                responseTodos.body(), 
                new TypeReference<List<PagoDTO>>() {});
        
        assertFalse(pagos.isEmpty(), "Debería haber al menos un pago en la lista");
        System.out.println("Total de pagos listados: " + pagos.size());
        
        // 2. Buscar por método de pago
        HttpRequest requestMetodo = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos?metodoPago=TARJETA"))
                .build();
        
        HttpResponse<String> responseMetodo = httpClient.send(requestMetodo, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseMetodo.statusCode(), "La búsqueda por método de pago debería funcionar");
        
        List<PagoDTO> pagosPorMetodo = objectMapper.readValue(
                responseMetodo.body(), 
                new TypeReference<List<PagoDTO>>() {});
        
        assertFalse(pagosPorMetodo.isEmpty(), "Debería encontrarse al menos un pago con el método buscado");
        System.out.println("Pagos encontrados por método: " + pagosPorMetodo.size());
        
        // 3. Buscar por orden
        HttpRequest requestOrden = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos?idOrden=" + ordenIdExistente))
                .build();
        
        HttpResponse<String> responseOrden = httpClient.send(requestOrden, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responseOrden.statusCode(), "La búsqueda por orden debería funcionar");
        
        List<PagoDTO> pagosPorOrden = objectMapper.readValue(
                responseOrden.body(), 
                new TypeReference<List<PagoDTO>>() {});
        
        assertFalse(pagosPorOrden.isEmpty(), "Debería haber al menos un pago para la orden");
        System.out.println("Pagos para la orden " + ordenIdExistente + ": " + pagosPorOrden.size());
        
        // 4. Usar endpoint específico para pagos por orden
        HttpRequest requestPagosPorOrden = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos/orden/" + ordenIdExistente))
                .build();
        
        HttpResponse<String> responsePagosPorOrden = httpClient.send(requestPagosPorOrden, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, responsePagosPorOrden.statusCode(), "El endpoint específico para pagos por orden debería funcionar");
        
        List<PagoDTO> pagosPorOrdenEspecifico = objectMapper.readValue(
                responsePagosPorOrden.body(), 
                new TypeReference<List<PagoDTO>>() {});
        
        assertFalse(pagosPorOrdenEspecifico.isEmpty(), "Debería haber al menos un pago para la orden");
        System.out.println("Pagos para la orden (endpoint específico) " + ordenIdExistente + ": " + pagosPorOrdenEspecifico.size());
    }
    
    void testEliminarPago() throws Exception {
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "El pago debería eliminarse correctamente");
        
        System.out.println("Pago eliminado correctamente");
    }
    
    void testPagoNoExisteDespuesDeEliminar() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El pago no debería existir después de ser eliminado");
        
        System.out.println("Verificado: el pago ya no existe después de eliminar");
    }
}