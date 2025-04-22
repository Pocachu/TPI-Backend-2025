package sv.edu.ues.fmocc.tpi135.test;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.service.ProductoService;
import sv.edu.ues.fmocc.tpi135.service.ProductoServiceImpl;

/**
 * Clase para probar la inserción de un producto directamente
 * utilizando las clases internas de la API.
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class InsercionPruebaDirecta {

    @EJB
    private ProductoService productoService;
    
    @PersistenceContext(unitName = "TipicosPU")
    private EntityManager em;

    /**
     * Método principal para ejecutar la prueba de inserción
     */
    public void ejecutarPrueba() {
        try {
            // Verificar que el servicio esté inyectado correctamente
            if (productoService == null) {
                System.err.println("Error: ProductoService no fue inyectado. Creando instancia manualmente.");
                productoService = new ProductoServiceImpl();
                
                // Aquí deberíamos inyectar manualmente el repositorio también, pero 
                // para simplificar el ejemplo, asumimos que está configurado correctamente
            }
            
            // Crear un nuevo DTO de producto
            ProductoDTO nuevoProducto = new ProductoDTO();
            nuevoProducto.setNombre("Sopa de Gallina India");
            nuevoProducto.setActivo(true);
            nuevoProducto.setObservaciones("Sopa tradicional salvadoreña con gallina criolla, verduras y especias. Incluye una porción de arroz blanco.");
            
            System.out.println("Intentando crear un nuevo producto...");
            
            // Llamar al método de servicio para crear el producto
            ProductoDTO productoCreado = productoService.crearProducto(nuevoProducto);
            
            // Verificar el resultado
            if (productoCreado != null && productoCreado.getIdProducto() != null) {
                System.out.println("¡Producto creado exitosamente!");
                System.out.println("ID del producto: " + productoCreado.getIdProducto());
                System.out.println("Nombre: " + productoCreado.getNombre());
                System.out.println("Activo: " + productoCreado.getActivo());
                System.out.println("Observaciones: " + productoCreado.getObservaciones());
            } else {
                System.out.println("Error: No se pudo crear el producto.");
            }
            
        } catch (Exception e) {
            System.err.println("Error durante la ejecución de la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método principal para ejecutar la clase desde un cliente Java EE
     */
    public static void main(String[] args) {
        try {
            // Crear una instancia y ejecutar la prueba
            InsercionPruebaDirecta prueba = new InsercionPruebaDirecta();
            prueba.ejecutarPrueba();
        } catch (Exception e) {
            System.err.println("Error al ejecutar la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}