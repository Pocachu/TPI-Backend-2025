package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;
import sv.edu.ues.fmocc.tpi135.entity.TipoProducto;
import sv.edu.ues.fmocc.tpi135.repository.TipoProductoRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.TipoProductoServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class TipoProductoSystem {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("tipicos_test")
            .withUsername("postgres")
            .withPassword("1234");

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static TipoProductoRepositoryImpl tipoProductoRepository;
    private static TipoProductoServiceImpl tipoProductoService;

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
        tipoProductoRepository = new TipoProductoRepositoryImpl();
        
        // Inyectar EntityManager en el repositorio usando reflexión
        try {
            java.lang.reflect.Field emField = TipoProductoRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(tipoProductoRepository, em);
        } catch (Exception e) {
            fail("Error al inyectar EntityManager: " + e.getMessage());
        }
        
        tipoProductoService = new TipoProductoServiceImpl();
        
        // Inyectar repositorio en el servicio usando reflexión
        try {
            java.lang.reflect.Field repoField = TipoProductoServiceImpl.class.getDeclaredField("tipoProductoRepository");
            repoField.setAccessible(true);
            repoField.set(tipoProductoService, tipoProductoRepository);
        } catch (Exception e) {
            fail("Error al inyectar repositorio: " + e.getMessage());
        }
        
        // Insertar datos iniciales para las pruebas
        em.getTransaction().begin();
        
        // Crear y persistir tipos de productos iniciales
        TipoProducto tipo1 = new TipoProducto();
        tipo1.setNombre("bebida");
        tipo1.setActivo(true);
        
        TipoProducto tipo2 = new TipoProducto();
        tipo2.setNombre("comida");
        tipo2.setActivo(true);
        
        TipoProducto tipo3 = new TipoProducto();
        tipo3.setNombre("tipicos");
        tipo3.setActivo(true);
        
        em.persist(tipo1);
        em.persist(tipo2);
        em.persist(tipo3);
        
        em.getTransaction().commit();
    }

    @Test
    public void testCrearTipoProducto() {
        // Crear un DTO para un nuevo tipo de producto
        TipoProductoDTO nuevoTipo = new TipoProductoDTO();
        nuevoTipo.setNombre("postre");
        nuevoTipo.setActivo(true);
        nuevoTipo.setObservaciones("Productos dulces para después de la comida");
        
        // Iniciar una transacción
        em.getTransaction().begin();
        
        // Intentar crear el tipo de producto
        TipoProductoDTO tipoCreado = tipoProductoService.crearTipoProducto(nuevoTipo);
        
        // Confirmar la transacción
        em.getTransaction().commit();
        
        // Verificar que se haya creado correctamente
        assertNotNull(tipoCreado);
        assertNotNull(tipoCreado.getIdTipoProducto());
        assertEquals("postre", tipoCreado.getNombre());
        
        // Verificar que se pueda recuperar
        Optional<TipoProductoDTO> recuperado = tipoProductoService.obtenerTipoProductoPorId(tipoCreado.getIdTipoProducto());
        assertTrue(recuperado.isPresent());
        assertEquals("postre", recuperado.get().getNombre());
    }

    @Test
    public void testActualizarTipoProducto() {
        // Crear un tipo de producto para actualizar
        TipoProductoDTO nuevoTipo = new TipoProductoDTO();
        nuevoTipo.setNombre("entrada");
        nuevoTipo.setActivo(true);
        nuevoTipo.setObservaciones("Platos para iniciar la comida");
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el tipo de producto
        TipoProductoDTO tipoCreado = tipoProductoService.crearTipoProducto(nuevoTipo);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Preparar la actualización
        TipoProductoDTO actualizacion = new TipoProductoDTO();
        actualizacion.setNombre("aperitivo");
        actualizacion.setObservaciones("Platos pequeños para iniciar la comida");
        
        // Iniciar nueva transacción
        em.getTransaction().begin();
        
        // Actualizar el tipo de producto
        TipoProductoDTO actualizado = tipoProductoService.actualizarTipoProducto(tipoCreado.getIdTipoProducto(), actualizacion);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar la actualización
        assertNotNull(actualizado);
        assertEquals("aperitivo", actualizado.getNombre());
        assertEquals("Platos pequeños para iniciar la comida", actualizado.getObservaciones());
        
        // Verificar que se pueda recuperar con los nuevos datos
        Optional<TipoProductoDTO> recuperado = tipoProductoService.obtenerTipoProductoPorId(tipoCreado.getIdTipoProducto());
        assertTrue(recuperado.isPresent());
        assertEquals("aperitivo", recuperado.get().getNombre());
    }

    @Test
    public void testEliminarTipoProducto() {
        // Crear un tipo de producto para eliminar
        TipoProductoDTO nuevoTipo = new TipoProductoDTO();
        nuevoTipo.setNombre("tipo temporal");
        nuevoTipo.setActivo(true);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el tipo de producto
        TipoProductoDTO tipoCreado = tipoProductoService.crearTipoProducto(nuevoTipo);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que exista
        Optional<TipoProductoDTO> antes = tipoProductoService.obtenerTipoProductoPorId(tipoCreado.getIdTipoProducto());
        assertTrue(antes.isPresent());
        
        // Iniciar transacción para eliminar
        em.getTransaction().begin();
        
        // Eliminar el tipo de producto
        boolean eliminado = tipoProductoService.eliminarTipoProducto(tipoCreado.getIdTipoProducto());
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que se haya eliminado
        assertTrue(eliminado);
        
        // Verificar que ya no existe
        Optional<TipoProductoDTO> despues = tipoProductoService.obtenerTipoProductoPorId(tipoCreado.getIdTipoProducto());
        assertFalse(despues.isPresent());
    }

    @Test
    public void testListarTiposProductos() {
        // Listar tipos de productos (ya insertamos 3 en el setUp)
        List<TipoProductoDTO> tipos = tipoProductoService.listarTiposProductos();
        
        // Verificar que hay al menos 3 tipos (los iniciales)
        assertNotNull(tipos);
        assertTrue(tipos.size() >= 3);
        
        // Verificar que se encuentren nuestros tipos iniciales
        boolean encontroBebida = false;
        boolean encontroComida = false;
        boolean encontroTipicos = false;
        
        for (TipoProductoDTO t : tipos) {
            switch (t.getNombre()) {
                case "bebida":
                    encontroBebida = true;
                    break;
                case "comida":
                    encontroComida = true;
                    break;
                case "tipicos":
                    encontroTipicos = true;
                    break;
            }
        }
        
        assertTrue(encontroBebida, "No se encontró el tipo 'bebida'");
        assertTrue(encontroComida, "No se encontró el tipo 'comida'");
        assertTrue(encontroTipicos, "No se encontró el tipo 'tipicos'");
    }
}