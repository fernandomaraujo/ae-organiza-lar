package com.aelares.resource;

import com.aelares.dto.request.TaskCompletionRequest;
import com.aelares.dto.response.ErrorResponse;
import com.aelares.dto.response.TaskCompletionResponse;
import com.aelares.dto.response.WeeklyPlanResponse;
import com.aelares.exception.ResourceNotFoundException;
import com.aelares.exception.UnauthorizedException;
import com.aelares.service.AuthService;
import com.aelares.service.TaskCompletionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/homes/{homeId}/tasks/{taskId}/completions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Conclusões de Tarefas", description = "Marcar/desmarcar tarefas como concluídas por data")
public class TaskCompletionResource {

    @Inject
    TaskCompletionService completionService;

    @Inject
    AuthService authService;

    /**
     * POST /api/v1/homes/{homeId}/tasks/{taskId}/completions
     * Marca a tarefa como concluída numa data específica.
     */
    @POST
    public Response complete(
            @PathParam("homeId") String homeId,
            @PathParam("taskId") String taskId,
            @HeaderParam("Authorization") String authHeader,
            @Valid TaskCompletionRequest request) {
        try {
            requireAuth(authHeader);
            UUID hId = UUID.fromString(homeId);
            UUID tId = UUID.fromString(taskId);

            TaskCompletionResponse resp = completionService.completeTask(hId, tId, request);
            return Response.status(Response.Status.CREATED).entity(resp).build();
        } catch (UnauthorizedException e) {
            return unauthorized(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest("ID inválido");
        }
    }

    /**
     * DELETE /api/v1/homes/{homeId}/tasks/{taskId}/completions/{date}
     * Desmarca a tarefa como concluída numa data específica.
     */
    @DELETE
    @Path("/{date}")
    public Response uncomplete(
            @PathParam("homeId") String homeId,
            @PathParam("taskId") String taskId,
            @PathParam("date") String date,
            @HeaderParam("Authorization") String authHeader) {
        try {
            requireAuth(authHeader);
            UUID hId = UUID.fromString(homeId);
            UUID tId = UUID.fromString(taskId);
            LocalDate completionDate = LocalDate.parse(date);

            completionService.uncompleteTask(hId, tId, completionDate);
            return Response.noContent().build();
        } catch (UnauthorizedException e) {
            return unauthorized(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest("ID ou data inválidos");
        }
    }

    /**
     * GET /api/v1/homes/{homeId}/tasks/{taskId}/completions?from=YYYY-MM-DD&to=YYYY-MM-DD
     * Lista conclusões de uma tarefa num intervalo.
     */
    @GET
    public Response getCompletions(
            @PathParam("homeId") String homeId,
            @PathParam("taskId") String taskId,
            @HeaderParam("Authorization") String authHeader,
            @QueryParam("from") String from,
            @QueryParam("to") String to) {
        try {
            requireAuth(authHeader);
            UUID hId = UUID.fromString(homeId);
            UUID tId = UUID.fromString(taskId);

            LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().withDayOfMonth(1);
            LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();

            List<TaskCompletionResponse> resp = completionService.getCompletions(hId, tId, fromDate, toDate);
            return Response.ok(resp).build();
        } catch (UnauthorizedException e) {
            return unauthorized(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest("ID ou data inválidos");
        }
    }

    // --- helpers ---

    private void requireAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token não fornecido");
        }
        String token = authHeader.substring(7);
        if (!authService.isTokenValid(token)) {
            throw new UnauthorizedException("Token inválido ou expirado");
        }
    }

    private Response unauthorized(String msg) {
        return Response.status(Response.Status.UNAUTHORIZED)
            .entity(new ErrorResponse(msg, Response.Status.UNAUTHORIZED.getStatusCode()))
            .build();
    }

    private Response notFound(String msg) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse(msg, Response.Status.NOT_FOUND.getStatusCode()))
            .build();
    }

    private Response badRequest(String msg) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse(msg, Response.Status.BAD_REQUEST.getStatusCode()))
            .build();
    }
}

