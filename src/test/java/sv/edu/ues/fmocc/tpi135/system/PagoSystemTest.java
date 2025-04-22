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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de sistema que validan el funcionamiento completo de la API
 * para la entidad Pago en un entorno real o similar a producción.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class PagoSystemTest {

    private final String BASE_URL = System.getProperty("api.url", "http://localhost:9080/tipicos-api/api");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    
    private Long pagoIdCreado;
    private Long ordenIdExistente;

    @BeforeAll
    void setUp() throws Exception {
        System.out.println("Iniciando pruebas en la URL: " + BASE_URL);
        
        // Esperar más tiempo a que la aplicación esté lista
        waitForApplicationReady();
        
        // Crear una orden primero si no encontramos una existente
        createOrFindExistingOrder();
        
        // Limpiar datos previos de pruebas anteriores
        cleanupTestData();
    }
    
    private void waitForApplicationReady() throws Exception {
        int maxRetries = 30;
        int retryCount = 0;
        boolean isReady = false;
        
        System.out.println("Esperando a que la aplicación esté disponible...");
        
        // Primero esperamos un tiempo fijo para dar tiempo al servidor de aplicaciones
        System.out.println("Esperando 30 segundos iniciales para el arranque del servidor...");
        TimeUnit.SECONDS.sleep(30);
        
        while (!isReady && retryCount < maxRetries) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(BASE_URL + "/productos")) // Probamos un endpoint más simple primero
                        .timeout(Duration.ofSeconds(10))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                System.out.println("Respuesta de verificación: Código " + response.statusCode());
                
                if (response.statusCode() == 200) {
                    isReady = true;
                    System.out.println("Aplicación disponible!");
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("La aplicación aún no está disponible. Reintentando... Error: " + e.getMessage());
            }
            
            if (!isReady) {
                retryCount++;
                System.out.println("Intento " + retryCount + " de " + maxRetries);
                TimeUnit.SECONDS.sleep(5);
            }
        }
        
        if (!isReady) {
            System.err.println("ERROR: La aplicación no está disponible después de " + maxRetries + " intentos");
            fail("La aplicación no está disponible después de " + maxRetries + " intentos");
        }
    }
    
    private void createOrFindExistingOrder() throws Exception {
        // Buscar una orden existente para usar en las pruebas
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/ordenes"))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Buscando órdenes existentes. Respuesta: " + response.statusCode());
            
            if (response.statusCode() == 200 && !response.body().isEmpty() && !response.body().equals("[]")) {
                // Extraer la primera orden disponible
                List<?> ordenes = objectMapper.readValue(response.body(), List.class);
                if (!ordenes.isEmpty()) {
                    Object orden = ordenes.get(0);
                    ordenIdExistente = Long.valueOf(objectMapper.convertValue(orden, java.util.Map.class).get("idOrden").toString());
                    System.out.println("Orden existente encontrada con ID: " + ordenIdExistente);
                    return;
                }
            }
            
            // Si no hay órdenes, crear una nueva
            System.out.println("No se encontraron órdenes, creando una nueva...");
            ordenIdExistente = createNewOrder();
            
        } catch (Exception e) {
            System.err.println("Error al buscar órdenes: " + e.getMessage());
            e.printStackTrace();
            
            // En caso de error, intentamos crear una nueva orden
            ordenIdExistente = createNewOrder();
        }
    }
    
    private Long createNewOrder() throws Exception {
        // Crear una orden simple para las pruebas
        String orderJson = "{\"sucursal\":\"S001\",\"detalles\":[{\"idProductoPrecio\":1,\"cantidad\":2,\"precio\":5.00}]}";
        
        HttpRequest createOrderRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(orderJson))
                .uri(URI.create(BASE_URL + "/ordenes"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = httpClient.send(createOrderRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 201) {
            System.err.println("Error al crear orden: " + response.body());
            // Si fallamos al crear una orden, usamos un ID fijo para las pruebas
            System.out.println("Usando ID de orden por defecto: 1");
            return 1L;
        }
        
        // Extraer el ID de la orden creada
        java.util.Map<?,?> responseMap = objectMapper.readValue(response.body(), java.util.Map.class);
        Long orderId = Long.valueOf(responseMap.get("idOrden").toString());
        System.out.println("Nueva orden creada con ID: " + orderId);
        return orderId;
    }
    
    private void cleanupTestData() {
        // Intentamos eliminar pagos previos que podrían interferir con las pruebas
        try {
            // Buscar pagos de la orden que estamos usando
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/pagos/orden/" + ordenIdExistente))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 && !response.body().equals("[]")) {
                List<PagoDTO> pagosExistentes = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<PagoDTO>>() {});
                
                // Eliminar pagos previos
                for (PagoDTO pago : pagosExistentes) {
                    System.out.println("Eliminando pago previo con ID: " + pago.getIdPago());
                    
                    HttpRequest deleteRequest = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(URI.create(BASE_URL + "/pagos/" + pago.getIdPago()))
                            .timeout(Duration.ofSeconds(10))
                            .build();
                    
                    httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al limpiar datos de prueba: " + e.getMessage());
            // Continuamos con las pruebas aunque falle la limpieza
        }
    }

    @Test
    void testCicloCompletoCRUD() throws Exception {
        try {
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
        } catch (Exception e) {
            System.err.println("Error en la prueba de ciclo CRUD: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
        System.out.println("Creando pago con body: " + requestBody);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/pagos"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de creación: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        if (response.statusCode() != 201) {
            fail("Error al crear pago: " + response.body() + " (Código: " + response.statusCode() + ")");
        }
        
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
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de obtener pago: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El pago debería encontrarse");
        
        // Extraer el pago
        PagoDTO pago = objectMapper.readValue(response.body(), PagoDTO.class);
        
        assertEquals(pagoIdCreado, pago.getIdPago(), "El ID del pago debería coincidir");
        assertEquals(ordenIdExistente, pago.getIdOrden(), "El ID de la orden debería coincidir");
        assertEquals("EFECTIVO", pago.getMetodoPago(), "El método de pago debería coincidir");
        
        // Verificar que existan detalles
        assertNotNull(pago.getDetalles(), "El pago debería tener detalles");
        assertFalse(pago.getDetalles().isEmpty(), "El pago debería tener al menos un detalle");
        
        System.out.println("Pago obtenido correctamente: " + pago.getIdPago() + " con " + 
                pago.getDetalles().size() + " detalles");
    }
    
    void testActualizarPago() throws Exception {
        // Primero obtenemos el pago actual para no perder los datos
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .timeout(Duration.ofSeconds(10))
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
        System.out.println("Actualizando pago con body: " + requestBody);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de actualización: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        assertEquals(200, response.statusCode(), "El pago debería actualizarse correctamente");
        
        // Extraer el pago actualizado
        PagoDTO pago = objectMapper.readValue(response.body(), PagoDTO.class);
        
        assertEquals(pagoIdCreado, pago.getIdPago(), "El ID del pago debería mantenerse");
        assertEquals("TARJETA", pago.getMetodoPago(), "El método de pago debería actualizarse");
        
        System.out.println("Pago actualizado correctamente a método: " + pago.getMetodoPago());
    }
    
    void testListarPagos() throws Exception {
        // Primero esperamos un poco para asegurarnos que las transacciones anteriores se completen
        TimeUnit.SECONDS.sleep(2);
        
        // 1. Listar todos los pagos
        HttpRequest requestTodos = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos"))
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> responseTodos = httpClient.send(requestTodos, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de listar pagos: " + responseTodos.statusCode() + " - " + responseTodos.body());
        
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
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> responseMetodo = httpClient.send(requestMetodo, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de buscar por método: " + responseMetodo.statusCode() + " - " + responseMetodo.body());
        
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
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> responseOrden = httpClient.send(requestOrden, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de buscar por orden: " + responseOrden.statusCode() + " - " + responseOrden.body());
        
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
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> responsePagosPorOrden = httpClient.send(requestPagosPorOrden, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de endpoint específico: " + responsePagosPorOrden.statusCode() + " - " + responsePagosPorOrden.body());
        
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
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de eliminar pago: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        assertEquals(204, response.statusCode(), "El pago debería eliminarse correctamente");
        
        System.out.println("Pago eliminado correctamente");
    }
    
    void testPagoNoExisteDespuesDeEliminar() throws Exception {
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/pagos/" + pagoIdCreado))
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Respuesta de verificar eliminación: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "El pago no debería existir después de ser eliminado");
        
        System.out.println("Verificado: el pago ya no existe después de eliminar");
    }
}