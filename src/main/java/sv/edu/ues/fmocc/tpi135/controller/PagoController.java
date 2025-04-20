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
import sv.edu.ues.fmocc.tpi135.dto.PagoDTO;
import sv.edu.ues.fmocc.tpi135.service.PagoService;

/**
 * Controlador REST para operaciones CRUD de pagos
 */
@Path("/pagos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PagoController {
    
    @Inject
    private PagoService pagoService;
    
    /**
     * Crea un nuevo pago
     * @param pagoDTO DTO con los datos del pago a crear
     * @return Respuesta con el pago creado y código 201 CREATED
     */
    @POST
    public Response crearPago(PagoDTO pagoDTO) {
        try {
            PagoDTO creado = pagoService.crearPago(pagoDTO);
            return Response.status(Status.CREATED)
                    .entity(creado)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear el pago: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Actualiza un pago existente
     * @param id ID del pago a actualizar
     * @param pagoDTO DTO con los datos actualizados
     * @return Respuesta con el pago actualizado y código 200 OK
     */
    @PUT
    @Path("/{id}")
    public Response actualizarPago(@PathParam("id") Long id, PagoDTO pagoDTO) {
        try {
            PagoDTO actualizado = pagoService.actualizarPago(id, pagoDTO);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar el pago: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene un pago por su ID
     * @param id ID del pago a buscar
     * @return Respuesta con el pago encontrado o 404 si no existe
     */
    @GET
    @Path("/{id}")
    public Response obtenerPagoPorId(@PathParam("id") Long id) {
        return pagoService.obtenerPagoPorId(id)
                .map(pago -> Response.ok(pago).build())
                .orElse(Response.status(Status.NOT_FOUND)
                        .entity("Pago no encontrado con ID: " + id)
                        .build());
    }
    
    /**
     * Lista todos los pagos o filtra por orden, fecha o método de pago
     * @param idOrden Filtro opcional por ID de orden
     * @param fecha Filtro opcional por fecha
     * @param metodoPago Filtro opcional por método de pago
     * @return Lista de pagos que cumplen con los filtros
     */
    @GET
    public Response listarPagos(
            @QueryParam("idOrden") Long idOrden,
            @QueryParam("fecha") Date fecha,
            @QueryParam("metodoPago") String metodoPago) {
        
        List<PagoDTO> pagos;
        
        if (idOrden != null) {
            // Filtrar por orden
            pagos = pagoService.buscarPagosPorIdOrden(idOrden);
        } else if (fecha != null) {
            // Filtrar por fecha
            pagos = pagoService.buscarPagosPorFecha(fecha);
        } else if (metodoPago != null && !metodoPago.trim().isEmpty()) {
            // Filtrar por método de pago
            pagos = pagoService.buscarPagosPorMetodoPago(metodoPago);
        } else {
            // Listar todos
            pagos = pagoService.listarPagos();
        }
        
        return Response.ok(pagos).build();
    }
    
    /**
     * Elimina un pago por su ID
     * @param id ID del pago a eliminar
     * @return Respuesta vacía con código 204 NO_CONTENT si se eliminó correctamente o 404 si no existe
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarPago(@PathParam("id") Long id) {
        boolean eliminado = pagoService.eliminarPago(id);
        
        if (eliminado) {
            return Response.noContent().build();
        } else {
            return Response.status(Status.NOT_FOUND)
                    .entity("Pago no encontrado con ID: " + id)
                    .build();
        }
    }
    
    /**
     * Obtiene todos los pagos asociados a una orden
     * @param idOrden ID de la orden
     * @return Lista de pagos asociados a la orden
     */
    @GET
    @Path("/orden/{idOrden}")
    public Response obtenerPagosPorOrden(@PathParam("idOrden") Long idOrden) {
        List<PagoDTO> pagos = pagoService.buscarPagosPorIdOrden(idOrden);
        return Response.ok(pagos).build();
    }
}