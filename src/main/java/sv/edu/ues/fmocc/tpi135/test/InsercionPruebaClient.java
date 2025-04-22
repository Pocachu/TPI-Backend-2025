package sv.edu.ues.fmocc.tpi135.test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.repository.ProductoRepository;
import sv.edu.ues.fmocc.tpi135.repository.ProductoRepositoryImpl;
import sv.edu.ues.fmocc.tpi135.service.ProductoService;
import sv.edu.ues.fmocc.tpi135.service.ProductoServiceImpl;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Clase cliente para ejecutar pruebas de inserción de productos
 * utilizando directamente las clases de la API.
 * 
 * Esta implementación utiliza reflexión para evitar dependencias
 * de inyección de dependencias en un entorno no gestionado.
 */
public class InsercionPruebaClient {

    public static void main(String[] args) {
        try {
            // Crear instancias manualmente o intentar usar CDI/EJB si es posible
            ProductoService productoService = crearServicio();
            
            // Crear un nuevo producto
            ProductoDTO nuevoProducto = new ProductoDTO();
            nuevoProducto.setNombre("Sopa de Gallina India");
            nuevoProducto.setActivo(true);
            nuevoProducto.setObservaciones("Sopa tradicional salvadoreña con gallina criolla, verduras y especias. Incluye una porción de arroz blanco.");
            
            System.out.println("=== PRUEBA DE INSERCIÓN DE PRODUCTO ===");
            System.out.println("Creando producto: " + nuevoProducto.getNombre());
            
            // Insertar el producto
            ProductoDTO productoCreado = productoService.crearProducto(nuevoProducto);
            
            // Mostrar resultado
            System.out.println("\n=== RESULTADO ===");
            if (productoCreado != null && productoCreado.getIdProducto() != null) {
                System.out.println("✅ Producto creado exitosamente");
                System.out.println("ID: " + productoCreado.getIdProducto());
                System.out.println("Nombre: " + productoCreado.getNombre());
                System.out.println("Activo: " + (productoCreado.getActivo() ? "Sí" : "No"));
                System.out.println("Observaciones: " + productoCreado.getObservaciones());
            } else {
                System.out.println("❌ Error: No se pudo crear el producto");
            }
            
        } catch (Exception e) {
            System.err.println("\n❌ ERROR DURANTE LA EJECUCIÓN:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crea una instancia del servicio de productos y utiliza reflexión para
     * configurar sus dependencias si es necesario.
     */
    private static ProductoService crearServicio() throws Exception {
        ProductoService service = null;
        
        // Intento 1: Usar CDI/EJB si estamos en un entorno gestionado
        try {
            // Esto funcionará si estamos en un entorno como GlassFish con CDI
            Properties props = new Properties();
            Context context = EJBContainer.createEJBContainer(props).getContext();
            service = (ProductoService) context.lookup("java:global/tipicos-api/ProductoServiceImpl");
            System.out.println("Servicio obtenido a través de CDI/EJB");
            return service;
        } catch (NamingException e) {
            System.out.println("No se pudo obtener el servicio a través de CDI/EJB: " + e.getMessage());
            // Continuamos con el siguiente intento
        }
        
        // Intento 2: Crear manualmente las instancias y configurarlas
        try {
            System.out.println("Creando servicio manualmente...");
            ProductoServiceImpl serviceImpl = new ProductoServiceImpl();
            ProductoRepository repository = new ProductoRepositoryImpl();
            
            // Usando reflexión para inyectar el repositorio
            Field repositoryField = ProductoServiceImpl.class.getDeclaredField("productoRepository");
            repositoryField.setAccessible(true);
            repositoryField.set(serviceImpl, repository);
            
            System.out.println("Servicio creado manualmente y configurado");
            return serviceImpl;
        } catch (Exception e) {
            System.err.println("Error al crear el servicio manualmente: " + e.getMessage());
            throw e;
        }
    }
}