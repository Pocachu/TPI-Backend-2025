package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDetalleDTO;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.dto.ProductoPrecioDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.Producto;
import sv.edu.ues.fmocc.tpi135.entity.ProductoPrecio;
import sv.edu.ues.fmocc.tpi135.repository.OrdenDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.OrdenDetalleRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.repository.ProductoPrecioRepository;
import sv.edu.ues.fmocc.tpi135.repository.ProductoPrecioRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.OrdenServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class OrdenSystemTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("tipicos_test")
            .withUsername("postgres")
            .withPassword("1234");

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static OrdenRepositoryImpl ordenRepository;
    private static OrdenDetalleRepositoryImpl ordenDetalleRepository;
    private static ProductoPrecioRepositoryImpl productoPrecioRepository;
    private static OrdenServiceImpl ordenService;
    private static Long idProductoPrecio;

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
        
        // Inicializar repositorios y servicio
        ordenRepository = new OrdenRepositoryImpl();
        ordenDetalleRepository = new OrdenDetalleRepositoryImpl();
        productoPrecioRepository = new ProductoPrecioRepositoryImpl();
        
        // Inyectar EntityManager en los repositorios usando reflexión
        try {
            java.lang.reflect.Field emField = OrdenRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(ordenRepository, em);
            
            emField = OrdenDetalleRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(ordenDetalleRepository, em);
            
            emField = ProductoPrecioRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(productoPrecioRepository, em);
        } catch (Exception e) {
            fail("Error al inyectar EntityManager: " + e.getMessage());
        }
        
        ordenService = new OrdenServiceImpl();
        
        // Inyectar repositorios en el servicio usando reflexión
        try {
            java.lang.reflect.Field repoField = OrdenServiceImpl.class.getDeclaredField("ordenRepository");
            repoField.setAccessible(true);
            repoField.set(ordenService, ordenRepository);
            
            repoField = OrdenServiceImpl.class.getDeclaredField("ordenDetalleRepository");
            repoField.setAccessible(true);
            repoField.set(ordenService, ordenDetalleRepository);
            
            repoField = OrdenServiceImpl.class.getDeclaredField("productoPrecioRepository");
            repoField.setAccessible(true);
            repoField.set(ordenService, productoPrecioRepository);
        } catch (Exception e) {
            fail("Error al inyectar repositorios: " + e.getMessage());
        }
        
        // Insertar datos iniciales para las pruebas
        em.getTransaction().begin();
        
        // Crear y persistir un producto
        Producto producto = new Producto();
        producto.setNombre("Pupusa de Queso");
        producto.setActivo(true);
        producto.setObservaciones("Pupusa tradicional de queso");
        em.persist(producto);
        
        // Crear y persistir un precio para el producto
        ProductoPrecio productoPrecio = new ProductoPrecio();
        productoPrecio.setIdProducto(producto.getIdProducto());
        productoPrecio.setFechaDesde(new Date());
        productoPrecio.setPrecioSugerido(new BigDecimal("1.00"));
        em.persist(productoPrecio);
        
        // Guardar el ID del precio del producto para usarlo en las pruebas
        idProductoPrecio = productoPrecio.getIdProductoPrecio();
        
        // Crear y persistir una orden con su detalle
        Orden orden = new Orden();
        orden.setFecha(new Date());
        orden.setSucursal("S001");
        orden.setAnulada(false);
        em.persist(orden);
        
        em.getTransaction().commit();
    }

    @Test
    public void testCrearOrden() {
        // Crear un DTO para una nueva orden
        OrdenDTO nuevaOrden = new OrdenDTO();
        nuevaOrden.setFecha(new Date());
        nuevaOrden.setSucursal("S002");
        nuevaOrden.setAnulada(false);
        
        // Crear detalle de la orden
        OrdenDetalleDTO detalle = new OrdenDetalleDTO();
        detalle.setIdProductoPrecio(idProductoPrecio);
        detalle.setCantidad(2);
        detalle.setPrecio(new BigDecimal("1.00"));
        detalle.setObservaciones("Sin curtido");
        
        nuevaOrden.setDetalles(Collections.singletonList(detalle));
        
        // Iniciar una transacción
        em.getTransaction().begin();
        
        // Intentar crear la orden
        OrdenDTO ordenCreada = ordenService.crearOrden(nuevaOrden);
        
        // Confirmar la transacción
        em.getTransaction().commit();
        
        // Verificar que se haya creado correctamente
        assertNotNull(ordenCreada);
        assertNotNull(ordenCreada.getIdOrden());
        assertEquals("S002", ordenCreada.getSucursal());
        
        // Verificar que se pueda recuperar
        Optional<OrdenDTO> recuperada = ordenService.obtenerOrdenPorId(ordenCreada.getIdOrden());
        assertTrue(recuperada.isPresent());
        assertEquals("S002", recuperada.get().getSucursal());
        
        // Verificar que se hayan creado los detalles
        assertNotNull(recuperada.get().getDetalles());
        assertFalse(recuperada.get().getDetalles().isEmpty());
        assertEquals(idProductoPrecio, recuperada.get().getDetalles().get(0).getIdProductoPrecio());
        assertEquals(2, recuperada.get().getDetalles().get(0).getCantidad());
        assertEquals("Sin curtido", recuperada.get().getDetalles().get(0).getObservaciones());
    }

    @Test
    public void testActualizarOrden() {
        // Crear una orden para actualizar
        OrdenDTO nuevaOrden = new OrdenDTO();
        nuevaOrden.setFecha(new Date());
        nuevaOrden.setSucursal("S003");
        nuevaOrden.setAnulada(false);
        
        // Crear detalle inicial
        OrdenDetalleDTO detalleInicial = new OrdenDetalleDTO();
        detalleInicial.setIdProductoPrecio(idProductoPrecio);
        detalleInicial.setCantidad(1);
        detalleInicial.setPrecio(new BigDecimal("1.00"));
        
        nuevaOrden.setDetalles(Collections.singletonList(detalleInicial));
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear la orden
        OrdenDTO ordenCreada = ordenService.crearOrden(nuevaOrden);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Preparar la actualización
        OrdenDTO actualizacion = new OrdenDTO();
        actualizacion.setSucursal("S003-mod");
        
        // Crear nuevo detalle para reemplazar el anterior
        OrdenDetalleDTO nuevoDetalle = new OrdenDetalleDTO();
        nuevoDetalle.setIdProductoPrecio(idProductoPrecio);
        nuevoDetalle.setCantidad(3);
        nuevoDetalle.setPrecio(new BigDecimal("1.00"));
        nuevoDetalle.setObservaciones("Con extra de queso");
        
        actualizacion.setDetalles(Collections.singletonList(nuevoDetalle));
        
        // Iniciar nueva transacción
        em.getTransaction().begin();
        
        // Actualizar la orden
        OrdenDTO actualizada = ordenService.actualizarOrden(ordenCreada.getIdOrden(), actualizacion);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar la actualización
        assertNotNull(actualizada);
        assertEquals("S003-mod", actualizada.getSucursal());
        
        // Verificar que se pueda recuperar con los nuevos datos
        Optional<OrdenDTO> recuperada = ordenService.obtenerOrdenPorId(ordenCreada.getIdOrden());
        assertTrue(recuperada.isPresent());
        assertEquals("S003-mod", recuperada.get().getSucursal());
        
        // Verificar que se hayan actualizado los detalles
        assertNotNull(recuperada.get().getDetalles());
        assertFalse(recuperada.get().getDetalles().isEmpty());
        assertEquals(3, recuperada.get().getDetalles().get(0).getCantidad());
        assertEquals("Con extra de queso", recuperada.get().getDetalles().get(0).getObservaciones());
    }

    @Test
    public void testAnularOrden() {
        // Crear una orden para anular
        OrdenDTO nuevaOrden = new OrdenDTO();
        nuevaOrden.setFecha(new Date());
        nuevaOrden.setSucursal("S004");
        nuevaOrden.setAnulada(false);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear la orden
        OrdenDTO ordenCreada = ordenService.crearOrden(nuevaOrden);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que la orden no está anulada
        Optional<OrdenDTO> antesDeAnular = ordenService.obtenerOrdenPorId(ordenCreada.getIdOrden());
        assertTrue(antesDeAnular.isPresent());
        assertFalse(antesDeAnular.get().getAnulada());
        
        // Iniciar transacción para anular
        em.getTransaction().begin();
        
        // Anular la orden
        boolean anulada = ordenService.anularOrden(ordenCreada.getIdOrden());
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que se haya anulado
        assertTrue(anulada);
        
        // Verificar que ahora está anulada
        Optional<OrdenDTO> despuesDeAnular = ordenService.obtenerOrdenPorId(ordenCreada.getIdOrden());
        assertTrue(despuesDeAnular.isPresent());
        assertTrue(despuesDeAnular.get().getAnulada());
    }

    @Test
    public void testEliminarOrden() {
        // Crear una orden para eliminar
        OrdenDTO nuevaOrden = new OrdenDTO();
        nuevaOrden.setFecha(new Date());
        nuevaOrden.setSucursal("S005");
        nuevaOrden.setAnulada(false);
        
        // Crear detalle de la orden
        OrdenDetalleDTO detalle = new OrdenDetalleDTO();
        detalle.setIdProductoPrecio(idProductoPrecio);
        detalle.setCantidad(1);
        
        nuevaOrden.setDetalles(Collections.singletonList(detalle));
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear la orden
        OrdenDTO ordenCreada = ordenService.crearOrden(nuevaOrden);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que exista
        Optional<OrdenDTO> antes = ordenService.obtenerOrdenPorId(ordenCreada.getIdOrden());
        assertTrue(antes.isPresent());
        
        // Iniciar transacción para eliminar
        em.getTransaction().begin();
        
        // Eliminar la orden
        boolean eliminada = ordenService.eliminarOrden(ordenCreada.getIdOrden());
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que se haya eliminado
        assertTrue(eliminada);
        
        // Verificar que ya no existe
        Optional<OrdenDTO> despues = ordenService.obtenerOrdenPorId(ordenCreada.getIdOrden());
        assertFalse(despues.isPresent());
    }

    @Test
    public void testBuscarOrdenesPorSucursal() {
        // Crear varias órdenes con diferentes sucursales
        OrdenDTO orden1 = new OrdenDTO();
        orden1.setFecha(new Date());
        orden1.setSucursal("SMG");
        orden1.setAnulada(false);
        
        OrdenDTO orden2 = new OrdenDTO();
        orden2.setFecha(new Date());
        orden2.setSucursal("SMG");
        orden2.setAnulada(false);
        
        OrdenDTO orden3 = new OrdenDTO();
        orden3.setFecha(new Date());
        orden3.setSucursal("SAN");
        orden3.setAnulada(false);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear las órdenes
        ordenService.crearOrden(orden1);
        ordenService.crearOrden(orden2);
        ordenService.crearOrden(orden3);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Buscar órdenes por sucursal
        List<OrdenDTO> resultado = ordenService.buscarOrdenesPorSucursal("SMG");
        
        // Verificar resultados
        assertNotNull(resultado);
        assertTrue(resultado.size() >= 2);
        
        // Verificar que solo se encontraron órdenes de la sucursal "SMG"
        for (OrdenDTO o : resultado) {
            assertEquals("SMG", o.getSucursal(), 
                    "Se encontró una orden con sucursal diferente a 'SMG': " + o.getSucursal());
        }
    }
}