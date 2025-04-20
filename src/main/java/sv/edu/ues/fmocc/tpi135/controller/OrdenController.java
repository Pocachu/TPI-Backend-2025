package sv.edu.ues.fmocc.tpi135.controller;

import java.util.Date;
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
import sv.edu.ues.fmocc.tpi135.dto.OrdenDTO;
import sv.edu.ues.fmocc.tpi135.service.OrdenService;

/**
 * Controlador REST para operaciones CRUD de órdenes
 */
@Path("/ordenes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdenController {
    
    @Inject
    private OrdenService ordenService;
    
    /**
     * Crea una nueva orden
     * @param ordenDTO DTO con los datos de la orden a crear
     * @return Respuesta con la orden creada y código 201 CREATED
     */
    @POST
    public Response crearOrden(OrdenDTO ordenDTO) {
        try {
            OrdenDTO creada = ordenService.crearOrden(ordenDTO);
            return Response.status(Status.CREATED)
                    .entity(creada)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear la orden: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Actualiza una orden existente
     * @param id ID de la orden a actualizar
     * @param ordenDTO DTO con los datos actualizados
     * @return Respuesta con la orden actualizada y código 200 OK
     */
    @PUT
    @Path("/{id}")
    public Response actualizarOrden(@PathParam("id") Long id, OrdenDTO ordenDTO) {
        try {
            OrdenDTO actualizada = ordenService.actualizarOrden(id, ordenDTO);
            return Response.ok(actualizada).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar la orden: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene una orden por su ID
     * @param id ID de la orden a buscar
     * @return Respuesta con la orden encontrada o 404 si no existe
     */
    @GET
    @Path("/{id}")
    public Response obtenerOrdenPorId(@PathParam("id") Long id) {
        return ordenService.obtenerOrdenPorId(id)
                .map(orden -> Response.ok(orden).build())
                .orElse(Response.status(Status.NOT_FOUND)
                        .entity("Orden no encontrada con ID: " + id)
                        .build());
    }
    
    /**
     * Lista todas las órdenes o filtra por fecha, sucursal o estado de anulación
     * @param fecha Filtro opcional por fecha
     * @param sucursal Filtro opcional por sucursal
     * @param anulada Filtro opcional por estado de anulación
     * @return Lista de órdenes que cumplen con los filtros
     */
    @GET
    public Response listarOrdenes(
            @QueryParam("fecha") Date fecha,
            @QueryParam("sucursal") String sucursal,
            @QueryParam("anulada") Boolean anulada) {
        
        List<OrdenDTO> ordenes;
        
        if (fecha != null) {
            // Filtrar por fecha
            ordenes = ordenService.buscarOrdenesPorFecha(fecha);
        } else if (sucursal != null && !sucursal.trim().isEmpty()) {
            // Filtrar por sucursal
            ordenes = ordenService.buscarOrdenesPorSucursal(sucursal);
        } else if (anulada != null) {
            // Filtrar por estado de anulación
            ordenes = ordenService.buscarOrdenesPorAnulada(anulada);
        } else {
            // Listar todas
            ordenes = ordenService.listarOrdenes();
        }
        
        return Response.ok(ordenes).build();
    }
    
    /**
     * Anula una orden por su ID
     * @param id ID de la orden a anular
     * @return Respuesta con código 204 NO_CONTENT si se anuló correctamente o 404 si no existe
     */
    @PUT
    @Path("/{id}/anular")
    public Response anularOrden(@PathParam("id") Long id) {
        boolean anulada = ordenService.anularOrden(id);
        
        if (anulada) {
            return Response.noContent().build();
        } else {
            return Response.status(Status.NOT_FOUND)
                    .entity("Orden no encontrada con ID: " + id)
                    .build();
        }
    }
    
    /**
     * Elimina una orden por su ID
     * @param id ID de la orden a eliminar
     * @return Respuesta vacía con código 204 NO_CONTENT si se eliminó correctamente o 404 si no existe
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarOrden(@PathParam("id") Long id) {
        boolean eliminada = ordenService.eliminarOrden(id);
        
        if (eliminada) {
            return Response.noContent().build();
        } else {
            return Response.status(Status.NOT_FOUND)
                    .entity("Orden no encontrada con ID: " + id)
                    .build();
        }
    }}