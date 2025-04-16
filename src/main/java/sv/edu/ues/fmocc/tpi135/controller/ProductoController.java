package sv.edu.ues.fmocc.tpi135.controller;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import sv.edu.ues.fmocc.tpi135.dto.ProductoDTO;
import sv.edu.ues.fmocc.tpi135.service.ProductoService;

/**
 * Controlador REST para operaciones CRUD de productos
 */
@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoController {
    
    @Inject
    private ProductoService productoService;
    
    /**
     * Crea un nuevo producto
     * @param productoDTO DTO con los datos del producto a crear
     * @return Respuesta con el producto creado y código 201 CREATED
     */
    @POST
    public Response crearProducto(ProductoDTO productoDTO) {
        try {
            ProductoDTO creado = productoService.crearProducto(productoDTO);
            return Response.status(Status.CREATED)
                    .entity(creado)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear el producto: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Actualiza un producto existente
     * @param id ID del producto a actualizar
     * @param productoDTO DTO con los datos actualizados
     * @return Respuesta con el producto actualizado y código 200 OK
     */
    @PUT
    @Path("/{id}")
    public Response actualizarProducto(@PathParam("id") Long id, ProductoDTO productoDTO) {
        try {
            ProductoDTO actualizado = productoService.actualizarProducto(id, productoDTO);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar el producto: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene un producto por su ID
     * @param id ID del producto a buscar
     * @return Respuesta con el producto encontrado o 404 si no existe
     */
    @GET
    @Path("/{id}")
    public Response obtenerProductoPorId(@PathParam("id") Long id) {
        return productoService.obtenerProductoPorId(id)
                .map(producto -> Response.ok(producto).build())
                .orElse(Response.status(Status.NOT_FOUND)
                        .entity("Producto no encontrado con ID: " + id)
                        .build());
    }
    
    /**
     * Lista todos los productos o filtra por nombre o estado
     * @param nombre Filtro opcional por nombre
     * @param activo Filtro opcional por estado activo/inactivo
     * @return Lista de productos que cumplen con los filtros
     */
    @GET
    public Response listarProductos(
            @QueryParam("nombre") String nombre,
            @QueryParam("activo") Boolean activo) {
        
        List<ProductoDTO> productos;
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            // Filtrar por nombre
            productos = productoService.buscarProductosPorNombre(nombre);
        } else if (activo != null) {
            // Filtrar por estado
            productos = productoService.buscarProductosPorEstado(activo);
        } else {
            // Listar todos
            productos = productoService.listarProductos();
        }
        
        return Response.ok(productos).build();
    }
    
    /**
     * Elimina un producto por su ID
     * @param id ID del producto a eliminar
     * @return Respuesta vacía con código 204 NO_CONTENT si se eliminó correctamente o 404 si no existe
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarProducto(@PathParam("id") Long id) {
        boolean eliminado = productoService.eliminarProducto(id);
        
        if (eliminado) {
            return Response.noContent().build();
        } else {
            return Response.status(Status.NOT_FOUND)
                    .entity("Producto no encontrado con ID: " + id)
                    .build();
        }
    }
}