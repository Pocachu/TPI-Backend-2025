package sv.edu.ues.fmocc.tpi135.test;

import javax.naming.InitialContext;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.service.ProductoService;

/**
 * Clase para probar la inserción de un producto utilizando directamente 
 * el servicio ProductoService de la API.
 */
public class InsercionPrueba {

    public static void main(String[] args) {
        try {
            // Obtener una referencia al servicio mediante JNDI (Java Naming and Directory Interface)
            InitialContext ctx = new InitialContext();
            ProductoService productoService = (ProductoService) ctx.lookup("java:global/tipicos-api/ProductoServiceImpl");
            
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
}