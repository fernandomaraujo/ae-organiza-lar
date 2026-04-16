package com.aelares.resource;

import com.aelares.dto.request.CreateTaskRequest;
import com.aelares.dto.response.TaskResponse;
import com.aelares.service.AuthService;
import com.aelares.service.HomeService;
import com.aelares.service.TaskService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/homes/{homeId}/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    @Inject
    TaskService taskService;

    @Inject
    HomeService homeService;

    @Inject
    AuthService authService;

    /**
     * GET /api/v1/homes/{homeId}/tasks - Lista todas as tarefas de uma residência
     */
    @GET
    public Response getTasks(
        @PathParam("homeId") String homeId,
        @HeaderParam("Authorization") String authHeader,
        @QueryParam("dayOfWeek") Integer dayOfWeek) {
        try {
            UUID hId = UUID.fromString(homeId);
            UUID userId = extractUserIdFromToken(authHeader);

            homeService.validateHomeOwnership(hId, userId);

            List<TaskResponse> tasks;
            if (dayOfWeek != null) {
                tasks = taskService.getTasksByDayOfWeek(hId, dayOfWeek);
            } else {
                tasks = taskService.getTasksByHomeId(hId);
            }

            return Response.ok(tasks).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * POST /api/v1/homes/{homeId}/tasks - Cria uma nova tarefa
     */
    @POST
    public Response createTask(
        @PathParam("homeId") String homeId,
        @HeaderParam("Authorization") String authHeader,
        @Valid CreateTaskRequest request) {
        try {
            UUID hId = UUID.fromString(homeId);
            UUID userId = extractUserIdFromToken(authHeader);

            homeService.validateHomeOwnership(hId, userId);
            TaskResponse task = taskService.createTask(hId, request);

            return Response.status(Response.Status.CREATED).entity(task).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new java.util.HashMap<String, String>() {{
                    put("message", e.getMessage());
                }})
                .build();
        }
    }

    /**
     * GET /api/v1/homes/{homeId}/tasks/{taskId} - Busca uma tarefa específica
     */
    @GET
    @Path("/{taskId}")
    public Response getTask(
        @PathParam("homeId") String homeId,
        @PathParam("taskId") String taskId,
        @HeaderParam("Authorization") String authHeader) {
        try {
            UUID hId = UUID.fromString(homeId);
            UUID tId = UUID.fromString(taskId);
            UUID userId = extractUserIdFromToken(authHeader);

            homeService.validateHomeOwnership(hId, userId);
            TaskResponse task = taskService.getTaskById(hId, tId);

            return Response.ok(task).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * PUT /api/v1/homes/{homeId}/tasks/{taskId} - Atualiza uma tarefa
     */
    @PUT
    @Path("/{taskId}")
    public Response updateTask(
        @PathParam("homeId") String homeId,
        @PathParam("taskId") String taskId,
        @HeaderParam("Authorization") String authHeader,
        @Valid CreateTaskRequest request) {
        try {
            UUID hId = UUID.fromString(homeId);
            UUID tId = UUID.fromString(taskId);
            UUID userId = extractUserIdFromToken(authHeader);

            homeService.validateHomeOwnership(hId, userId);
            TaskResponse task = taskService.updateTask(hId, tId, request);

            return Response.ok(task).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * DELETE /api/v1/homes/{homeId}/tasks/{taskId} - Deleta uma tarefa
     */
    @DELETE
    @Path("/{taskId}")
    public Response deleteTask(
        @PathParam("homeId") String homeId,
        @PathParam("taskId") String taskId,
        @HeaderParam("Authorization") String authHeader) {
        try {
            UUID hId = UUID.fromString(homeId);
            UUID tId = UUID.fromString(taskId);
            UUID userId = extractUserIdFromToken(authHeader);

            homeService.validateHomeOwnership(hId, userId);
            taskService.deleteTask(hId, tId);

            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private UUID extractUserIdFromToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Token não fornecido");
        }
        String token = authHeader.substring(7);
        return authService.extractUserIdFromToken(token);
    }
}

