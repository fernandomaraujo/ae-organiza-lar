package com.aelares.resource;

import com.aelares.dto.request.CreateHomeRequest;
import com.aelares.dto.response.ErrorResponse;
import com.aelares.dto.response.HomeResponse;
import com.aelares.dto.response.WeeklyPlanResponse;
import com.aelares.exception.ResourceNotFoundException;
import com.aelares.exception.UnauthorizedException;
import com.aelares.service.AuthService;
import com.aelares.service.HomeService;
import com.aelares.service.TaskCompletionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/homes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HomeResource {

    @Inject
    HomeService homeService;

    @Inject
    AuthService authService;

    @Inject
    TaskCompletionService completionService;

    /**
     * GET /api/v1/homes - Lista todas as residências do usuário
     */
    @GET
    public Response getHomes(@HeaderParam("Authorization") String authHeader) {
        try {
            UUID userId = extractUserIdFromToken(authHeader);
            List<HomeResponse> homes = homeService.getHomesByUserId(userId);
            return Response.ok(homes).build();
        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        }
    }

    /**
     * POST /api/v1/homes - Cria uma nova residência
     */
    @POST
    public Response createHome(
        @HeaderParam("Authorization") String authHeader,
        @Valid CreateHomeRequest request) {
        try {
            UUID userId = extractUserIdFromToken(authHeader);
            HomeResponse home = homeService.createHome(userId, request);
            return Response.status(Response.Status.CREATED).entity(home).build();
        } catch (UnauthorizedException e) {
            return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        }
    }

    /**
     * GET /api/v1/homes/{id} - Busca uma residência específica
     */
    @GET
    @Path("/{id}")
    public Response getHomeById(
        @PathParam("id") String id,
        @HeaderParam("Authorization") String authHeader) {
        try {
            UUID homeId = UUID.fromString(id);
            UUID userId = extractUserIdFromToken(authHeader);

            HomeResponse home = homeService.getHomeById(homeId, userId);

            return Response.ok(home).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("ID de residencia invalido", Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                .build();
        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        }
    }

    /**
     * PUT /api/v1/homes/{id} - Atualiza uma residência
     */
    @PUT
    @Path("/{id}")
    public Response updateHome(
        @PathParam("id") String id,
        @HeaderParam("Authorization") String authHeader,
        @Valid CreateHomeRequest request) {
        try {
            UUID homeId = UUID.fromString(id);
            UUID userId = extractUserIdFromToken(authHeader);

            HomeResponse home = homeService.updateHome(homeId, userId, request);

            return Response.ok(home).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("ID de residencia invalido", Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                .build();
        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        }
    }

    /**
     * DELETE /api/v1/homes/{id} - Deleta uma residência
     */
    @DELETE
    @Path("/{id}")
    public Response deleteHome(
        @PathParam("id") String id,
        @HeaderParam("Authorization") String authHeader) {
        try {
            UUID homeId = UUID.fromString(id);
            UUID userId = extractUserIdFromToken(authHeader);

            homeService.deleteHome(homeId, userId);

            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("ID de residencia invalido", Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
        } catch (com.aelares.exception.ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                .build();
        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        }
    }

    private UUID extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token nao fornecido");
        }
        String token = authHeader.substring(7);
        return authService.extractUserIdFromToken(token);
    }

    /**
     * GET /api/v1/homes/{id}/weekly-plan?week=YYYY-MM-DD
     * Retorna o planejamento semanal do lar para a semana que contém a data informada.
     * Sem parâmetro, usa a semana corrente.
     */
    @GET
    @Path("/{id}/weekly-plan")
    public Response getWeeklyPlan(
            @PathParam("id") String id,
            @HeaderParam("Authorization") String authHeader,
            @QueryParam("week") String week) {
        try {
            UUID homeId = UUID.fromString(id);
            UUID userId = extractUserIdFromToken(authHeader);
            LocalDate referenceDate = week != null ? LocalDate.parse(week) : LocalDate.now();

            WeeklyPlanResponse plan = completionService.getWeeklyPlan(homeId, userId, referenceDate);
            return Response.ok(plan).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("ID ou data inválidos", Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                .build();
        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage(), Response.Status.UNAUTHORIZED.getStatusCode()))
                .build();
        }
    }
}

