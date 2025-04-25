package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.Producto;
import sv.edu.ues.fmocc.tpi135.repository.ProductoRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.ProductoServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class ProductoSystemTest {

    // Configuración del contenedor PostgreSQL
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("tipicos_test")
            .withUsername("postgres")
            .withPassword("1234");

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static ProductoRepositoryImpl productoRepository;
    private static ProductoServiceImpl productoService;

    @BeforeAll
    public static void setUp() {
        // Iniciar el contenedor
        postgres.start();

        // Configurar propiedades para la conexión
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", postgres.getJdbcUrl());
        properties.put("javax.persistence.jdbc.user", postgres.getUsername());
        properties.put("javax.persistence.jdbc.password", postgres.getPassword());
        properties.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        
        // Crear tablas automáticamente
        properties.put("javax.persistence.schema-generation.database.action", "create");
        
        // Crear EntityManagerFactory
        emf = Persistence.createEntityManagerFactory("TipicosPUTest", properties);
        em = emf.createEntityManager();
        
        // Inicializar repositorio y servicio
        productoRepository = new ProductoRepositoryImpl();
        
        // Inyectar EntityManager en el repositorio usando reflexión
        try {
            java.lang.reflect.Field emField = ProductoRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(productoRepository, em);
        } catch (Exception e) {
            fail("Error al inyectar EntityManager: " + e.getMessage());
        }
        
        productoService = new ProductoServiceImpl();
        
        // Inyectar repositorio en el servicio usando reflexión
        try {
            java.lang.reflect.Field repoField = ProductoServiceImpl.class.getDeclaredField("productoRepository");
            repoField.setAccessible(true);
            repoField.set(productoService, productoRepository);
        } catch (Exception e) {
            fail("Error al inyectar repositorio: " + e.getMessage());
        }
        
        // Insertar datos iniciales para las pruebas
        em.getTransaction().begin();
        
        // Crear y persistir un producto
        Producto producto = new Producto();
        producto.setNombre("Pupusa de Queso");
        producto.setActivo(true);
        producto.setObservaciones("Pupusa tradicional de queso");
        
        em.persist(producto);
        em.getTransaction().commit();
    }

    @Test
    public void testCrearProducto() {
        // Crear un DTO para un nuevo producto
        ProductoDTO nuevoProducto = new ProductoDTO();
        nuevoProducto.setNombre("Yuca Frita");
        nuevoProducto.setActivo(true);
        nuevoProducto.setObservaciones("Yuca frita con curtido y salsa de tomate");
        
        // Iniciar una transacción
        em.getTransaction().begin();
        
        // Intentar crear el producto
        ProductoDTO productoCreado = productoService.crearProducto(nuevoProducto);
        
        // Confirmar la transacción
        em.getTransaction().commit();
        
        // Verificar que se haya creado correctamente
        assertNotNull(productoCreado);
        assertNotNull(productoCreado.getIdProducto());
        assertEquals("Yuca Frita", productoCreado.getNombre());
        
        // Verificar que se pueda recuperar
        Optional<ProductoDTO> recuperado = productoService.obtenerProductoPorId(productoCreado.getIdProducto());
        assertTrue(recuperado.isPresent());
        assertEquals("Yuca Frita", recuperado.get().getNombre());
    }

    @Test
    public void testActualizarProducto() {
        // Crear un producto para actualizar
        ProductoDTO nuevoProducto = new ProductoDTO();
        nuevoProducto.setNombre("Empanada");
        nuevoProducto.setActivo(true);
        nuevoProducto.setObservaciones("Empanada de plátano");
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el producto
        ProductoDTO productoCreado = productoService.crearProducto(nuevoProducto);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Preparar la actualización
        ProductoDTO actualizacion = new ProductoDTO();
        actualizacion.setNombre("Empanada Dulce");
        actualizacion.setObservaciones("Empanada de plátano con leche y azúcar");
        
        // Iniciar nueva transacción
        em.getTransaction().begin();
        
        // Actualizar el producto
        ProductoDTO actualizado = productoService.actualizarProducto(productoCreado.getIdProducto(), actualizacion);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar la actualización
        assertNotNull(actualizado);
        assertEquals("Empanada Dulce", actualizado.getNombre());
        assertEquals("Empanada de plátano con leche y azúcar", actualizado.getObservaciones());
        
        // Verificar que se pueda recuperar con los nuevos datos
        Optional<ProductoDTO> recuperado = productoService.obtenerProductoPorId(productoCreado.getIdProducto());
        assertTrue(recuperado.isPresent());
        assertEquals("Empanada Dulce", recuperado.get().getNombre());
    }

    @Test
    public void testEliminarProducto() {
        // Crear un producto para eliminar
        ProductoDTO nuevoProducto = new ProductoDTO();
        nuevoProducto.setNombre("Producto Temporal");
        nuevoProducto.setActivo(true);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el producto
        ProductoDTO productoCreado = productoService.crearProducto(nuevoProducto);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que exista
        Optional<ProductoDTO> antes = productoService.obtenerProductoPorId(productoCreado.getIdProducto());
        assertTrue(antes.isPresent());
        
        // Iniciar transacción para eliminar
        em.getTransaction().begin();
        
        // Eliminar el producto
        boolean eliminado = productoService.eliminarProducto(productoCreado.getIdProducto());
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que se haya eliminado
        assertTrue(eliminado);
        
        // Verificar que ya no existe
        Optional<ProductoDTO> despues = productoService.obtenerProductoPorId(productoCreado.getIdProducto());
        assertFalse(despues.isPresent());
    }

    @Test
    public void testListarProductos() {
        // Crear varios productos
        ProductoDTO producto1 = new ProductoDTO();
        producto1.setNombre("Tamal");
        producto1.setActivo(true);
        
        ProductoDTO producto2 = new ProductoDTO();
        producto2.setNombre("Enchilada");
        producto2.setActivo(true);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear los productos
        productoService.crearProducto(producto1);
        productoService.crearProducto(producto2);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Listar productos
        List<ProductoDTO> productos = productoService.listarProductos();
        
        // Verificar que hay al menos los que acabamos de crear
        assertNotNull(productos);
        assertTrue(productos.size() >= 2);
        
        // Verificar que se encuentren nuestros productos creados
        boolean encontroTamal = false;
        boolean encontroEnchilada = false;
        
        for (ProductoDTO p : productos) {
            if ("Tamal".equals(p.getNombre())) {
                encontroTamal = true;
            } else if ("Enchilada".equals(p.getNombre())) {
                encontroEnchilada = true;
            }
        }
        
        assertTrue(encontroTamal, "No se encontró el producto 'Tamal'");
        assertTrue(encontroEnchilada, "No se encontró el producto 'Enchilada'");
    }
}