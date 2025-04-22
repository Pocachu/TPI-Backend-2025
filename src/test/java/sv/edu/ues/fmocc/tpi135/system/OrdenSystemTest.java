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
import com.fasterxml.jackson.databind.DeserializationFeature;
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

    // Permitir configuración vía propiedades del sistema para entornos CI/CD
    private final String BASE_URL = System.getProperty("api.url", "http://localhost:9080/tipicos-api/api");
    private final int WAIT_TIME_SECONDS = Integer.parseInt(System.getProperty("wait.time.seconds", "60"));
    private final int RETRY_INTERVAL_SECONDS = Integer.parseInt(System.getProperty("retry.interval.seconds", "5"));
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(30)) // Incrementado para mayor tolerancia
            .build();
            
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // Mayor tolerancia en deserialización
            .build();
    
    private Long ordenIdCreada;
    private Long productoIdParaTest;

    @BeforeAll
    void setUp() throws Exception {
        System.out.println("Configuración de la prueba con URL base: " + BASE_URL);
        
        // Esperar a que la aplicación esté lista
        waitForApplicationReady();
        
        // Limpiar datos previos si es necesario
        cleanupTestData();
        
        // Buscar o crear un producto para usar en las pruebas
        setupTestProduct();
    }
    
    private void waitForApplicationReady() throws Exception {
        int maxRetries = 30;
        int retryCount = 0;
        boolean isReady = false;
        
        System.out.println("Esperando a que la aplicación esté disponible (máximo " + WAIT_TIME_SECONDS + " segundos)...");
        
        Exception lastException = null;
        
        while (!isReady && retryCount < maxRetries) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(BASE_URL + "/productos"))
                        .timeout(Duration.ofSeconds(10))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    isReady = true;
                    System.out.println("¡Aplicación disponible después de " + (retryCount * RETRY_INTERVAL_SECONDS) + " segundos!");
                } else {
                    System.out.println("La aplicación respondió con código " + response.statusCode() + ", esperando...");
                }
            } catch (Exception e) {
                lastException = e;
                System.out.println("La aplicación aún no está disponible. Reintentando... (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
            }
            
            if (!isReady) {
                retryCount++;
                TimeUnit.SECONDS.sleep(RETRY_INTERVAL_SECONDS);
            }
        }
        
        if (!isReady) {
            System.err.println("La aplicación no respondió después de " + WAIT_TIME_SECONDS + " segundos");
            if (lastException != null) {
                System.err.println("Último error: " + lastException.getMessage());
                lastException.printStackTrace();
            }
            fail("La aplicación no está disponible después de " + maxRetries + " intentos. Último error: " + 
                 (lastException != null ? lastException.getMessage() : "desconocido"));
        }
    }
    
    private void cleanupTestData() {
        try {
            // Buscar órdenes de prueba anteriores y eliminarlas
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/ordenes?sucursal=S001"))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                List<OrdenDTO> ordenesExistentes = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<OrdenDTO>>() {});
                
                // Eliminar órdenes previas de prueba
                for (OrdenDTO orden : ordenesExistentes) {
                    if (orden.getSucursal() != null && orden.getSucursal().equals("S001")) {
                        try {
                            HttpRequest deleteRequest = HttpRequest.newBuilder()
                                    .DELETE()
                                    .uri(URI.create(BASE_URL + "/ordenes/" + orden.getIdOrden()))
                                    .build();
                            
                            httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                            System.out.println("Eliminada orden de prueba anterior: " + orden.getIdOrden());
                        } catch (Exception e) {
                            System.out.println("No se pudo eliminar la orden " + orden.getIdOrden() + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error durante la limpieza de datos: " + e.getMessage());
            // Continuamos con la prueba incluso si hay errores en la limpieza
        }
    }
    
    private void setupTestProduct() throws Exception {
        // Intentar encontrar un producto existente primero
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/productos"))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                List<Object> productos = objectMapper.readValue(responseBody, List.class);
                
                if (!productos.isEmpty()) {
                    // Usar el primer producto disponible
                    Object producto = productos.get(0);
                    productoIdParaTest = Long.valueOf(objectMapper.convertValue(producto, java.util.Map.class).get("idProducto").toString());
                    System.out.println("Usando producto existente con ID: " + productoIdParaTest);
                    
                    // Ahora buscar un producto_precio para este producto
                    setupProductoPrecioParaTest();
                    return;
                }
            }
            
            // Si no hay productos, crear uno
            crearProductoParaTest();
            
        } catch (Exception e) {
            System.out.println("Error al buscar producto para pruebas: " + e.getMessage());
            // Intentar crear uno nuevo
            crearProductoParaTest();
        }
    }
    
    private void crearProductoParaTest() throws Exception {
        // Crear un nuevo producto para las pruebas
        String productoJson = "{\"nombre\":\"Producto para pruebas de sistema\",\"activo\":true,\"observaciones\":\"Creado automáticamente para pruebas\"}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(productoJson))
                .uri(URI.create(BASE_URL + "/productos"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201) {
            // Producto creado correctamente
            Object producto = objectMapper.readValue(response.body(), Object.class);
            productoIdParaTest = Long.valueOf(objectMapper.convertValue(producto, java.util.Map.class).get("idProducto").toString());
            System.out.println("Producto creado con ID: " + productoIdParaTest);
            
            // Crear un precio para este producto
            crearProductoPrecioParaTest();
        } else {
            System.out.println("No se pudo crear producto para pruebas. Usando ID 1 por defecto.");
            productoIdParaTest = 1L;
            setupProductoPrecioParaTest();
        }
    }
    
    private void crearProductoPrecioParaTest() throws Exception {
        // Crear un precio para el producto de prueba
        String preciojson = "{\"idProducto\":" + productoIdParaTest + ",\"fechaDesde\":\"" 
                + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()) 
                + "\",\"precioSugerido\":5.00}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(preciojson))
                .uri(URI.create(BASE_URL + "/productos-precios"))
                .header("Content-Type", "application/json")
                .build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201) {
                // Precio creado correctamente
                System.out.println("Precio creado para el producto de prueba");
            } else {
                System.out.println("No se pudo crear precio. Error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.out.println("Error al crear precio: " + e.getMessage());
            // Continuamos con la prueba
        }
    }
    
    private void setupProductoPrecioParaTest() {
        // Si no podemos crear un precio, asumimos que el ID 1 está disponible
        System.out.println("Usando producto_precio con ID 1 para las pruebas");
    }

    @Test
    void testCicloCompletoCRUD() throws Exception {
        try {
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
        } catch (Exception e) {
            System.err.println("Error en el ciclo de pruebas CRUD: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    void testCrearOrden() throws Exception {
        // Crear los detalles de la orden
        List<OrdenDetalleDTO> detalles = new ArrayList<>();
        OrdenDetalleDTO detalle = new OrdenDetalleDTO();
        detalle.setIdProductoPrecio(1L); // ID por defecto, podríamos buscar uno dinámicamente
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
        System.out.println("Creando orden con: " + requestBody);
        
        // Enviar solicitud POST
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/ordenes"))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta de crear orden: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        if (response.statusCode() != 201) {
            fail("Error al crear orden. Código: " + response.statusCode() + ", Respuesta: " + response.body());
        }
        
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
        // Asegurarse de que tenemos un ID válido
        assertTrue(ordenIdCreada != null && ordenIdCreada > 0, "ID de orden inválido para la prueba");
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta de obtener orden: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        if (response.statusCode() != 200) {
            fail("Error al obtener orden. Código: " + response.statusCode() + ", Respuesta: " + response.body());
        }
        
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
        // Asegurarse de que tenemos un ID válido
        assertTrue(ordenIdCreada != null && ordenIdCreada > 0, "ID de orden inválido para la prueba");
        
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
        System.out.println("Actualizando orden con: " + requestBody);
        
        // Enviar solicitud PUT
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .header("Content-Type", "application/json")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta de actualizar orden: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        if (response.statusCode() != 200) {
            fail("Error al actualizar orden. Código: " + response.statusCode() + ", Respuesta: " + response.body());
        }
        
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
        System.out.println("Respuesta de listar órdenes: " + responseTodas.statusCode() + " - " + responseTodas.body());
        
        // Verificar respuesta
        if (responseTodas.statusCode() != 200) {
            fail("Error al listar órdenes. Código: " + responseTodas.statusCode() + ", Respuesta: " + responseTodas.body());
        }
        
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
        System.out.println("Respuesta de filtrar por sucursal: " + responseSucursal.statusCode() + " - " + responseSucursal.body());
        
        // Verificar respuesta
        if (responseSucursal.statusCode() != 200) {
            fail("Error al filtrar órdenes por sucursal. Código: " + responseSucursal.statusCode() + ", Respuesta: " + responseSucursal.body());
        }
        
        List<OrdenDTO> ordenesPorSucursal = objectMapper.readValue(
                responseSucursal.body(), 
                new TypeReference<List<OrdenDTO>>() {});
        
        assertFalse(ordenesPorSucursal.isEmpty(), "Debería encontrarse al menos una orden con la sucursal buscada");
        assertEquals("S002", ordenesPorSucursal.get(0).getSucursal(), "La sucursal debe coincidir con la búsqueda");
        System.out.println("Órdenes encontradas por sucursal: " + ordenesPorSucursal.size());
    }
    
    void testAnularOrden() throws Exception {
        // Asegurarse de que tenemos un ID válido
        assertTrue(ordenIdCreada != null && ordenIdCreada > 0, "ID de orden inválido para la prueba");
        
        // Enviar solicitud PUT para anular
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada + "/anular"))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta de anular orden: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        if (response.statusCode() != 204) {
            fail("Error al anular orden. Código: " + response.statusCode() + ", Respuesta: " + response.body());
        }
        
        // Verificar que la orden ahora está anulada
        HttpRequest verifyRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> verifyResponse = httpClient.send(verifyRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta de verificar anulación: " + verifyResponse.statusCode() + " - " + verifyResponse.body());
        
        // Verificar respuesta
        if (verifyResponse.statusCode() != 200) {
            fail("Error al verificar anulación. Código: " + verifyResponse.statusCode() + ", Respuesta: " + verifyResponse.body());
        }
        
        OrdenDTO ordenAnulada = objectMapper.readValue(verifyResponse.body(), OrdenDTO.class);
        assertTrue(ordenAnulada.getAnulada(), "La orden debería estar marcada como anulada");
        
        System.out.println("Orden anulada correctamente");
    }
    
    void testEliminarOrden() throws Exception {
        // Asegurarse de que tenemos un ID válido
        assertTrue(ordenIdCreada != null && ordenIdCreada > 0, "ID de orden inválido para la prueba");
        
        // Enviar solicitud DELETE
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta de eliminar orden: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        if (response.statusCode() != 204) {
            fail("Error al eliminar orden. Código: " + response.statusCode() + ", Respuesta: " + response.body());
        }
        
        System.out.println("Orden eliminada correctamente");
    }
    
    void testOrdenNoExisteDespuesDeEliminar() throws Exception {
        // Asegurarse de que tenemos un ID válido
        assertTrue(ordenIdCreada != null && ordenIdCreada > 0, "ID de orden inválido para la prueba");
        
        // Enviar solicitud GET
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/ordenes/" + ordenIdCreada))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta de verificar eliminación: " + response.statusCode() + " - " + response.body());
        
        // Verificar respuesta
        assertEquals(404, response.statusCode(), "La orden no debería existir después de ser eliminada");
        
        System.out.println("Verificado: la orden ya no existe después de eliminar");
    }
}