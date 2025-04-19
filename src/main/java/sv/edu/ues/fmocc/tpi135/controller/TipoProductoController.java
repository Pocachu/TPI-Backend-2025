package sv.edu.ues.fmocc.tpi135.controller;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import sv.edu.ues.fmocc.tpi135.dto.TipoProductoDTO;
import sv.edu.ues.fmocc.tpi135.service.TipoProductoService;

/**
 * Controlador REST para operaciones CRUD de tipos de productos
 */
@Path("/tipos-productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoProductoController {
    
    @Inject
    private TipoProductoService tipoProductoService;
    
    /**
     * Crea un nuevo tipo de producto
     * @param tipoProductoDTO DTO con los datos del tipo de producto a crear
     * @return Respuesta con el tipo de producto creado y código 201 CREATED
     */
    @POST
    public Response crearTipoProducto(TipoProductoDTO tipoProductoDTO) {
        try {
            TipoProductoDTO creado = tipoProductoService.crearTipoProducto(tipoProductoDTO);
            return Response.status(Status.CREATED)
                    .entity(creado)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear el tipo de producto: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Actualiza un tipo de producto existente
     * @param id ID del tipo de producto a actualizar
     * @param tipoProductoDTO DTO con los datos actualizados
     * @return Respuesta con el tipo de producto actualizado y código 200 OK
     */
    @PUT
    @Path("/{id}")
    public Response actualizarTipoProducto(@PathParam("id") Integer id, TipoProductoDTO tipoProductoDTO) {
        try {
            TipoProductoDTO actualizado = tipoProductoService.actualizarTipoProducto(id, tipoProductoDTO);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar el tipo de producto: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene un tipo de producto por su ID
     * @param id ID del tipo de producto a buscar
     * @return Respuesta con el tipo de producto encontrado o 404 si no existe
     */
    @GET
    @Path("/{id}")
    public Response obtenerTipoProductoPorId(@PathParam("id") Integer id) {
        return tipoProductoService.obtenerTipoProductoPorId(id)
                .map(tipoProducto -> Response.ok(tipoProducto).build())
                .orElse(Response.status(Status.NOT_FOUND)
                        .entity("Tipo de producto no encontrado con ID: " + id)
                        .build());
    }
    
    /**
     * Lista todos los tipos de productos o filtra por nombre o estado
     * @param nombre Filtro opcional por nombre
     * @param activo Filtro opcional por estado activo/inactivo
     * @return Lista de tipos de productos que cumplen con los filtros
     */
    @GET
    public Response listarTiposProductos(
            @QueryParam("nombre") String nombre,
            @QueryParam("activo") Boolean activo) {
        
        List<TipoProductoDTO> tiposProductos;
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            // Filtrar por nombre
            tiposProductos = tipoProductoService.buscarTiposProductosPorNombre(nombre);
        } else if (activo != null) {
            // Filtrar por estado
            tiposProductos = tipoProductoService.buscarTiposProductosPorEstado(activo);
        } else {
            // Listar todos
            tiposProductos = tipoProductoService.listarTiposProductos();
        }
        
        return Response.ok(tiposProductos).build();
    }
    
    /**
     * Elimina un tipo de producto por su ID
     * @param id ID del tipo de producto a eliminar
     * @return Respuesta vacía con código 204 NO_CONTENT si se eliminó correctamente o 404 si no existe
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarTipoProducto(@PathParam("id") Integer id) {
        boolean eliminado = tipoProductoService.eliminarTipoProducto(id);
        
        if (eliminado) {
            return Response.noContent().build();
        } else {
            return Response.status(Status.NOT_FOUND)
                    .entity("Tipo de producto no encontrado con ID: " + id)
                    .build();
        }
    }
}