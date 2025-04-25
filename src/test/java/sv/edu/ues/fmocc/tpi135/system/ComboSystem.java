package sv.edu.ues.fmocc.tpi135.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;
import sv.edu.ues.fmocc.tpi135.entity.Combo;
import sv.edu.ues.fmocc.tpi135.repository.ComboRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.ComboServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class ComboSystem {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("tipicos_test")
            .withUsername("postgres")
            .withPassword("1234");

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static ComboRepositoryImpl comboRepository;
    private static ComboServiceImpl comboService;

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
        comboRepository = new ComboRepositoryImpl();
        
        // Inyectar EntityManager en el repositorio usando reflexión
        try {
            java.lang.reflect.Field emField = ComboRepositoryImpl.class.getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(comboRepository, em);
        } catch (Exception e) {
            fail("Error al inyectar EntityManager: " + e.getMessage());
        }
        
        comboService = new ComboServiceImpl();
        
        // Inyectar repositorio en el servicio usando reflexión
        try {
            java.lang.reflect.Field repoField = ComboServiceImpl.class.getDeclaredField("comboRepository");
            repoField.setAccessible(true);
            repoField.set(comboService, comboRepository);
        } catch (Exception e) {
            fail("Error al inyectar repositorio: " + e.getMessage());
        }
        
        // Insertar datos iniciales para las pruebas
        em.getTransaction().begin();
        
        // Crear y persistir combos iniciales
        Combo combo1 = new Combo();
        combo1.setNombre("Combo Pupusero");
        combo1.setActivo(true);
        combo1.setDescripcionPublica("Incluye 3 pupusas a elección, 1 bebida y curtido extra");
        
        Combo combo2 = new Combo();
        combo2.setNombre("Combo Típico Familiar");
        combo2.setActivo(true);
        combo2.setDescripcionPublica("Incluye 10 pupusas variadas, 4 bebidas y curtido familiar");
        
        em.persist(combo1);
        em.persist(combo2);
        
        em.getTransaction().commit();
    }

    @Test
    public void testCrearCombo() {
        // Crear un DTO para un nuevo combo
        ComboDTO nuevoCombo = new ComboDTO();
        nuevoCombo.setNombre("Combo Desayuno");
        nuevoCombo.setActivo(true);
        nuevoCombo.setDescripcionPublica("Incluye 2 pupusas, 1 café y 1 plátano frito");
        
        // Iniciar una transacción
        em.getTransaction().begin();
        
        // Intentar crear el combo
        ComboDTO comboCreado = comboService.crearCombo(nuevoCombo);
        
        // Confirmar la transacción
        em.getTransaction().commit();
        
        // Verificar que se haya creado correctamente
        assertNotNull(comboCreado);
        assertNotNull(comboCreado.getIdCombo());
        assertEquals("Combo Desayuno", comboCreado.getNombre());
        
        // Verificar que se pueda recuperar
        Optional<ComboDTO> recuperado = comboService.obtenerComboPorId(comboCreado.getIdCombo());
        assertTrue(recuperado.isPresent());
        assertEquals("Combo Desayuno", recuperado.get().getNombre());
    }

    @Test
    public void testActualizarCombo() {
        // Crear un combo para actualizar
        ComboDTO nuevoCombo = new ComboDTO();
        nuevoCombo.setNombre("Combo Economico");
        nuevoCombo.setActivo(true);
        nuevoCombo.setDescripcionPublica("Incluye 2 pupusas y 1 refresco");
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el combo
        ComboDTO comboCreado = comboService.crearCombo(nuevoCombo);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Preparar la actualización
        ComboDTO actualizacion = new ComboDTO();
        actualizacion.setNombre("Combo Especial");
        actualizacion.setDescripcionPublica("Incluye 3 pupusas, 1 refresco y 1 postre");
        
        // Iniciar nueva transacción
        em.getTransaction().begin();
        
        // Actualizar el combo
        ComboDTO actualizado = comboService.actualizarCombo(comboCreado.getIdCombo(), actualizacion);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar la actualización
        assertNotNull(actualizado);
        assertEquals("Combo Especial", actualizado.getNombre());
        assertEquals("Incluye 3 pupusas, 1 refresco y 1 postre", actualizado.getDescripcionPublica());
        
        // Verificar que se pueda recuperar con los nuevos datos
        Optional<ComboDTO> recuperado = comboService.obtenerComboPorId(comboCreado.getIdCombo());
        assertTrue(recuperado.isPresent());
        assertEquals("Combo Especial", recuperado.get().getNombre());
    }

    @Test
    public void testEliminarCombo() {
        // Crear un combo para eliminar
        ComboDTO nuevoCombo = new ComboDTO();
        nuevoCombo.setNombre("Combo Temporal");
        nuevoCombo.setActivo(true);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear el combo
        ComboDTO comboCreado = comboService.crearCombo(nuevoCombo);
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que exista
        Optional<ComboDTO> antes = comboService.obtenerComboPorId(comboCreado.getIdCombo());
        assertTrue(antes.isPresent());
        
        // Iniciar transacción para eliminar
        em.getTransaction().begin();
        
        // Eliminar el combo
        boolean eliminado = comboService.eliminarCombo(comboCreado.getIdCombo());
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Verificar que se haya eliminado
        assertTrue(eliminado);
        
        // Verificar que ya no existe
        Optional<ComboDTO> despues = comboService.obtenerComboPorId(comboCreado.getIdCombo());
        assertFalse(despues.isPresent());
    }

    @Test
    public void testListarCombos() {
        // Listar combos (ya insertamos 2 en el setUp)
        List<ComboDTO> combos = comboService.listarCombos();
        
        // Verificar que hay al menos 2 combos (los iniciales)
        assertNotNull(combos);
        assertTrue(combos.size() >= 2);
        
        // Verificar que se encuentren nuestros combos iniciales
        boolean encontroPupusero = false;
        boolean encontroFamiliar = false;
        
        for (ComboDTO c : combos) {
            if ("Combo Pupusero".equals(c.getNombre())) {
                encontroPupusero = true;
            } else if ("Combo Típico Familiar".equals(c.getNombre())) {
                encontroFamiliar = true;
            }
        }
        
        assertTrue(encontroPupusero, "No se encontró el combo 'Combo Pupusero'");
        assertTrue(encontroFamiliar, "No se encontró el combo 'Combo Típico Familiar'");
    }

    @Test
    public void testBuscarCombosPorNombre() {
        // Crear combos con nombres específicos para buscar
        ComboDTO combo1 = new ComboDTO();
        combo1.setNombre("Combo Salvadoreño");
        combo1.setActivo(true);
        
        ComboDTO combo2 = new ComboDTO();
        combo2.setNombre("Combo Salvadoreño Especial");
        combo2.setActivo(true);
        
        ComboDTO combo3 = new ComboDTO();
        combo3.setNombre("Otro Combo");
        combo3.setActivo(true);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear los combos
        comboService.crearCombo(combo1);
        comboService.crearCombo(combo2);
        comboService.crearCombo(combo3);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Buscar combos por nombre parcial
        List<ComboDTO> resultado = comboService.buscarCombosPorNombre("Salvadoreño");
        
        // Verificar resultados
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        
        // Verificar que solo se encontraron los combos con "Salvadoreño" en el nombre
        for (ComboDTO c : resultado) {
            assertTrue(c.getNombre().contains("Salvadoreño"), 
                    "Se encontró un combo que no contiene 'Salvadoreño' en su nombre: " + c.getNombre());
        }
    }

    @Test
    public void testBuscarCombosPorEstado() {
        // Crear combos con diferentes estados
        ComboDTO comboActivo = new ComboDTO();
        comboActivo.setNombre("Combo Activo");
        comboActivo.setActivo(true);
        
        ComboDTO comboInactivo = new ComboDTO();
        comboInactivo.setNombre("Combo Inactivo");
        comboInactivo.setActivo(false);
        
        // Iniciar transacción
        em.getTransaction().begin();
        
        // Crear los combos
        comboService.crearCombo(comboActivo);
        comboService.crearCombo(comboInactivo);
        
        // Finalizar transacción
        em.getTransaction().commit();
        
        // Buscar combos activos
        List<ComboDTO> combosActivos = comboService.buscarCombosPorEstado(true);
        
        // Buscar combos inactivos
        List<ComboDTO> combosInactivos = comboService.buscarCombosPorEstado(false);
        
        // Verificar resultados
        assertNotNull(combosActivos);
        assertNotNull(combosInactivos);
        
        // Verificar que solo se encuentran combos con el estado correspondiente
        for (ComboDTO c : combosActivos) {
            assertTrue(c.getActivo(), "Se encontró un combo inactivo en la lista de activos: " + c.getNombre());
        }
        
        for (ComboDTO c : combosInactivos) {
            assertFalse(c.getActivo(), "Se encontró un combo activo en la lista de inactivos: " + c.getNombre());
        }
        
        // Debe haber al menos un combo inactivo (el que acabamos de crear)
        assertTrue(combosInactivos.size() >= 1, "No se encontraron combos inactivos");
        
        // Verificar que el combo inactivo está en la lista de inactivos
        boolean encontroInactivo = false;
        for (ComboDTO c : combosInactivos) {
            if ("Combo Inactivo".equals(c.getNombre())) {
                encontroInactivo = true;
                break;
            }
        }
        assertTrue(encontroInactivo, "No se encontró el 'Combo Inactivo' en la lista de inactivos");
    }
}