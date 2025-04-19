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
import sv.edu.ues.fmocc.tpi135.dto.ComboDTO;
import sv.edu.ues.fmocc.tpi135.service.ComboService;

/**
 * Controlador REST para operaciones CRUD de combos
 */
@Path("/combos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComboController {
    
    @Inject
    private ComboService comboService;
    
    /**
     * Crea un nuevo combo
     * @param comboDTO DTO con los datos del combo a crear
     * @return Respuesta con el combo creado y código 201 CREATED
     */
    @POST
    public Response crearCombo(ComboDTO comboDTO) {
        try {
            ComboDTO creado = comboService.crearCombo(comboDTO);
            return Response.status(Status.CREATED)
                    .entity(creado)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear el combo: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Actualiza un combo existente
     * @param id ID del combo a actualizar
     * @param comboDTO DTO con los datos actualizados
     * @return Respuesta con el combo actualizado y código 200 OK
     */
    @PUT
    @Path("/{id}")
    public Response actualizarCombo(@PathParam("id") Long id, ComboDTO comboDTO) {
        try {
            ComboDTO actualizado = comboService.actualizarCombo(id, comboDTO);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar el combo: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene un combo por su ID
     * @param id ID del combo a buscar
     * @return Respuesta con el combo encontrado o 404 si no existe
     */
    @GET
    @Path("/{id}")
    public Response obtenerComboPorId(@PathParam("id") Long id) {
        return comboService.obtenerComboPorId(id)
                .map(combo -> Response.ok(combo).build())
                .orElse(Response.status(Status.NOT_FOUND)
                        .entity("Combo no encontrado con ID: " + id)
                        .build());
    }
    
    /**
     * Lista todos los combos o filtra por nombre o estado
     * @param nombre Filtro opcional por nombre
     * @param activo Filtro opcional por estado activo/inactivo
     * @return Lista de combos que cumplen con los filtros
     */
    @GET
    public Response listarCombos(
            @QueryParam("nombre") String nombre,
            @QueryParam("activo") Boolean activo) {
        
        List<ComboDTO> combos;
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            // Filtrar por nombre
            combos = comboService.buscarCombosPorNombre(nombre);
        } else if (activo != null) {
            // Filtrar por estado
            combos = comboService.buscarCombosPorEstado(activo);
        } else {
            // Listar todos
            combos = comboService.listarCombos();
        }
        
        return Response.ok(combos).build();
    }
    
    /**
     * Elimina un combo por su ID
     * @param id ID del combo a eliminar
     * @return Respuesta vacía con código 204 NO_CONTENT si se eliminó correctamente o 404 si no existe
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarCombo(@PathParam("id") Long id) {
        boolean eliminado = comboService.eliminarCombo(id);
        
        if (eliminado) {
            return Response.noContent().build();
        } else {
            return Response.status(Status.NOT_FOUND)
                    .entity("Combo no encontrado con ID: " + id)
                    .build();
        }
    }
}