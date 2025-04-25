package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;
import sv.edu.ues.fmocc.tpi135.dto.PagoDetalleDTO;
import sv.edu.ues.fmocc.tpi135.entity.Orden;
import sv.edu.ues.fmocc.tpi135.entity.Pago;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepository;
import sv.edu.ues.fmocc.tpi135.repository.OrdenRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.repository.PagoDetalleRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoDetalleRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.repository.PagoRepository;
import sv.edu.ues.fmocc.tpi135.repository.PagoRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.PagoServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class PagoSystemTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("tipicos_test")
            .withUsername("postgres")
            .withPassword("1234");

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static PagoRepositoryImpl pagoRepository;
    private static PagoDetalleRepositoryImpl pagoDetalleRepository;
    private static OrdenRepositoryImpl ordenRepository;
    private static PagoServiceImpl pagoService;
    private static Long idOrden;

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
        pagoRepository = new PagoRepositoryImpl();
        pagoDetalleRepository = new PagoDetalleRepositoryImpl();
        ordenRepository = new OrdenRepositoryImpl();
        
        // Inyectar EntityManager en los repositorios usando reflexión
        try {
            java.lang.reflect.Field emField = PagoRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(pagoRepository, em);
            
            emField = PagoDetalleRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(pagoDetalleRepository, em);
            
            emField = OrdenRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(ordenRepository, em);
        } catch (Exception e) {
            fail("Error al inyectar EntityManager: " + e.getMessage());
        }
        
        pagoService = new PagoServiceImpl();
        
        // Inyectar repositorios en el servicio usando reflexión
        try {
            java.lang.reflect.Field repoField = PagoServiceImpl.class.getDeclaredField("pagoRepository");
            repoField.setAccessible(true);
            repoField.set(pagoService, pagoRepository);
            
            repoField = PagoServiceImpl.class.getDeclaredField("pagoDetalleRepository");
            repoField.setAccessible(true);
            repoField.set(pagoService, pagoDetalleRepository);
            
            repoField = PagoServiceImpl.class.getDeclaredField("ordenRepository");
            repoField.setAccessible(true);
            repoField.set(pagoService, ordenRepository);
        } catch (Exception e) {
            fail("Error al inyectar repositorios: " + e.getMessage());
        }
        
        // Insertar datos iniciales para las pruebas
        em.getTransaction().begin();
        
        // Crear y persistir una orden
        Orden orden = new Orden();
        orden.setFecha(new Date());
        orden.setSucursal("S001");
        orden.setAnulada(false);
        em.persist(orden);
        
        // Guardar el ID de la orden para usarlo en las pruebas
        idOrden = orden.getIdOrden();
        
        // Crear y persistir un pago inicial
        Pago pago = new Pago();
        pago.setIdOrden(idOrden);
        pago.setFecha(new Date());
        pago.setMetodoPago("EFECTIVO");
        pago.setReferencia("Pago inicial de prueba");
        em.persist(pago);
        
        em.getTransaction().commit();
    }

    @Test
    public void testCrearPago() {
        // Crear un DTO para un nuevo pago
        PagoDTO nuevoPago = new PagoDTO();
        nuevoPago.setIdOrden(idOrden);
        nuevoPago.setFecha(new Date());
        nuevoPago.setMetodoPago("TARJETA");
        nuevoPago.setReferencia("Pago con tarjeta de crédito");
        
        // Crear detalle del pago
        PagoDetalleDTO detalle = new PagoDetalleDTO();
        detalle.setMonto(new BigDecimal("25.00"));
        detalle.setObservaciones("Pago con tarjeta Visa");
        
        nuevoPago.setDetalles(Collections.singletonList(detalle));
        
        // Iniciar una transacción
        em.getTransaction().begin();
        
        // Intentar crear el pago
        PagoDTO pagoCreado = pagoService.crearPago(nuevoPago);
        
        // Confirmar la transacción
        em.getTransaction().commit();
        
        // Verificar que se haya creado correctamente
        assertNotNull(pagoCreado);
        assertNotNull(pagoCreado.getIdPago());
        assertEquals("TARJETA", pagoCreado.getMetodoPago());
        
        // Verificar que se pueda recuperar
        Optional<PagoDTO> recuperado = pagoService.obtenerPagoPorId(pagoCreado.getIdPago());
        assertTrue(recuperado.isPresent());
        assertEquals("TARJETA", recuperado.get().getMetodoPago());
        
        // Verificar que se hayan creado los detalles
        assertNotNull(recuperado.get().getDetalles());
        assertFalse(recuperado.get().getDetalles().isEmpty());
        assertEquals(new BigDecimal("25.00"), recuperado.get().getDetalles().get(0).getMonto().stripTrailingZeros());
        assertEquals("Pago con tarjeta Visa", recuperado.get().getDetalles().get(0).getObservaciones());
    }

    @Test
    public void testActualizarPago() {
        // Crear un pago para actualizar
        PagoDTO nuevoPago = new PagoDTO();
        nuevoPago.setIdOrden(idOrden);
        nuevoPago.setFecha(new Date());
        nuevoPago.setMetodoPago("EFECTIVO");
        nuevoPago.setReferencia("Pago inicial");
        
        // Crear detalle inicial
        PagoDetalleDTO detalleInicial = new PagoDetalleDTO();
        detalleInicial.setMonto(new BigDecimal("15.00"));
        detalleInicial.setObservaciones("Pago parcial");
        
        nuevoPago.setDetalles(Collections.singletonList(detalleInicial));
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el pago
        PagoDTO pagoCreado = pagoService.crearPago(nuevoPago);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Preparar la actualización
        PagoDTO actualizacion = new PagoDTO();
        actualizacion.setMetodoPago("TRANSFERENCIA");
        actualizacion.setReferencia("Pago actualizado por transferencia");
        
        // Crear nuevo detalle para reemplazar el anterior
        PagoDetalleDTO nuevoDetalle = new PagoDetalleDTO();
        nuevoDetalle.setMonto(new BigDecimal("20.00"));
        nuevoDetalle.setObservaciones("Pago completo por transferencia");
        
        actualizacion.setDetalles(Collections.singletonList(nuevoDetalle));
        
        // Iniciar nueva transacción
        em.getTransaction().begin();
        
        // Actualizar el pago
        PagoDTO actualizado = pagoService.actualizarPago(pagoCreado.getIdPago(), actualizacion);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar la actualización
        assertNotNull(actualizado);
        assertEquals("TRANSFERENCIA", actualizado.getMetodoPago());
        assertEquals("Pago actualizado por transferencia", actualizado.getReferencia());
        
        // Verificar que se pueda recuperar con los nuevos datos
        Optional<PagoDTO> recuperado = pagoService.obtenerPagoPorId(pagoCreado.getIdPago());
        assertTrue(recuperado.isPresent());
        assertEquals("TRANSFERENCIA", recuperado.get().getMetodoPago());
        
        // Verificar que se hayan actualizado los detalles
        assertNotNull(recuperado.get().getDetalles());
        assertFalse(recuperado.get().getDetalles().isEmpty());
        assertEquals(new BigDecimal("20.00"), recuperado.get().getDetalles().get(0).getMonto().stripTrailingZeros());
        assertEquals("Pago completo por transferencia", recuperado.get().getDetalles().get(0).getObservaciones());
    }

    @Test
    public void testEliminarPago() {
        // Crear un pago para eliminar
        PagoDTO nuevoPago = new PagoDTO();
        nuevoPago.setIdOrden(idOrden);
        nuevoPago.setFecha(new Date());
        nuevoPago.setMetodoPago("EFECTIVO");
        nuevoPago.setReferencia("Pago para eliminar");
        
        // Crear detalle del pago
        PagoDetalleDTO detalle = new PagoDetalleDTO();
        detalle.setMonto(new BigDecimal("10.00"));
        
        nuevoPago.setDetalles(Collections.singletonList(detalle));
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el pago
        PagoDTO pagoCreado = pagoService.crearPago(nuevoPago);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que exista
        Optional<PagoDTO> antes = pagoService.obtenerPagoPorId(pagoCreado.getIdPago());
        assertTrue(antes.isPresent());
        
        // Iniciar transacción para eliminar
        em.getTransaction().begin();
        
        // Eliminar el pago
        boolean eliminado = pagoService.eliminarPago(pagoCreado.getIdPago());
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que se haya eliminado
        assertTrue(eliminado);
        
        // Verificar que ya no existe
        Optional<PagoDTO> despues = pagoService.obtenerPagoPorId(pagoCreado.getIdPago());
        assertFalse(despues.isPresent());
    }

    @Test
    public void testBuscarPagosPorIdOrden() {
        // Crear varios pagos para la misma orden
        PagoDTO pago1 = new PagoDTO();
        pago1.setIdOrden(idOrden);
        pago1.setFecha(new Date());
        pago1.setMetodoPago("EFECTIVO");
        pago1.setReferencia("Primer pago");
        
        PagoDTO pago2 = new PagoDTO();
        pago2.setIdOrden(idOrden);
        pago2.setFecha(new Date());
        pago2.setMetodoPago("TARJETA");
        pago2.setReferencia("Segundo pago");
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear los pagos
        pagoService.crearPago(pago1);
        pagoService.crearPago(pago2);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Buscar pagos por ID de orden
        List<PagoDTO> resultado = pagoService.buscarPagosPorIdOrden(idOrden);
        
        // Verificar resultados
        assertNotNull(resultado);
        assertTrue(resultado.size() >= 2);
        
        // Verificar que solo se encontraron pagos para la orden especificada
        for (PagoDTO p : resultado) {
            assertEquals(idOrden, p.getIdOrden(),
                    "Se encontró un pago para una orden diferente: " + p.getIdOrden());
        }
    }

    @Test
    public void testBuscarPagosPorMetodoPago() {
        // Crear pagos con diferentes métodos de pago
        PagoDTO pagoEfectivo = new PagoDTO();
        pagoEfectivo.setIdOrden(idOrden);
        pagoEfectivo.setFecha(new Date());
        pagoEfectivo.setMetodoPago("EFECTIVO");
        
        PagoDTO pagoTarjeta = new PagoDTO();
        pagoTarjeta.setIdOrden(idOrden);
        pagoTarjeta.setFecha(new Date());
        pagoTarjeta.setMetodoPago("TARJETA");
        
        PagoDTO pagoTransferencia = new PagoDTO();
        pagoTransferencia.setIdOrden(idOrden);
        pagoTransferencia.setFecha(new Date());
        pagoTransferencia.setMetodoPago("TRANSFERENCIA");
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear los pagos
        pagoService.crearPago(pagoEfectivo);
        pagoService.crearPago(pagoTarjeta);
        pagoService.crearPago(pagoTransferencia);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Buscar pagos por método de pago
        List<PagoDTO> resultadoEfectivo = pagoService.buscarPagosPorMetodoPago("EFECTIVO");
        List<PagoDTO> resultadoTarjeta = pagoService.buscarPagosPorMetodoPago("TARJETA");
        List<PagoDTO> resultadoTransferencia = pagoService.buscarPagosPorMetodoPago("TRANSFERENCIA");
        
        // Verificar resultados
        assertNotNull(resultadoEfectivo);
        assertNotNull(resultadoTarjeta);
        assertNotNull(resultadoTransferencia);
        
        assertTrue(resultadoEfectivo.size() >= 1);
        assertTrue(resultadoTarjeta.size() >= 1);
        assertTrue(resultadoTransferencia.size() >= 1);
        
        // Verificar que solo se encontraron pagos con el método correspondiente
        for (PagoDTO p : resultadoEfectivo) {
            assertEquals("EFECTIVO", p.getMetodoPago(),
                    "Se encontró un pago con método diferente a 'EFECTIVO': " + p.getMetodoPago());
        }
        
        for (PagoDTO p : resultadoTarjeta) {
            assertEquals("TARJETA", p.getMetodoPago(),
                    "Se encontró un pago con método diferente a 'TARJETA': " + p.getMetodoPago());
        }
        
        for (PagoDTO p : resultadoTransferencia) {
            assertEquals("TRANSFERENCIA", p.getMetodoPago(),
                    "Se encontró un pago con método diferente a 'TRANSFERENCIA': " + p.getMetodoPago());
        }
    }
}